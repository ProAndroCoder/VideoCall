package com.qatasoft.videocall.registerlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
        val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        direct_login()

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

        if(!(email.isEmpty() || password.isEmpty())){
            Log.d(TAG, "email : $email")
            Log.d(TAG, "password : $password")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (!it.isSuccessful) return@addOnCompleteListener

                        val loginInfo=LoginInfo(email,password)

                        val mPreference=MyPreference(this)
                        mPreference.setLoginInfo(loginInfo)

                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    .addOnFailureListener {
                        Log.d("LoginActivity", "There is An Error While LoginActivity : ${it.message}")
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    }
        }


    }

    //Önceden giriş yapılmış ise ve çıkış yapılmamış ise o bilgilerle giriş yapar.
    private fun direct_login() {
        val myPreference = MyPreference(this)
        if (myPreference.isLoggedIn()) {
            val loginInfo:LoginInfo=myPreference.getLoginInfo()
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
}
