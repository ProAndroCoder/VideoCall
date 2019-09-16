package com.qatasoft.videocall.VideoCallRequest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.MyPreference
import com.qatasoft.videocall.R
import com.qatasoft.videocall.VideoChatViewActivity
import com.qatasoft.videocall.models.User
import kotlinx.android.synthetic.main.activity_get_video_request.*

class GetVideoRequest : AppCompatActivity() {

    var user= User("","","","")
    var mUser= User("","","","")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_video_request)

        val myPreference= MyPreference(this)
        mUser=myPreference.getUserInfo()

        user = intent.getParcelableExtra(SendVideoRequest.TEMP_TOKEN)
        Toast.makeText(this, user.username,Toast.LENGTH_SHORT).show()

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
}
