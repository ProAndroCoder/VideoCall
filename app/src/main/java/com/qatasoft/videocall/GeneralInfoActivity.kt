package com.qatasoft.videocall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        img_general_back.setOnClickListener {
            onBackPressed()
        }
    }
}
