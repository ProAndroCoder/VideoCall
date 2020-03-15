package com.qatasoft.videocall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.MediaController
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.qatasoft.videocall.MainActivity.Companion.keyViewActivityType
import com.qatasoft.videocall.MainActivity.Companion.keyViewActivityUri
import com.qatasoft.videocall.data.db.entities.Tools
import kotlinx.android.synthetic.main.activity_view.*

class ViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        val uri = intent.getStringExtra(keyViewActivityUri)!!.toUri()
        val type = intent.getStringExtra(keyViewActivityType)

        Log.d("ChatLogActivityLogs", "URI : $uri  $type")

        //videoview_view.setVideoPath("/storage/emulated/0/Movies/VID_20200122_113026.mp4")

        when (type) {
            Tools.Video -> {
                videoview_view.setVideoURI(uri)
                val mediaController = MediaController(this)

                videoview_view.setMediaController(mediaController)

                videoview_view.requestFocus()

                videoview_view.setOnPreparedListener {
                    videoview_view.seekTo(1)
                }

                videoview_view.start()
            }
            Tools.Image -> {
                videoview_view.visibility = View.GONE
                img_view.visibility = View.VISIBLE
                Glide.with(this).load(uri).into(img_view)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
