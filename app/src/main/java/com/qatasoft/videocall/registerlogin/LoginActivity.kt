package com.qatasoft.videocall.registerlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.qatasoft.videocall.R

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.qatasoft.videocall.Fragments.HomeFragment
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.MyPreference
import com.qatasoft.videocall.models.User
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    companion object {
        val TAG = "LoginActivity"
        var myUser = User("", "", "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //direct_login()

        btn_login.setOnClickListener {
            perform_login()
        }

        txt_signup.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun perform_login() {
        val email = et_login_Email.text.toString()
        val password = et_login_Password.text.toString()

        Log.d("LoginActivity", "email : $email")
        Log.d("LoginActivity", "password : $password")

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener


                    startActivity(Intent(this, MainActivity::class.java))
                }
                .addOnFailureListener {
                    Log.d("LoginActivity", "There is An Error While LoginActivity : ${it.message}")
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
    }

    private fun direct_login() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword("moro@gmail.com", "123456")
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    val uid = FirebaseAuth.getInstance().uid
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            HomeFragment.currentUser = p0.getValue(User::class.java)

                            if (HomeFragment.currentUser != null) {
                                Log.d(HomeFragment.TAG, "Current User : ${HomeFragment.currentUser?.username}")
                                val myPreference = MyPreference(applicationContext)
                                myPreference.setLoginInfo(HomeFragment.currentUser!!)

                                startActivity(Intent(applicationContext, MainActivity::class.java))
                            }
                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })

                }
                .addOnFailureListener {
                    Log.d("LoginActivity", "There is An Error While LoginActivity : ${it.message}")
                }
    }
}
