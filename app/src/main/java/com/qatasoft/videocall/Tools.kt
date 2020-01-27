package com.qatasoft.videocall

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide

class Tools {
    fun fetchImage(context: Context, imgSource: String, imageView: ImageView) {
        Glide.with(context).load(imgSource).into(imageView)
    }
}