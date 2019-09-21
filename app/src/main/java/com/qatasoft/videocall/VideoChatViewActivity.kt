package com.qatasoft.videocall

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.qatasoft.videocall.VideoCallRequest.SendVideoRequest
import com.qatasoft.videocall.models.User
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import kotlinx.android.synthetic.main.activity_video_chat_view.*
import kotlinx.android.synthetic.main.menu_left_drawer_live_call.*

class VideoChatViewActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
        private const val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
    }

    private var mRtcEngine: RtcEngine? = null
    private val logTAG = "VideoChatViewActivity"
    private var isCaller = false
    private var user = User("", "", "", "")
    private var mUser = User("", "", "", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat_view)

        //Getting General info about signing user and target user which we talk with video
        getGeneralInfo()

        //Injecting Sliding Root NavBuilder Menu which left of the screen
        injectSlidingRootNav()

        //Check the permissions and start the video calling
        initAgoraEngineAndJoinChannel()

        //When turn on the video swipe which in the SlidingRootNav
        menu_swipe_video.onSwipedOnListener = {
            mRtcEngine!!.muteLocalVideoStream(true)

            val container = local_video_view_container as FrameLayout
            val surfaceView = container.getChildAt(0) as SurfaceView
            surfaceView.setZOrderMediaOverlay(false)
            surfaceView.visibility = if (true) View.GONE else View.VISIBLE
        }

        //When turn off the video swipe which in the SlidingRootNav
        menu_swipe_video.onSwipedOffListener = {
            mRtcEngine!!.muteLocalVideoStream(false)

            val container = local_video_view_container as FrameLayout
            val surfaceView = container.getChildAt(0) as SurfaceView
            surfaceView.setZOrderMediaOverlay(true)
            surfaceView.visibility = if (false) View.GONE else View.VISIBLE
        }

        //When turn on the voice swipe which in the SlidingRootNav
        menu_swipe_voice.onSwipedOnListener = {
            mRtcEngine!!.muteLocalAudioStream(true)
        }

        //When turn off the voice swipe which in the SlidingRootNav
        menu_swipe_voice.onSwipedOffListener = {
            mRtcEngine!!.muteLocalAudioStream(false)
        }
    }

    private fun getGeneralInfo() {
        val myPreference = MyPreference(this)
        mUser = myPreference.getUserInfo()

        user = intent.getParcelableExtra(SendVideoRequest.TEMP_TOKEN)
        isCaller = intent.getBooleanExtra(SendVideoRequest.CALLER_KEY, false)
    }

    private fun injectSlidingRootNav() {
        //Implementing the Sliding Root Navigation
        val slidingRootNavBuilder = SlidingRootNavBuilder(this).withToolbarMenuToggle(video_chat_toolbar)
                .withMenuOpened(false)
                .withMenuLayout(R.layout.menu_left_drawer_live_call)

        slidingRootNavBuilder.inject()
    }

    private fun initAgoraEngineAndJoinChannel() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initializeAgoraEngine()
            setupVideoProfile()
            setupLocalVideo()
            joinChannel()
        }
    }

    //Checking Permissions For Camera and Audio
    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i(logTAG, "checkSelfPermission $permission $requestCode")
        if (ContextCompat.checkSelfPermission(this,
                        permission) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(permission),
                    requestCode)
            return false
        }
        return true
    }

    //When Permission Results
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        Log.i(logTAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode)

        when (requestCode) {
            PERMISSION_REQ_ID_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO)
                    finish()
                }
            }
            PERMISSION_REQ_ID_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel()
                } else {
                    showLongToast("No permission for " + Manifest.permission.CAMERA)
                    finish()
                }
            }
        }
    }

    private fun showLongToast(msg: String) {
        this.runOnUiThread { Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show() }
    }

    //On Channel Leaving Destroys the Session
    override fun onDestroy() {
        super.onDestroy()

        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }


    fun onLocalVideoMuteClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            //If the image is not pre-selected we should filter the image for understand
            iv.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        mRtcEngine!!.muteLocalVideoStream(iv.isSelected)

        val container = local_video_view_container as FrameLayout
        val surfaceView = container.getChildAt(0) as SurfaceView
        surfaceView.setZOrderMediaOverlay(!iv.isSelected)
        surfaceView.visibility = if (iv.isSelected) View.GONE else View.VISIBLE
    }

    fun onLocalAudioMuteClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        mRtcEngine!!.muteLocalAudioStream(iv.isSelected)
    }

    fun onSwitchCameraClicked() {
        mRtcEngine!!.switchCamera()
    }

    fun onEncCallClicked() {
        finish()
    }

    private fun initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, getString(R.string.agora_app_id), mRtcEventHandler)
        } catch (e: Exception) {
            Log.e(logTAG, Log.getStackTraceString(e))

            throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
        }
    }

    private fun setupVideoProfile() {
        mRtcEngine!!.enableVideo()
//      mRtcEngine!!.setVideoProfile(Constants.VIDEO_PROFILE_360P, false) // Earlier than 2.3.0
        mRtcEngine!!.setVideoEncoderConfiguration(VideoEncoderConfiguration(VideoEncoderConfiguration.VD_840x480,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.COMPATIBLE_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT))
    }

    private fun setupLocalVideo() {
        val container = local_video_view_container as FrameLayout
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun joinChannel() {
        mRtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)

        mRtcEngine!!.joinChannel(user.token, "hello", "Extra Optional Data", 0) // if you do not specify the uid, we will generate the uid for you
    }

    private fun setupRemoteVideo(uid: Int) {
        val container = remote_video_view_container as FrameLayout

        if (container.childCount >= 1) {
            return
        }

        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        container.addView(surfaceView)
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))

        surfaceView.tag = uid // for mark purpose
    }

    private fun leaveChannel() {
        if (isCaller) {
            val ref = FirebaseDatabase.getInstance().getReference("/videorequests/${user.uid}/${mUser.uid}")

            ref.removeValue()
        }

        mRtcEngine!!.leaveChannel()

        startService(Intent(this, BackgroundService::class.java))

        startActivity(Intent(this, MainActivity::class.java))
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

    }

    private fun onRemoteUserLeft() {
        val container = remote_video_view_container as FrameLayout
        container.removeAllViews()

        if (isCaller) {
            val ref = FirebaseDatabase.getInstance().getReference("/videorequests/${user.uid}/${mUser.uid}")

            ref.removeValue()
        }
        startService(Intent(this, BackgroundService::class.java))

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun onRemoteUserVideoMuted(uid: Int, muted: Boolean) {
        val container = remote_video_view_container as FrameLayout

        val surfaceView = container.getChildAt(0) as SurfaceView

        val tag = surfaceView.tag
        if (tag != null && tag as Int == uid) {
            surfaceView.visibility = if (muted) View.GONE else View.VISIBLE
        }
    }

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { onRemoteUserLeft() }
        }

        override fun onUserMuteVideo(uid: Int, muted: Boolean) {
            runOnUiThread { onRemoteUserVideoMuted(uid, muted) }
        }
    }
}
