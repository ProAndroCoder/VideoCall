package com.qatasoft.videocall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.RelativeLayout
import com.qatasoft.videocall.models.GeneralInfo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_general_info.*

class GeneralInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_info)

        val generalInfo = intent.getParcelableExtra<GeneralInfo>("GeneralInfo")

        if (generalInfo != null) {
            Picasso.get().load(generalInfo.img_general_url).into(img_general_info)

            title_general.text = generalInfo.title
            text_general.text = generalInfo.text
        }

        videoView.setVideoPath("/storage/emulated/0/Movies/VID_20200122_113026.mp4")
        var mediaController = MediaController(this)

        videoView.setMediaController(mediaController)

        val metrics2 = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics2)
        val params2 = videoView.layoutParams as RelativeLayout.LayoutParams
        params2.width = (300 * metrics2.density).toInt()
        params2.height = (250 * metrics2.density).toInt()
        params2.leftMargin = 30
        videoView.layoutParams = params2

        videoView.requestFocus()

        videoView.seekTo(2)

        videoView.setOnClickListener {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            val params = videoView.layoutParams as RelativeLayout.LayoutParams
            params.width = metrics.widthPixels
            params.height = metrics.heightPixels
            params.leftMargin = 0
            videoView.layoutParams = params
            videoView.start()
        }

        img_general_back.setOnClickListener {
            onBackPressed()
        }
    }
}
