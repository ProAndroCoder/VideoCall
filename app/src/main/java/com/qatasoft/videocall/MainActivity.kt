package com.qatasoft.videocall

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.qatasoft.videocall.Fragments.FriendsFragment
import com.qatasoft.videocall.Fragments.HomeFragment
import com.qatasoft.videocall.Fragments.NewMessageFragment
import com.qatasoft.videocall.Fragments.SearchFragment
import com.qatasoft.videocall.models.LoginInfo
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.registerlogin.LoginActivity

class MainActivity : AppCompatActivity() {
    val manager=supportFragmentManager
    val TAG="MainActivity"

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {

                openHomeFragment()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {

                openSearchFragment()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_new_messages -> {

                openNewMessageFragment()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_friends -> {

                openFriendsFragment()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_signout ->{
                //Arka planda uygulama kapansa bile çalışan Servisleri kapatır.
                stopService()
                //Shared Preference ile telefonda bulunan kullanıcı bilgilerini silme işlemi. Uid boş gönderilirse çıkış yapar.
                val myPreference=MyPreference(this)

                myPreference.setLoginInfo(LoginInfo("",""))
                myPreference.setUserInfo(User("","","",""))

                // Firebase ile kullanıcının çıkışını sağlamak ve onu LoginActivity'e yollama işi
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
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

    fun openHomeFragment(){
        val transaction=manager.beginTransaction()
        val fragment=HomeFragment()
        transaction.replace(R.id.fragmentHolder,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun openSearchFragment(){
        val transaction=manager.beginTransaction()
        val fragment= SearchFragment()
        transaction.replace(R.id.fragmentHolder,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun openFriendsFragment(){
        val transaction=manager.beginTransaction()
        val fragment=FriendsFragment()
        transaction.replace(R.id.fragmentHolder,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun openNewMessageFragment(){
        val transaction=manager.beginTransaction()
        val fragment=NewMessageFragment()
        transaction.replace(R.id.fragmentHolder,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun startService(){
        startService(Intent(this,BackgroundService::class.java))
    }

    fun stopService(){
        stopService(Intent(this,BackgroundService::class.java))
    }

    fun fetchUserInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach() {
                    Log.d(TAG, "User Info : ${it.toString()}")
                    val user = it.getValue(User::class.java)

                    if (user != null && user.uid == FirebaseAuth.getInstance().uid) {
                        val myPreference=MyPreference(applicationContext)
                        myPreference.setUserInfo(user)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "There is an error while fetching user datas.")
            }
        })
    }
}
