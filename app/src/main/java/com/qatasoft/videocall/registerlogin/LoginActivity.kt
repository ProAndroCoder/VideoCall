package com.qatasoft.videocall.registerlogin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.qatasoft.videocall.R
import com.google.firebase.auth.FirebaseAuth
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.MyPreference
import com.qatasoft.videocall.models.LoginInfo
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    companion object {
        const val logTAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        setContentView(R.layout.activity_login)

        directLogin()
    }

    private fun performLogin() {
        val email = login_etEmail.text.toString()
        val password = login_etPassword.text.toString()

        if (!(email.isEmpty() || password.isEmpty())) {
            Log.d(logTAG, "email : $email")
            Log.d(logTAG, "password : $password")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) return@addOnCompleteListener

                        val loginInfo = LoginInfo(email, password)

                        val mPreference = MyPreference(this)
                        mPreference.setLoginInfo(loginInfo)

                        startActivity(Intent(this, MainActivity::class.java))
                        overridePendingTransition(R.anim.slide_in_right, R.anim.stay)

                    }
                    .addOnFailureListener {
                        Log.d("LoginActivity", "There is An Error While LoginActivity : ${it.message}")
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    }
        }
    }

    //Önceden giriş yapılmış ise ve çıkış yapılmamış ise o bilgilerle giriş yapar.
    private fun directLogin() {
        val myPreference = MyPreference(this)
        val loginInfo: LoginInfo = myPreference.getLoginInfo()
        if (myPreference.isLoggedIn() && !loginInfo.email.equals("")) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(loginInfo.email, loginInfo.password)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) return@addOnCompleteListener

                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    .addOnFailureListener {
                        Log.d("LoginActivity", "There is An Error While LoginActivity : ${it.message}")
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event!!.keyCode == KeyEvent.KEYCODE_SPACE && event.action == KeyEvent.ACTION_UP) {
            Toast.makeText(this, "SPACE PRESSED", Toast.LENGTH_LONG).show()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    fun onLoginClick(view: View) {
        when (view.id) {
            R.id.cirLoginButton -> {
                cirLoginButton.startAnimation()

                performLogin()

                cirLoginButton.stopAnimation()
            }

            R.id.login_registerButton -> {
                startActivity(Intent(this, RegisterActivity::class.java))
                overridePendingTransition(R.anim.stay, R.anim.stay)
            }

            R.id.login_addButton -> {
                startActivity(Intent(this, RegisterActivity::class.java))
                overridePendingTransition(R.anim.left_out, R.anim.stay)
            }

            R.id.login_forgotButton -> {
                Toast.makeText(applicationContext, "Switch to ForgotActivity", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
