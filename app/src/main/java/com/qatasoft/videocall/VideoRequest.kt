package com.qatasoft.videocall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.qatasoft.videocall.Fragments.NewMessageFragment
import com.qatasoft.videocall.models.User

class VideoRequest : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_request)

        Toast.makeText(this,intent.getParcelableExtra<User>(NewMessageFragment.USER_KEY).token,Toast.LENGTH_LONG).show()
    }
}
