package com.qatasoft.videocall.registerlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.qatasoft.videocall.R
import kotlinx.android.synthetic.main.activity_forgot.*

class ForgotActivity : AppCompatActivity() {
    val logTAG = "ForgotActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)
    }

    fun performSendInfo() {
        val email = forgot_etEmail.text.toString()

        if (email.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.empty_email), Toast.LENGTH_SHORT).show()
            cirForgotButton.revertAnimation()
            return
        }

        if (!email.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))) {
            Toast.makeText(this, resources.getString(R.string.not_valid_email), Toast.LENGTH_SHORT).show()
            cirForgotButton.revertAnimation()
            return
        }

        //Write Main Codes To Send Reset Pass Info
        cirForgotButton.revertAnimation()
        Toast.makeText(this, "I Will Write This Code. Please Forgive Me :)", Toast.LENGTH_SHORT).show()

    }

    fun onForgotClick(view: View) {
        when (view.id) {
            R.id.cirForgotButton -> {
                cirForgotButton.startAnimation {
                    performSendInfo()
                }
            }

            R.id.forgot_loginButton -> {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.transition.slide_in_left, android.R.anim.slide_out_right)
            }

            R.id.forgot_backButton -> {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.transition.slide_in_left, android.R.anim.slide_out_right)
            }
        }
    }
}
