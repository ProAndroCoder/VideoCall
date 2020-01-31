package com.qatasoft.videocall.videoCallRequests

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.qatasoft.videocall.*
import com.qatasoft.videocall.bottomFragments.MessagesFragment.Companion.USER_KEY
import com.qatasoft.videocall.messages.ChatLogActivity
import com.qatasoft.videocall.models.Tools
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.request.FBaseControl
import io.fotoapparat.Fotoapparat
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.back
import kotlinx.android.synthetic.main.activity_get_video_request.*

class GetVideoRequest : AppCompatActivity() {

    var user = User()
    private var mUser = User()
    private val logTAG = "GetVideoRequest"

    private var fotoapparat: Fotoapparat? = null
    private var fotoapparatState: FotoapparatState? = null
    private var cameraStatus: CameraState? = null
    private var ringtone: Ringtone? = null

    private val PERMISSION_REQUEST = 98
    private var permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

    private val fBaseControl = FBaseControl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_video_request)

        getGeneralInfo()

        stopService(Intent(this, BackgroundService::class.java))

        get_req_swipe_confirm.onSwipedOnListener = {
            confirmCall()
        }

        get_req_swipe_reject.onSwipedOffListener = {
            rejectCall()
        }

        get_req_chat.setOnClickListener {
            rejectCall()

            //Seçilen kişiyi ChatLogActivity'e gönderme işlemi (Mesajlaşma)
            val intent = Intent(this, ChatLogActivity::class.java)
            //Başka activitye nesne gönderme Parcelable
            intent.putExtra(USER_KEY, user)
            startActivity(intent)
        }
        playRingtone()
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

    private fun playRingtone() {
        ringtone = defaultRingtone

        ringtone!!.play()
    }

    private fun stopRingtone() {
        ringtone!!.stop()
    }

    private fun getGeneralInfo() {
        val myPreference = MyPreference(this)
        mUser = myPreference.getUserInfo()

        user = intent.getParcelableExtra(SendVideoRequest.TEMP_TOKEN)!!

        Glide.with(this).load(user.profileImageUrl).into(get_req_circleimage_user)

        get_req_text_username.text = user.username
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (ringtone!!.isPlaying) {
            ringtone!!.stop()
        }
        moveTaskToBack(true)
    }

    private fun confirmCall() {
        fBaseControl.callRequestOperations(mUser.uid, user.uid, Tools.addRequest, Tools.videoReqType)

        stopRingtone()

        val intent = Intent(applicationContext, VideoChatViewActivity::class.java)
        intent.putExtra(SendVideoRequest.CALLER_KEY, false)
        intent.putExtra(SendVideoRequest.TEMP_TOKEN, user)
        startActivity(intent)
    }

    private fun rejectCall() {
        fBaseControl.callRequestOperations(mUser.uid, user.uid, Tools.removeRequest, Tools.videoReqType)

        ringtone!!.stop()

        startService(Intent(this, BackgroundService::class.java))

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

        finish()
    }

    private fun createFotoapparat() {
        val cameraView = get_req_camera_view

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

    private fun getVideoRequest() {
        createFotoapparat()

        fotoapparat?.start()
        fotoapparatState = FotoapparatState.ON
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
            rejectCall()
            Toast.makeText(this, "Version Problem", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        ringtone!!.stop()
        fotoapparat?.stop()
        fotoapparatState = FotoapparatState.OFF
    }

    override fun onResume() {
        super.onResume()
        if (!hasNoPermissions() && fotoapparatState == FotoapparatState.OFF) {
            Log.i(logTAG, "OnResume")
        }
    }

    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    override fun onRestart() {
        super.onRestart()
        ringtone!!.stop()
        Log.i(logTAG, "OnRestart")
        startActivity(Intent(this, MainActivity::class.java))
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
                        rejectCall()
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                    } else {
                        rejectCall()
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

// Extension property to get default ringtone
val Context.defaultRingtone: Ringtone
    get() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        return RingtoneManager.getRingtone(this, uri)
    }
