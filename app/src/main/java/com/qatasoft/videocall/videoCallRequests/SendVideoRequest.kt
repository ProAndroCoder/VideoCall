package com.qatasoft.videocall.videoCallRequests

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.qatasoft.videocall.*
import com.qatasoft.videocall.MainActivity.Companion.mUser
import com.qatasoft.videocall.bottomFragments.MessagesFragment.Companion.USER_KEY
import com.qatasoft.videocall.messages.ChatLogActivity
import com.qatasoft.videocall.models.Token
import com.qatasoft.videocall.models.Tools
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.request.FBaseControl
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

@Suppress("DUPLICATE_LABEL_IN_WHEN")
class SendVideoRequest : AppCompatActivity() {
    val logTAG = "VideoRequest"
    var user = User()
    private var channel = "hello"

    private var fotoapparat: Fotoapparat? = null
    private var fotoapparatState: FotoapparatState? = null
    private var cameraStatus: CameraState? = null

    private val PERMISSION_REQUEST = 98

    val fBaseControl = FBaseControl()
    private var permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

    companion object {
        const val CALLER_KEY = "USER_CALLER"
        const val TEMP_TOKEN = "VIDEO_CALL_TEMP_TOKEN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_video_request)

        getGeneralInfo()

        stopService(Intent(this, BackgroundService::class.java))

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

    private fun getVideoRequest() {
        createFotoapparat()

        fotoapparat?.start()
        fotoapparatState = FotoapparatState.ON

        //Get Token from api with retrofit
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

                user = User(user.profileImageUrl, user.uid, user.username, generatedToken)
                mUser = User(mUser.profileImageUrl, mUser.uid, mUser.username, generatedToken)

                //Adding VideoRequest
                addVideoRequest()
            }
        })
    }

    private fun getGeneralInfo() {
        val myPreference = MyPreference(this)
        mUser = myPreference.getUserInfo()

        user = intent.getParcelableExtra(USER_KEY)!!

        Glide.with(this).load(user.profileImageUrl).into(send_req_circleimage_user)

        send_req_text_username.text = user.username
    }

    private fun rejectCall() {
        fBaseControl.callRequestOperations(mUser.uid, user.uid, Tools.removeRequest, Tools.videoReqType)

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

    fun addVideoRequest() {
        fBaseControl.callRequestOperations(mUser.uid, user.uid, Tools.addRequest, Tools.videoReqType)

        onVideoRequestWait()
    }

    private fun onVideoRequestWait() {
        val ref = FirebaseDatabase.getInstance().getReference("/videorequests/${user.uid}/${mUser.uid}")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d(logTAG, "Video Call Confirmed")

                fBaseControl.callRequestOperations(mUser.uid, user.uid, Tools.addRequestLog, Tools.reqLogType)

                val intent = Intent(applicationContext, VideoChatViewActivity::class.java)
                intent.putExtra(CALLER_KEY, true)
                intent.putExtra(TEMP_TOKEN, user)
                startActivity(intent)
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {
                fBaseControl.callRequestOperations(mUser.uid, user.uid, Tools.addRequestLog, Tools.reqLogType)

                Toast.makeText(applicationContext, "Video Call Rejected", Toast.LENGTH_LONG).show()
                finish()
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun checkPermissions(context: Context, permissionArray: Array<String>): Boolean {
        var allSuccess = true
        permissionArray.indices.forEach { i ->
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED) {
                allSuccess = false
            }
        }
        return allSuccess
    }

    override fun onBackPressed() {
        super.onBackPressed()
        rejectCall()
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermissions(this, permissions)) {
                getVideoRequest()
                Toast.makeText(this, "Permission Already Provided", Toast.LENGTH_SHORT).show()
            } else {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        } else {
            finish()
            Toast.makeText(this, "Version Problem", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        fotoapparat?.stop()
        fotoapparatState = FotoapparatState.OFF
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            permissions.indices.forEach { i ->
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[i])
                    if (requestAgain) {
                        finish()
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    } else {
                        finish()
                        Toast.makeText(this, "Go to Settings and enable permissions.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            if (allSuccess) {
                getVideoRequest()
                Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

enum class CameraState {
    FRONT, BACK
}

enum class FotoapparatState {
    ON, OFF
}
