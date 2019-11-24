package com.qatasoft.videocall.registerlogin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
        const val logTAG = "LoginActivityInfo"
    }

    var email: String = ""
    var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        setContentView(R.layout.activity_login)

        hideSystemUI()

        directLogin()
    }

    private fun hideSystemUI() {
        this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                or View.SYSTEM_UI_FLAG_IMMERSIVE)
    }

    private fun performLogin() {
        Log.d(logTAG, "email : $email")
        Log.d(logTAG, "password : $password")

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.empty_values), Toast.LENGTH_SHORT).show()
            cirLoginButton.revertAnimation()
            return
        }

        if (!email.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))) {
            Toast.makeText(this, resources.getString(R.string.not_valid_email), Toast.LENGTH_SHORT).show()
            cirLoginButton.revertAnimation()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    val loginInfo = LoginInfo(email, password)

                    val mPreference = MyPreference(this)

                    Log.d(logTAG, "email : " + email + " password : " + password + " 1")
                    mPreference.setLoginInfo(loginInfo)

                    cirLoginButton.revertAnimation()

                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(R.anim.right_in, R.anim.left_out)
                }
                .addOnFailureListener {
                    cirLoginButton.revertAnimation()

                    Log.d("LoginActivity", "There is An Error While LoginActivity : ${it.message}")
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
    }

    //Önceden giriş yapılmış ise ve çıkış yapılmamış ise o bilgilerle giriş yapar.
    private fun directLogin() {
        val loginInfo: LoginInfo = MyPreference(this).getLoginInfo()

        email = loginInfo.email
        password = loginInfo.password

        if (email != "" || password != "") {
            cirLoginButton.startAnimation {
                performLogin()
            }
        }
    }

    fun onLoginClick(view: View) {
        when (view.id) {
            R.id.cirLoginButton -> {
                email = login_etEmail.text.toString()
                password = login_etPassword.text.toString()

                cirLoginButton.startAnimation {
                    performLogin()
                }
            }

            R.id.login_registerButton -> {
                startActivity(Intent(this, RegisterActivity::class.java))
                overridePendingTransition(R.anim.right_in, R.anim.left_out)
            }

            R.id.login_addButton -> {
                startActivity(Intent(this, RegisterActivity::class.java))
                overridePendingTransition(R.anim.right_in, R.anim.left_out)
            }

            R.id.login_forgotButton -> {
                startActivity(Intent(this, ForgotActivity::class.java))
                overridePendingTransition(R.anim.right_in, R.anim.left_out)
            }
        }
    }
}