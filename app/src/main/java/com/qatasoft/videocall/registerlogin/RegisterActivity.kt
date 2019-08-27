package com.qatasoft.videocall.registerlogin

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
import com.qatasoft.videocall.messages.LatestMessagesActivity
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_register.setOnClickListener {
            perform_register()
        }

        txt_signin.setOnClickListener {
            Log.d("RegisterActivity", "Go to LoginActivity Activity")
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btn_selectPhoto.setOnClickListener {
            Log.d("RegisterActivity", "Try to show Select Photo")

            //Resim Seçiciyi açma
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
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

            imageview_selectPhoto.setImageBitmap(bitmap)

            btn_selectPhoto.alpha = 0f
        }
    }

    private fun perform_register() {
        val username = et_Username.text.toString()
        val email = et_Email.text.toString()
        val password = et_Password.text.toString()

        Log.d("RegisterActivity", "Username : $username")
        Log.d("RegisterActivity", "Email : $email")
        Log.d("RegisterActivity", "Password : $password")

        if (email.isEmpty() || password.isEmpty()) {
            Log.d("RegisterActivity", "Email Or Password is Empty")
            Toast.makeText(this, "Email Or Password Cant Be Empty", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("RegisterActivity", "User is Created UID : ${it.result?.user?.uid}")

                UploadProfileImage()
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to Create User : ${it.message}")
                Toast.makeText(this,it.message,Toast.LENGTH_LONG).show()
            }
    }

    //Firebase'e resim upload eden metod
    private fun UploadProfileImage() {
        if (selectedPhotoUri == null) {
            Log.d("RegisterActivity", "Photo URI is Null")
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference().child("UserProfileImage/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Image Successfully Uploaded : ${it.metadata?.path}")

                //Dosyanın konumunu getirir
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "File Location : $it")

                    saveUserToDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "There is An Error While Uploading Image : ${it.message}")
            }
    }

    private fun saveUserToDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(profileImageUrl, uid, et_Username.text.toString())

        ref.setValue(user).addOnSuccessListener {
            Log.d("RegisterActivity", "User is Saved To Firebase Database")

            val intent = Intent(this, LatestMessagesActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
