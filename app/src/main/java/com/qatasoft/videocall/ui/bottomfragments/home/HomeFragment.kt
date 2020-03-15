package com.qatasoft.videocall.ui.bottomfragments.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import com.denzcoskun.imageslider.models.SlideModel
import android.view.ViewGroup
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.R
import com.qatasoft.videocall.ui.bottomfragments.messages.MessagesFragment
import com.qatasoft.videocall.ui.bottomfragments.users.UsersFragment
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    companion object {
        const val logTAG = "MessagesFragment"
        const val USER_KEY = "USER_INFO_KEY"
        var isMessage = true
    }

    private val imageList = ArrayList<SlideModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageList.add(SlideModel("https://1.bp.blogspot.com/-GUZsgr8my50/XJUWOhyHyaI/AAAAAAAABUo/bljp3LCS3SUtj-judzlntiETt7G294WcgCLcBGAs/s1600/fox.jpg", "Foxes live wild in the city.", true))
        imageList.add(SlideModel("https://2.bp.blogspot.com/-CyLH9NnPoAo/XJUWK2UHiMI/AAAAAAAABUk/D8XMUIGhDbwEhC29dQb-7gfYb16GysaQgCLcBGAs/s1600/tiger.jpg"))
        imageList.add(SlideModel("https://3.bp.blogspot.com/-uJtCbNrBzEc/XJUWQPOSrfI/AAAAAAAABUs/ZlReSwpfI3Ack60629Rv0N8hSrPFHb3TACLcBGAs/s1600/elephant.jpg", "The population of elephants is decreasing in the world."))
        image_slider.setImageList(imageList)

        linear_login.visibility = View.GONE

        onClickProcesses()
    }

    private fun onClickProcesses() {
        home_messaging.setOnClickListener {
            isMessage = true
            val transaction = activity!!.supportFragmentManager.beginTransaction()
            val fragment = MessagesFragment()
            transaction.replace(R.id.fragmentHolder, fragment)
            transaction.addToBackStack(null)
            transaction.commit()

            MainActivity.nav!!.selectedItemId = R.id.navigation_messages
        }

        home_video_calling.setOnClickListener {
            isMessage = false
            val transaction = activity!!.supportFragmentManager.beginTransaction()
            val fragment = MessagesFragment()
            transaction.replace(R.id.fragmentHolder, fragment)
            transaction.addToBackStack(null)
            transaction.commit()

            MainActivity.nav!!.selectedItemId = R.id.navigation_messages
        }

        home_find_friends.setOnClickListener {
            val transaction = activity!!.supportFragmentManager.beginTransaction()
            val fragment = UsersFragment()
            transaction.replace(R.id.fragmentHolder, fragment)
            transaction.addToBackStack(null)
            transaction.commit()

            MainActivity.nav!!.selectedItemId = R.id.navigation_users
        }
    }
}