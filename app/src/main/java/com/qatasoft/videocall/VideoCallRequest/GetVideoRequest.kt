package com.qatasoft.videocall.VideoCallRequest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.qatasoft.videocall.*
import com.qatasoft.videocall.models.User
import com.squareup.picasso.Picasso
import io.fotoapparat.Fotoapparat
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.back
import io.fotoapparat.view.CameraView
import kotlinx.android.synthetic.main.activity_get_video_request.*
import kotlinx.android.synthetic.main.activity_send_video_request.*

class GetVideoRequest : AppCompatActivity() {

    var user= User("","","","")
    var mUser= User("","","","")

    var fotoapparat: Fotoapparat? = null
    var fotoapparatState: FotoapparatState? = null
    var cameraStatus: CameraState? = null

    val permissions = arrayOf(android.Manifest.permission.CAMERA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_video_request)

        stopService(Intent(this, BackgroundService::class.java))

        val myPreference= MyPreference(this)
        mUser=myPreference.getUserInfo()

        user = intent.getParcelableExtra(SendVideoRequest.TEMP_TOKEN)
        Toast.makeText(this, user.username,Toast.LENGTH_SHORT).show()

        createFotoapparat()

        cameraStatus = CameraState.BACK
        fotoapparatState = FotoapparatState.OFF

        setUserInfo()

        btnConfirm.setOnClickListener {
            Toast.makeText(this,"Kabul Edildi",Toast.LENGTH_LONG).show()

            val ref = FirebaseDatabase.getInstance().getReference("/videorequests/${mUser.uid}/${user.uid}")

            ref.setValue(mUser)

            val intent= Intent(applicationContext, VideoChatViewActivity::class.java)
            intent.putExtra(SendVideoRequest.CALLER_KEY,false)
            intent.putExtra(SendVideoRequest.TEMP_TOKEN,user)
            startActivity(intent)
        }

        btnReject.setOnClickListener {
            Toast.makeText(this,"Red Edildi",Toast.LENGTH_LONG).show()

            val ref = FirebaseDatabase.getInstance().getReference("/videorequests/${mUser.uid}/${user.uid}")

            ref.removeValue()

            startActivity(Intent(applicationContext, MainActivity::class.java))
        }

    }

    private fun createFotoapparat() {
        val cameraView = findViewById<CameraView>(R.id.camera_view)

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

    private fun hasNoPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 0)
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

    private fun setUserInfo() {
        Picasso.get().load(user.profileImageUrl).into(get_req_circleimage_user)

        get_req_text_username.text = user.username
    }
}
