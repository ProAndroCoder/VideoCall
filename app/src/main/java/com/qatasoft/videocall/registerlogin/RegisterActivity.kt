package com.qatasoft.videocall.registerlogin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.MyPreference
import com.qatasoft.videocall.R
import com.qatasoft.videocall.data.db.entities.LoginInfo
import com.qatasoft.videocall.data.db.entities.User
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*
import android.provider.MediaStore

class RegisterActivity : AppCompatActivity() {
    val logTAG = "RegisterActivityInfo"
    private var email: String = ""
    var username: String = ""
    var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        changeStatusBarColor()
        hideSystemUI()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Select Photo kısmı bittiğinde buraya gelir ve eğer requestCode 0 ise data da boş değilse ve Başarılı bir şekilde bir resim seçildiyse bunları yap diyor
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterActivity", "Photo Selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            circleimg_profile.setImageBitmap(bitmap)
        }
    }

    private fun performRegister() {
        username = register_etUsername.text.toString()
        email = register_etEmail.text.toString()
        password = register_etPassword.text.toString()
        val passwordAgain = register_etPasswordAgain.text.toString()

        Log.d(logTAG, "Username : $username")
        Log.d(logTAG, "Email : $email")
        Log.d(logTAG, "Password : $password")

        if (email.isEmpty() || password.isEmpty() || passwordAgain.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, resources.getString(R.string.empty_values), Toast.LENGTH_SHORT).show()
            cirRegisterButton.revertAnimation()
            return
        }

        if (!email.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))) {
            Toast.makeText(this, resources.getString(R.string.not_valid_email), Toast.LENGTH_SHORT).show()
            cirRegisterButton.revertAnimation()
            return
        }

        if (!password.equals(passwordAgain)) {
            Toast.makeText(this, resources.getString(R.string.not_same_passwords), Toast.LENGTH_SHORT).show()
            cirRegisterButton.revertAnimation()
            return
        }

        if (selectedPhotoUri == null) {
            Toast.makeText(this, resources.getString(R.string.error_profile_img), Toast.LENGTH_SHORT).show()
            cirRegisterButton.revertAnimation()
            return
        }

        //Create User Info To Signin
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    Log.d(logTAG, "User is Created UID : ${it.result?.user?.uid}")

                    uploadProfileImage()
                }
                .addOnFailureListener {
                    cirRegisterButton.revertAnimation()

                    Log.d(logTAG, "Failed to Create User : ${it.message}")
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    cirRegisterButton.revertAnimation()
                }
    }

    //Firebase'e resim upload eden metod
    private fun uploadProfileImage() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().reference.child("UserProfileImage/$filename")

        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener { img ->
                    Log.d(logTAG, "Image Successfully Uploaded : ${img.metadata?.path}")

                    //Dosyanın konumunu getirir
                    ref.downloadUrl.addOnSuccessListener { imgUri ->
                        Log.d(logTAG, "File Location : $imgUri")
                        saveUserToDatabase(imgUri.toString())
                    }
                }
                .addOnFailureListener {
                    cirRegisterButton.revertAnimation()
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()

                    Log.d(logTAG, "There is An Error While Uploading Image : ${it.message}")
                }
    }

    private fun saveUserToDatabase(profileImageUrl: String) {
        val loginInfo = LoginInfo(email, password)

        val mPreference = MyPreference(this)
        mPreference.setLoginInfo(loginInfo)

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(profileImageUrl, uid, register_etUsername.text.toString(), "", "", "", email, false)

        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d(logTAG, "User is Saved To Firebase Database")

                    cirRegisterButton.revertAnimation()

                    val intent = Intent(this, MainActivity::class.java)

                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    overridePendingTransition(R.transition.slide_in_left, android.R.anim.slide_out_right)
                }
                .addOnFailureListener {
                    cirRegisterButton.revertAnimation()
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
    }

    private fun hideSystemUI() {
        this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                or View.SYSTEM_UI_FLAG_IMMERSIVE)
    }

    fun onRegisterClick(view: View) {
        when (view.id) {
            R.id.circleimg_profile -> {
                //Resim Seçiciyi açma
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
            }

            R.id.cirRegisterButton -> {
                cirRegisterButton.startAnimation {
                    performRegister()
                }
            }

            R.id.register_loginButton -> {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.transition.slide_in_left, android.R.anim.slide_out_right)
            }

            R.id.register_backButton -> {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.transition.slide_in_left, android.R.anim.slide_out_right)
            }
        }
    }
}
