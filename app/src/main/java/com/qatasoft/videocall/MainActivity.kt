package com.qatasoft.videocall

import android.content.Intent
import android.os.Bundle
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
import com.qatasoft.videocall.Fragments.*
import com.qatasoft.videocall.models.LoginInfo
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.registerlogin.LoginActivity
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_settings.*

class MainActivity : AppCompatActivity() {
    private val manager = supportFragmentManager
    val logTAG = "MainActivity"

    companion object {
        const val OwnerInfo = "IsOwnerInfo"
        var mUser = User("", "", "", "", "","", "", false)
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
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

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
        args.putBoolean(OwnerInfo, true)
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

    fun stopService() {
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
            myPreference.setUserInfo(User("", "", "", "", "", "","", false))

            // Firebase ile kullanıcının çıkışını sağlamak ve onu LoginActivity'e yollama işi
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    fun profileOnClick(view: View) {
        when (view.id) {
            R.id.not_owner -> {
                val transaction = manager.beginTransaction()
                val fragment = ProfileFragment()
                val args = Bundle()
                args.putBoolean(OwnerInfo, false)
                fragment.arguments = args
                transaction.replace(R.id.fragmentHolder, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
    }
}
