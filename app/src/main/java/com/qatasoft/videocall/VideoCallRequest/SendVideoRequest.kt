package com.qatasoft.videocall.VideoCallRequest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.qatasoft.videocall.Fragments.NewMessageFragment
import com.qatasoft.videocall.MyPreference
import com.qatasoft.videocall.R
import com.qatasoft.videocall.VideoChatViewActivity
import com.qatasoft.videocall.messages.ChatLogActivity
import com.qatasoft.videocall.models.Token
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.request.IApiServer
import com.squareup.picasso.Picasso
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import kotlinx.android.synthetic.main.activity_send_video_request.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SendVideoRequest : AppCompatActivity() {

    private var mRtcEngine: RtcEngine? = null

    companion object{
        val CALLER_KEY="USER_CALLER"
        val TEMP_TOKEN="VIDEO_CALL_TEMP_TOKEN"
    }

    val TAG="VideoRequest"
    var mUser= User("","","","")
    var user= User("","","","")
    var channel="hello"
    var uid= FirebaseAuth.getInstance().uid

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {

            Log.d("SendVideoRequest","Video Req Getting")

            val intent= Intent(applicationContext, VideoChatViewActivity::class.java)
            intent.putExtra(CALLER_KEY,true)
            intent.putExtra(TEMP_TOKEN,user)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_video_request)

        val myPreference= MyPreference(this)
        mUser=myPreference.getUserInfo()

        user = intent.getParcelableExtra(NewMessageFragment.USER_KEY)
        Toast.makeText(this, user.uid,Toast.LENGTH_SHORT).show()


        setUserInfo()
        getToken()


    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this,
                        permission) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(permission),
                    requestCode)
            return false
        }
        return true
    }

    private fun setUserInfo() {
        Picasso.get().load(user.profileImageUrl).into(send_req_circleimage_user)

        send_req_text_username.text=user.username
    }

    //Tokeni apiden retrofit ile alma işlemi
    fun getToken(){
        val retrofit= Retrofit.Builder().baseUrl("https://videocallkotlin.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val apiService=retrofit.create(IApiServer::class.java)

        val response=apiService.getToken(channel, 0)

        response.enqueue(object: Callback<Token> {
            override fun onFailure(call: Call<Token>, t: Throwable) {
                Log.d(ChatLogActivity.TAG,t.message.toString())
            }

            override fun onResponse(call: Call<Token>, response: Response<Token>) {
                val generatedToken= response.body()!!.token
                Log.d(TAG, "token : $generatedToken")

                user=User(user.profileImageUrl, user.uid, user.username,generatedToken)
                mUser=User(mUser.profileImageUrl, mUser.uid, mUser.username,generatedToken)
                addVideoRequest()

                if (checkSelfPermission(Manifest.permission.CAMERA, 23)) {
                    initializeAgoraEngine()
                }
            }
        })
    }

    fun addVideoRequest(){
        val toId = user.uid

        val ref = FirebaseDatabase.getInstance().getReference("/videorequests/$toId/$uid")

        ref.setValue(mUser)

        onVideoRequestWait()
    }

    fun onVideoRequestWait(){
        val toId = user.uid

        val ref = FirebaseDatabase.getInstance().getReference("/videorequests/$toId/$uid")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d(TAG,"Video Confirmed By Target User")
                Log.d(ChatLogActivity.TAG,"Changed")

                val intent= Intent(applicationContext, VideoChatViewActivity::class.java)
                intent.putExtra(CALLER_KEY,true)
                intent.putExtra(TEMP_TOKEN,user)
                startActivity(intent)
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {
                Toast.makeText(applicationContext,"Video Call Rejected",Toast.LENGTH_LONG).show()
                finish()


            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun setupLocalVideo() {
        val container = findViewById<FrameLayout>(R.id.send_req_local_video_view_container)
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))

        joinChannel()
    }

    private fun initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, getString(R.string.agora_app_id), mRtcEventHandler)
        } catch (e: Exception) {

            throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
        }

        setupVideoProfile()
    }

    private fun setupVideoProfile() {
        mRtcEngine!!.enableVideo()
//      mRtcEngine!!.setVideoProfile(Constants.VIDEO_PROFILE_360P, false) // Earlier than 2.3.0
        mRtcEngine!!.setVideoEncoderConfiguration(VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT))
        setupLocalVideo()
    }

    private fun joinChannel() {
        mRtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);

        Log.d(TAG,"Token All : "+user.token)
        mRtcEngine!!.joinChannel(user.token, "hello", "Extra Optional Data", 0) // if you do not specify the uid, we will generate the uid for you
    }
}
