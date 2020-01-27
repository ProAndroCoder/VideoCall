package com.qatasoft.videocall

import android.Manifest
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.qatasoft.videocall.models.ChatMessage
import com.qatasoft.videocall.videoCallRequests.SendVideoRequest
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.views.ChatFromItem
import com.qatasoft.videocall.views.ChatToItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import kotlinx.android.synthetic.main.activity_video_chat_view.*
import kotlinx.android.synthetic.main.activity_video_chat_view.rel
import kotlinx.android.synthetic.main.menu_left_drawer_live_call.*

class VideoChatViewActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
        private const val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
    }

    val adapter = GroupAdapter<ViewHolder>()
    private var mRtcEngine: RtcEngine? = null
    private val logTAG = "VideoChatViewActivity"
    private var isCaller = false
    private var user = User("", "", "", "", "", "", "", false)
    private var mUser = User("", "", "", "", "", "", "", false)
    private var isFront = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat_view)

        //Getting General info about signing user and target user which we talk with video
        getGeneralInfo()

        //Injecting Sliding Root NavBuilder Menu which left of the screen
        injectSlidingRootNav()

        //Check the permissions and start the video calling
        initAgoraEngineAndJoinChannel()

        imgChat.setOnClickListener {
            if (rel.visibility == View.VISIBLE) {
                rel.visibility = View.INVISIBLE
                live_chat.visibility = View.INVISIBLE
            } else {
                rel.visibility = View.VISIBLE
                live_chat.visibility = View.VISIBLE

                fetchMessages()
            }
        }

        //When turn on or turn off the video swipe which in the SlidingRootNav
        menu_swipe_video.setOnStateChangeListener {
            if (it) {
                menu_swipe_video.setSlidingButtonBackground(ContextCompat.getDrawable(this, R.drawable.rounded_red))

                mRtcEngine!!.muteLocalVideoStream(true)

                val container = local_video_view_container as FrameLayout
                val surfaceView = container.getChildAt(0) as SurfaceView
                surfaceView.setZOrderMediaOverlay(false)
                surfaceView.visibility = View.GONE
            } else {
                menu_swipe_video.setSlidingButtonBackground(ContextCompat.getDrawable(this, R.drawable.rounded_green))

                mRtcEngine!!.muteLocalVideoStream(false)

                val container = local_video_view_container as FrameLayout
                val surfaceView = container.getChildAt(0) as SurfaceView
                surfaceView.setZOrderMediaOverlay(true)
                surfaceView.visibility = View.VISIBLE
            }
        }

        //When turn on or off the voice swipe which in the SlidingRootNav
        menu_swipe_voice.setOnStateChangeListener {
            if (it) {
                menu_swipe_voice.setSlidingButtonBackground(ContextCompat.getDrawable(this, R.drawable.rounded_red))
                mRtcEngine!!.muteLocalAudioStream(true)
            } else {
                menu_swipe_voice.setSlidingButtonBackground(ContextCompat.getDrawable(this, R.drawable.rounded_green))

                mRtcEngine!!.muteLocalAudioStream(false)
            }
        }

        //When Switch Cam on the SlidingRootNav Clicked The camera changing (Front - Back)
        menu_linear_switch_cam.setOnClickListener {
            if (isFront) {
                switch_cam_img.background = ContextCompat.getDrawable(this, R.drawable.rounded_normal)
                isFront = false
            } else {
                switch_cam_img.background = ContextCompat.getDrawable(this, R.drawable.rounded_grey)
                isFront = true
            }
            mRtcEngine!!.switchCamera()
        }

        //When end Call button clicked. The user left the channel.End Call is finished.
        video_chat_endCall.setOnClickListener {
            leaveChannel()
        }
    }

    private fun fetchMessages() {
        adapter.clear()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(logTAG, chatMessage.text)
                    val currentUser = mUser

                    if (fromId == chatMessage.fromId && user.uid == chatMessage.toId) {
                        adapter.add(ChatFromItem(chatMessage, currentUser,applicationContext))
                    } else if (fromId == chatMessage.toId && user.uid == chatMessage.fromId) {
                        adapter.add(ChatToItem(chatMessage, user,applicationContext))
                    }

                    live_chat.scrollToPosition(adapter.itemCount - 1)
                }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    //On Home Button Pressed
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        val width = size.x
        val height = size.y

        val aspectRatio = Rational(width, height)

        val mPictureInPictureParamsBuilder = PictureInPictureParams.Builder()

        mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build()

        enterPictureInPictureMode(mPictureInPictureParamsBuilder.build())
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (!isInPictureInPictureMode) {
            leaveChannel()
        }
    }

    //Removes the video call data from firebase
    private fun removeFirebaseData() {
        if (isCaller) {
            val ref = FirebaseDatabase.getInstance().getReference("/videorequests/${user.uid}/${mUser.uid}")

            ref.removeValue()
        }
    }

    private fun getGeneralInfo() {
        live_chat.adapter = adapter

        val myPreference = MyPreference(this)
        mUser = myPreference.getUserInfo()

        user = intent.getParcelableExtra(SendVideoRequest.TEMP_TOKEN)!!
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
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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
        removeFirebaseData()

        mRtcEngine!!.leaveChannel()

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
            runOnUiThread { leaveChannel() }
        }

        override fun onUserMuteVideo(uid: Int, muted: Boolean) {
            runOnUiThread { onRemoteUserVideoMuted(uid, muted) }
        }
    }
}
