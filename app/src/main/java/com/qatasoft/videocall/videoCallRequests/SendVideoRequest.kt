package com.qatasoft.videocall.videoCallRequests

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.qatasoft.videocall.*
import com.qatasoft.videocall.bottomFragments.MessagesFragment.Companion.USER_KEY
import com.qatasoft.videocall.messages.ChatLogActivity
import com.qatasoft.videocall.models.ChatMessage
import com.qatasoft.videocall.models.Token
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.request.IApiServer
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.back
import io.fotoapparat.selector.front
import kotlinx.android.synthetic.main.activity_send_video_request.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DUPLICATE_LABEL_IN_WHEN")
class SendVideoRequest : AppCompatActivity() {
    val logTAG = "VideoRequest"
    var mUser = User("", "", "", "", "", "", "", false)
    var user = User("", "", "", "", "", "", "", false)
    private var channel = "hello"
    var uid = FirebaseAuth.getInstance().uid

    private var fotoapparat: Fotoapparat? = null
    private var fotoapparatState: FotoapparatState? = null
    private var cameraStatus: CameraState? = null

    companion object {
        const val CALLER_KEY = "USER_CALLER"
        const val TEMP_TOKEN = "VIDEO_CALL_TEMP_TOKEN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_video_request)

        requestPermission()

        getGeneralInfo()

        saveVideoChatInfo()

        stopService(Intent(this, BackgroundService::class.java))

        createFotoapparat()

        getToken()

        send_req_end_call.setOnClickListener {
            rejectCall()
        }

        send_req_change_cam.setOnClickListener {
            switchCamera()
        }

        send_req_chat.setOnClickListener {
            rejectCall()

            //Seçilen kişiyi ChatLogActivity'e gönderme işlemi (Mesajlaşma)
            val intent = Intent(this, ChatLogActivity::class.java)
            //Başka activitye nesne gönderme Parcelable
            intent.putExtra(USER_KEY, user)
            startActivity(intent)
        }
    }

    private fun getGeneralInfo() {
        val myPreference = MyPreference(this)
        mUser = myPreference.getUserInfo()

        user = intent.getParcelableExtra(USER_KEY)!!

        Glide.with(this).load(user.profileImageUrl).into(send_req_circleimage_user)

        send_req_text_username.text = user.username
    }

    override fun onBackPressed() {
        super.onBackPressed()
        rejectCall()
    }

    private fun rejectCall() {
        val ref = FirebaseDatabase.getInstance().getReference("/videorequests/${user.uid}/${mUser.uid}")

        ref.removeValue()

        finish()
    }

    private fun createFotoapparat() {
        val cameraView = send_req_camera_view

        fotoapparat = Fotoapparat(
                context = this,
                view = cameraView,
                scaleType = ScaleType.CenterCrop,
                lensPosition = back(),
                logger = loggers(
                        logcat()
                ),
                cameraErrorCallback = { error ->
                    println("Recorder errors: $error")
                }
        )

        cameraStatus = CameraState.BACK
        fotoapparatState = FotoapparatState.OFF
    }

    private fun switchCamera() {
        fotoapparat?.switchTo(
                lensPosition = if (cameraStatus == CameraState.BACK) front() else back(),
                cameraConfiguration = CameraConfiguration()
        )

        if (cameraStatus == CameraState.BACK) {
            cameraStatus = CameraState.FRONT
            Log.d(logTAG, "Switched To FRONT Camera")
        } else {
            cameraStatus = CameraState.BACK
            Log.d(logTAG, "Switched To BACK Camera")
        }
    }

    override fun onStart() {
        super.onStart()
        if (hasNoPermissions()) {
            requestPermission()
        } else {
            fotoapparat?.start()
            fotoapparatState = FotoapparatState.ON
        }
    }

    override fun onStop() {
        super.onStop()
        fotoapparat?.stop()
        fotoapparatState = FotoapparatState.OFF
    }

    override fun onResume() {
        super.onResume()
        if (!hasNoPermissions() && fotoapparatState == FotoapparatState.OFF) {
            val intent = Intent(baseContext, SendVideoRequest::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
    }

    //Tokeni apiden retrofit ile alma işlemi
    private fun getToken() {
        val retrofit = Retrofit.Builder().baseUrl("https://videocallkotlin.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val apiService = retrofit.create(IApiServer::class.java)

        val response = apiService.getToken(channel, 0)

        response.enqueue(object : Callback<Token> {
            override fun onFailure(call: Call<Token>, t: Throwable) {
                Log.d(ChatLogActivity.logTAG, t.message.toString())
            }

            override fun onResponse(call: Call<Token>, response: Response<Token>) {
                val generatedToken = response.body()!!.token
                Log.d(logTAG, "token : $generatedToken")

                user = User(user.profileImageUrl, user.uid, user.username, generatedToken, "", "", "", false)
                mUser = User(mUser.profileImageUrl, mUser.uid, mUser.username, generatedToken, "", "", "", false)

                addVideoRequest()
            }
        })
    }


    fun addVideoRequest() {
        val toId = user.uid

        val ref = FirebaseDatabase.getInstance().getReference("/videorequests/$toId/$uid")

        ref.setValue(mUser)

        onVideoRequestWait()
    }

    private fun onVideoRequestWait() {
        val toId = user.uid

        val ref = FirebaseDatabase.getInstance().getReference("/videorequests/$toId/$uid")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d(logTAG, "Video Call Confirmed")

                val intent = Intent(applicationContext, VideoChatViewActivity::class.java)
                intent.putExtra(CALLER_KEY, true)
                intent.putExtra(TEMP_TOKEN, user)
                startActivity(intent)
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {
                Toast.makeText(applicationContext, "Video Call Rejected", Toast.LENGTH_LONG).show()
                finish()


            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun saveVideoChatInfo() {
        val sendingTime = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault()).format(Date())

        val latestCallRef = FirebaseDatabase.getInstance().getReference("/latest-calls/${mUser.uid}/${user.uid}")

        val chatMessage = ChatMessage(latestCallRef.key!!, "Outgoing Call", mUser.uid, user.uid, sendingTime)
        latestCallRef.setValue(chatMessage)

        val latestToCallRef = FirebaseDatabase.getInstance().getReference("/latest-calls/${user.uid}/${mUser.uid}")
        val chatMessage2 = ChatMessage(latestCallRef.key!!, "Incoming Call", mUser.uid, user.uid, sendingTime)
        latestToCallRef.setValue(chatMessage2)
    }
}

enum class CameraState {
    FRONT, BACK
}

enum class FotoapparatState {
    ON, OFF
}
