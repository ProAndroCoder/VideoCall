package com.qatasoft.videocall

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.qatasoft.videocall.bottomFragments.*
import com.qatasoft.videocall.models.GeneralInfo
import com.qatasoft.videocall.models.LoginInfo
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.registerlogin.LoginActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val manager = supportFragmentManager
    val logTAG = "MainActivity"

    companion object {
        const val IsOwnerInfo = "IsOwnerInfo"
        const val OwnerInfo = "OwnerInfo"
        var mUser = User()
        var nav: BottomNavigationView? = null
        var isVisible = true
        var isBackPressedToExit = false
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                openHomeFragment()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_users -> {
                openUsersFragment()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_messages -> {
                openMessagesFragment()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                openSettingsFragment()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                openProfileFragment()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView = nav_view
        nav = navView

        openHomeFragment()

        fetchUserInfo()

        startService()

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    private fun openHomeFragment() {
        val transaction = manager.beginTransaction()
        val fragment = HomeFragment()
        transaction.replace(R.id.fragmentHolder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun openUsersFragment() {
        val transaction = manager.beginTransaction()
        val fragment = UsersFragment()
        transaction.replace(R.id.fragmentHolder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun openMessagesFragment() {
        val transaction = manager.beginTransaction()
        val fragment = MessagesFragment()
        transaction.replace(R.id.fragmentHolder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun openProfileFragment() {
        val transaction = manager.beginTransaction()
        val fragment = ProfileFragment()
        val args = Bundle()
        args.putBoolean(IsOwnerInfo, true)
        fragment.arguments = args
        transaction.replace(R.id.fragmentHolder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun openSettingsFragment() {
        val transaction = manager.beginTransaction()
        val fragment = SettingsFragment()
        transaction.replace(R.id.fragmentHolder, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun startService() {
        startService(Intent(this, BackgroundService::class.java))
    }

    private fun stopService() {
        stopService(Intent(this, BackgroundService::class.java))
    }

    private fun fetchUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach() {
                    Log.d(logTAG, "User Info : $it")
                    val user = it.getValue(User::class.java)

                    if (user != null && user.uid == FirebaseAuth.getInstance().uid) {
                        val myPreference = MyPreference(applicationContext)
                        myPreference.setUserInfo(user)
                    }
                }

                controlSession()
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(logTAG, "There is an error while fetching user datas.")
            }
        })
    }

    private fun controlSession() {
        val myPreference = MyPreference(this)
        mUser = myPreference.getUserInfo()

        if (mUser.uid.isEmpty()) {
            //Arka planda uygulama kapansa bile çalışan Servisleri kapatır.
            stopService(Intent(this, BackgroundService::class.java))

            //Shared Preference ile telefonda bulunan kullanıcı bilgilerini silme işlemi. Uid boş gönderilirse çıkış yapar.
            myPreference.setLoginInfo(LoginInfo("", ""))
            myPreference.setUserInfo(User("", "", "", "", "", "", "", false))

            // Firebase ile kullanıcının çıkışını sağlamak ve onu LoginActivity'e yollama işi
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        //If double clicked then exit app - Sending to HomeActivity to Exit App
        if (isBackPressedToExit) {
            Toast.makeText(this, "Exiting", Toast.LENGTH_LONG).show()
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("Exit me", true)
            startActivity(intent)
            finish()

        } else {// If back button clicked one time then fetch to navigation_home
            nav!!.selectedItemId = R.id.navigation_home
            nav!!.visibility = View.VISIBLE

            isBackPressedToExit = true
            Toast.makeText(this, "Press Back Again To Exit App", Toast.LENGTH_LONG).show()

            Handler().postDelayed({ isBackPressedToExit = false }, 2000)
        }

    }

    fun settingsOnClick(view: View) {
        when (view.id) {
            R.id.card_about -> {
                val generalInfo = GeneralInfo("https://d2v9y0dukr6mq2.cloudfront.net/video/thumbnail/V1xq1AADx/videoblocks-group-of-people-connecting-via-smart-phones-close-up-of-hands-using-cell-phones-and-text-messaging-4k-20s-30s_swzcjc3hwz_thumbnail-full01.png", "About Tab Info", "This is general About Tab Info")
                val intent = Intent(this, GeneralInfoActivity::class.java)
                intent.putExtra("GeneralInfo", generalInfo)
                startActivity(intent)
            }

            R.id.card_statistics -> {
                val generalInfo = GeneralInfo("https://d2v9y0dukr6mq2.cloudfront.net/video/thumbnail/V1xq1AADx/videoblocks-group-of-people-connecting-via-smart-phones-close-up-of-hands-using-cell-phones-and-text-messaging-4k-20s-30s_swzcjc3hwz_thumbnail-full01.png", "Statistic Tab Info", "This is general Statistic Tab Info")
                val intent = Intent(this, GeneralInfoActivity::class.java)
                intent.putExtra("GeneralInfo", generalInfo)
                startActivity(intent)
            }

            R.id.card_announcements -> {
                val generalInfo = GeneralInfo("https://d2v9y0dukr6mq2.cloudfront.net/video/thumbnail/V1xq1AADx/videoblocks-group-of-people-connecting-via-smart-phones-close-up-of-hands-using-cell-phones-and-text-messaging-4k-20s-30s_swzcjc3hwz_thumbnail-full01.png", "Announcements Tab Info", "This is general Announcements Tab Info")
                val intent = Intent(this, GeneralInfoActivity::class.java)
                intent.putExtra("GeneralInfo", generalInfo)
                startActivity(intent)
            }

            R.id.card_help -> {
                val generalInfo = GeneralInfo("https://d2v9y0dukr6mq2.cloudfront.net/video/thumbnail/V1xq1AADx/videoblocks-group-of-people-connecting-via-smart-phones-close-up-of-hands-using-cell-phones-and-text-messaging-4k-20s-30s_swzcjc3hwz_thumbnail-full01.png", "Help Tab Info", "This is general Help Tab Info")
                val intent = Intent(this, GeneralInfoActivity::class.java)
                intent.putExtra("GeneralInfo", generalInfo)
                startActivity(intent)
            }

            R.id.card_rules -> {
                val generalInfo = GeneralInfo("https://d2v9y0dukr6mq2.cloudfront.net/video/thumbnail/V1xq1AADx/videoblocks-group-of-people-connecting-via-smart-phones-close-up-of-hands-using-cell-phones-and-text-messaging-4k-20s-30s_swzcjc3hwz_thumbnail-full01.png", "Rules Tab Info", "This is general Rules Tab Info")
                val intent = Intent(this, GeneralInfoActivity::class.java)
                intent.putExtra("GeneralInfo", generalInfo)
                startActivity(intent)
            }

            R.id.card_privacy -> {
                val generalInfo = GeneralInfo("https://d2v9y0dukr6mq2.cloudfront.net/video/thumbnail/V1xq1AADx/videoblocks-group-of-people-connecting-via-smart-phones-close-up-of-hands-using-cell-phones-and-text-messaging-4k-20s-30s_swzcjc3hwz_thumbnail-full01.png", "Privacy Tab Info", "This is general Privacy Tab Info")
                val intent = Intent(this, GeneralInfoActivity::class.java)
                intent.putExtra("GeneralInfo", generalInfo)
                startActivity(intent)
            }

            R.id.card_signout -> {
                stopService()
                //Shared Preference ile telefonda bulunan kullanıcı bilgilerini silme işlemi. Uid boş gönderilirse çıkış yapar.
                val myPreference = MyPreference(this)

                myPreference.setLoginInfo(LoginInfo("", ""))
                myPreference.setUserInfo(User("", "", "", "", "", "", "", false))

                // Firebase ile kullanıcının çıkışını sağlamak ve onu LoginActivity'e yollama işi
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }
}