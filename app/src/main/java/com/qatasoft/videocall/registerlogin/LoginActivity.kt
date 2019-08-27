package com.qatasoft.videocall.registerlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.qatasoft.videocall.R

import com.google.firebase.auth.FirebaseAuth
import com.qatasoft.videocall.messages.LatestMessagesActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

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

                    startActivity(Intent(this, LatestMessagesActivity::class.java))
                }
                .addOnFailureListener {
                    Log.d("LoginActivity", "There is An Error While LoginActivity : ${it.message}")
                }
    }

    private fun direct_login() {
        FirebaseAuth.getInstance().signInWithEmailAndPassword("moro@gmail.com", "123456")
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    startActivity(Intent(this, LatestMessagesActivity::class.java))
                }
                .addOnFailureListener {
                    Log.d("LoginActivity", "There is An Error While LoginActivity : ${it.message}")
                }
    }
}
