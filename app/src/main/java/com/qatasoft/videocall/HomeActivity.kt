package com.qatasoft.videocall

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.denzcoskun.imageslider.models.SlideModel
import com.qatasoft.videocall.models.GeneralInfo
import com.qatasoft.videocall.registerlogin.LoginActivity
import com.qatasoft.videocall.registerlogin.RegisterActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_general_info.*
import kotlinx.android.synthetic.main.fragment_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home)

        if( intent.getBooleanExtra("Exit me", false)){
            finish();
            return; // add this to prevent from doing unnecessary stuffs
        }

        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel("https://1.bp.blogspot.com/-GUZsgr8my50/XJUWOhyHyaI/AAAAAAAABUo/bljp3LCS3SUtj-judzlntiETt7G294WcgCLcBGAs/s1600/fox.jpg", "Foxes live wild in the city.", true))
        imageList.add(SlideModel("https://2.bp.blogspot.com/-CyLH9NnPoAo/XJUWK2UHiMI/AAAAAAAABUk/D8XMUIGhDbwEhC29dQb-7gfYb16GysaQgCLcBGAs/s1600/tiger.jpg"))
        imageList.add(SlideModel("https://3.bp.blogspot.com/-uJtCbNrBzEc/XJUWQPOSrfI/AAAAAAAABUs/ZlReSwpfI3Ack60629Rv0N8hSrPFHb3TACLcBGAs/s1600/elephant.jpg", "The population of elephants is decreasing in the world."))
        image_slider.setImageList(imageList)

        onClickProcesses()
    }

    private fun onClickProcesses() {
        home_messaging.setOnClickListener {
            val generalInfo = GeneralInfo("https://d2v9y0dukr6mq2.cloudfront.net/video/thumbnail/V1xq1AADx/videoblocks-group-of-people-connecting-via-smart-phones-close-up-of-hands-using-cell-phones-and-text-messaging-4k-20s-30s_swzcjc3hwz_thumbnail-full01.png", "Messaging Tab Info", "This is general Messaging Tab Info")
            val intent = Intent(this, GeneralInfoActivity::class.java)
            intent.putExtra("GeneralInfo", generalInfo)
            startActivity(intent)
        }

        home_video_calling.setOnClickListener {
            val generalInfo = GeneralInfo("https://cdn.apk-cloud.com/detail/screenshot/CMARHcruMyfgTW8wqHFl-lHFsbNMUqoQQs0Ri3jlftgHtQVXZ8DafSY27ZJibJx1PQ=h900.png", "Video Calling Tab Info", "This is general Video Calling Tab Info")
            val intent = Intent(this, GeneralInfoActivity::class.java)
            intent.putExtra("GeneralInfo", generalInfo)
            startActivity(intent)
        }

        home_find_friends.setOnClickListener {
            val generalInfo = GeneralInfo("https://d2v9y0dukr6mq2.cloudfront.net/video/thumbnail/tHF1OBF/4k-mature-friends-working-out-in-park-smartphone-with-fitness-app-in-foreground_hpg-8jpqkx_thumbnail-full01.png", "Find Friends Tab Info", "This is general Find Friends Tab Info")
            val intent = Intent(this, GeneralInfoActivity::class.java)
            intent.putExtra("GeneralInfo", generalInfo)
            startActivity(intent)
        }

        home_login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        home_register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}