package com.qatasoft.videocall

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.qatasoft.videocall.Fragments.FriendsFragment
import com.qatasoft.videocall.Fragments.HomeFragment
import com.qatasoft.videocall.Fragments.NewMessageFragment
import com.qatasoft.videocall.Fragments.SearchFragment
import com.qatasoft.videocall.registerlogin.LoginActivity

class MainActivity : AppCompatActivity() {
    val manager=supportFragmentManager

    private lateinit var textMessage: TextView
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
}
