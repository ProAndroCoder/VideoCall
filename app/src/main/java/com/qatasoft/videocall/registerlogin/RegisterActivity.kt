package com.qatasoft.videocall.registerlogin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.qatasoft.videocall.R
import com.qatasoft.videocall.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.MyPreference
import com.qatasoft.videocall.models.LoginInfo
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*
import android.view.WindowManager
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat

class RegisterActivity : AppCompatActivity() {
    val logTAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        changeStatusBarColor()

//        btn_selectPhoto.setOnClickListener {
//            Log.d("RegisterActivity", "Try to show Select Photo")
//
//            //Resim Seçiciyi açma
//            val intent = Intent(Intent.ACTION_PICK)
//            intent.type = "image/*"
//            startActivityForResult(intent, 0)
//        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            //window.setStatusBarColor(Color.TRANSPARENT)
            window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.register_bk_color)
        }
    }

    var selectedPhotoUri: Uri? = null

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        //Select Photo kısmı bittiğinde buraya gelir ve eğer requestCode 0 ise data da boş değilse ve Başarılı bir şekilde bir resim seçildiyse bunları yap diyor
//        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
//            Log.d("RegisterActivity", "Photo Selected")
//
//            selectedPhotoUri = data.data
//
//            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
//
//            imageview_selectPhoto.setImageBitmap(bitmap)
//
//            btn_selectPhoto.alpha = 0f
//        }
//    }

    private fun performRegister() {
        val username = register_etUsername.text.toString()
        val email = register_etEmail.text.toString()
        val password = register_etPassword.text.toString()

        Log.d(logTAG, "Username : $username")
        Log.d(logTAG, "Email : $email")
        Log.d(logTAG, "Password : $password")

        if (email.isEmpty() || password.isEmpty()) {
            Log.d(logTAG, "Email Or Password is Empty")
            Toast.makeText(this, "Email Or Password Cant Be Empty", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    Log.d(logTAG, "User is Created UID : ${it.result?.user?.uid}")

                    //uploadProfileImage()

                    saveUserToDatabase("NULL")

                    val loginInfo = LoginInfo(email, password)

                    val mPreference = MyPreference(this)
                    mPreference.setLoginInfo(loginInfo)
                }
                .addOnFailureListener {
                    Log.d(logTAG, "Failed to Create User : ${it.message}")
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
    }

    //Firebase'e resim upload eden metod
    private fun uploadProfileImage() {
        if (selectedPhotoUri == null) {
            Log.d(logTAG, "Photo URI is Null")
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference().child("UserProfileImage/$filename")

        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d(logTAG, "Image Successfully Uploaded : ${it.metadata?.path}")

                    //Dosyanın konumunu getirir
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d(logTAG, "File Location : $it")

                        saveUserToDatabase(it.toString())
                    }
                }
                .addOnFailureListener {
                    Log.d(logTAG, "There is An Error While Uploading Image : ${it.message}")
                }
    }

    private fun saveUserToDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(profileImageUrl, uid, register_etUsername.text.toString(), "","","",false)

        ref.setValue(user).addOnSuccessListener {
            Log.d(logTAG, "User is Saved To Firebase Database")

            val intent = Intent(this, MainActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    fun onRegisterClick(view: View) {
        when (view.id) {
            R.id.cirRegisterButton -> {
                performRegister()
            }

            R.id.register_loginButton -> {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right)
            }

            R.id.register_backButton -> {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right)
            }
        }
    }
}
