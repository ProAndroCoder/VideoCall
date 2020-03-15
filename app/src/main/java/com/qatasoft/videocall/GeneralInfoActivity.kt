package com.qatasoft.videocall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.RelativeLayout
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.qatasoft.videocall.data.db.entities.GeneralInfo
import kotlinx.android.synthetic.main.activity_general_info.*
import java.io.File

class GeneralInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_info)

        val generalInfo = intent.getParcelableExtra<GeneralInfo>("GeneralInfo")

        if (generalInfo != null) {
            //Picasso.get().load(generalInfo.img_general_url).into(img_general_info)

            title_general.text = generalInfo.title
            text_general.text = generalInfo.text
        }

        videoView.setVideoPath("/storage/emulated/0/Movies/VID_20200122_113026.mp4")
        val mediaController = MediaController(this)

        videoView.setMediaController(mediaController)

        /*val metrics2 = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics2)
        val params2 = videoView.layoutParams as RelativeLayout.LayoutParams
        params2.width = (300 * metrics2.density).toInt()
        params2.height = (250 * metrics2.density).toInt()
        params2.leftMargin = 30
        videoView.layoutParams = params2*/

        videoView.requestFocus()

        videoView.seekTo(1)

        videoView.setOnPreparedListener {
            videoView.seekTo(1)
        }

        val filePath = "/storage/emulated/0/Movies/VID_20200127_120401.mp4"

        Glide.with(this).load("https://firebasestorage.googleapis.com/v0/b/messageapp-9e68b.appspot.com/o/Attachments%2Fvideo%2F125a14e1-da30-400f-8de6-f8fd13ada677?alt=media&token=b0ddbb56-6366-4508-953a-7f56fa7174ae").error(R.drawable.add_ico).into(img_general_info)
        /*videoView.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.resume()
            } else {
                videoView.start()
            }
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            val params = videoView.layoutParams as RelativeLayout.LayoutParams
            params.width = metrics.widthPixels
            params.height = metrics.heightPixels
            params.leftMargin = 0
            videoView.layoutParams = params
            videoView.start()
        }*/

        img_general_back.setOnClickListener {
            onBackPressed()
        }
    }
}
