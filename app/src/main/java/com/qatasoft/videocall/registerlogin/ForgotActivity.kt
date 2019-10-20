package com.qatasoft.videocall.registerlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.qatasoft.videocall.R

class ForgotActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)
    }

    fun onForgotClick(view: View) {
        when (view.id) {
            R.id.cirForgotButton -> {
                Toast.makeText(this, "Sending Reset To API", Toast.LENGTH_SHORT).show()
            }

            R.id.forgot_loginButton -> {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right)
            }

            R.id.forgot_backButton -> {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right)
            }
        }
    }
}
