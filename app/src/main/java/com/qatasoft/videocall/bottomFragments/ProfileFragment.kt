package com.qatasoft.videocall.bottomFragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.MainActivity.Companion.mUser
import com.qatasoft.videocall.MyPreference
import com.qatasoft.videocall.R
import com.qatasoft.videocall.messages.ChatLogActivity
import com.qatasoft.videocall.models.User
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.circleimg_profile
import java.util.*

class ProfileFragment : Fragment() {
    private val logTAG = "ProfileFragmentLog"
    private var isEnable = false
    private var isOwner = false

    var newProfileUrl: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(logTAG, "email : " + mUser.email)

        isOwner = arguments!!.getBoolean(MainActivity.IsOwnerInfo)

        Log.d(logTAG, " Is Owner : $isOwner")

        if (isOwner) {
            getInfo(mUser)
            profile_btn_edit.setOnClickListener {
                enableEdit()
            }

            profile_btn_send_cancel.setOnClickListener {
                disableEdit()
                getInfo(mUser)
            }

            profile_btn_follow_save.setOnClickListener {
                val username = profile_et_username.text.toString()
                val email = profile_et_email.text.toString()
                val mobile = profile_et_mobile.text.toString()
                val about = profile_et_about.text.toString()

                val myPreference = MyPreference(context)
                mUser = User(newProfileUrl, mUser.uid, username, mUser.token, about, mobile, email, mUser.isFollowed)

                myPreference.setUserInfo(mUser)

                mUser = myPreference.getUserInfo()
                Log.d(logTAG, "email -: " + myPreference.getUserInfo().email)
                disableEdit()
                getInfo(mUser)
            }
        } else {
            val user = arguments!!.getParcelable<User>(MainActivity.OwnerInfo)
            if (user != null) {
                getInfo(user)
            }

            profile_img_back.visibility = View.VISIBLE

            profile_img_back.setOnClickListener {
                val nav = MainActivity.nav
                nav!!.selectedItemId = R.id.navigation_home
                nav.visibility = View.VISIBLE
            }

            profile_btn_follow_save.visibility = View.VISIBLE

            profile_btn_follow_save.setOnClickListener {
                if (user!!.isFollowed) {
                    removeFollowed(user)
                } else {
                    addToFollowed(user)
                }
            }
            profile_btn_send_cancel.visibility = View.VISIBLE

            profile_btn_send_cancel.setOnClickListener {
                val intent = Intent(activity, ChatLogActivity::class.java)
                intent.putExtra(MessagesFragment.USER_KEY, user)
                startActivity(intent)
            }
            profile_btn_edit.visibility = View.GONE
        }
    }

    private fun getInfo(userInfo: User) {
        if (userInfo.profileImageUrl != "") {
            Glide.with(this).load(userInfo.profileImageUrl).into(circleimg_profile)
        }

        //Changing DrawableLeft of Button
        if (userInfo.isFollowed) {
            profile_btn_follow_save.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.tick_ico, 0, 0, 0)
            profile_btn_follow_save.text = "Followed"
        } else {
            profile_btn_follow_save.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.add_ico, 0, 0, 0)
            profile_btn_follow_save.text = "Follow"
        }

        profile_username.text = userInfo.username
        profile_et_username.setText(userInfo.username)
        profile_et_email.setText(userInfo.email)
        profile_et_mobile.setText(userInfo.mobile)
        profile_et_about.setText(userInfo.about)
    }

    //Enabling Edit
    private fun enableEdit() {
        profile_btn_follow_save.visibility = View.VISIBLE
        profile_btn_send_cancel.visibility = View.VISIBLE
        profile_btn_edit.visibility = View.GONE

        img_profile_update.visibility = View.VISIBLE

        img_profile_update.setOnClickListener {
            //Change Profile Image
            //Resim Seçiciyi açma
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        profile_btn_follow_save.text = getString(R.string.profile_apply)
        profile_btn_send_cancel.text = getString(R.string.profile_cancel)

        profile_btn_follow_save.setPadding(30, 20, 30, 20)
        profile_btn_follow_save.setCompoundDrawables(null, null, null, null)

        profile_btn_send_cancel.setPadding(30, 20, 30, 20)
        profile_btn_send_cancel.setCompoundDrawables(null, null, null, null)

        setEtEnable(profile_et_username)
        setEtEnable(profile_et_email)
        setEtEnable(profile_et_mobile)
        setEtEnable(profile_et_about)

        isEnable = true
    }

    //Disabling Edit
    private fun disableEdit() {
        profile_btn_follow_save.visibility = View.GONE
        profile_btn_send_cancel.visibility = View.GONE
        profile_btn_edit.visibility = View.VISIBLE
        img_profile_update.visibility = View.GONE


        setEtDisable(profile_et_username)
        setEtDisable(profile_et_email)
        setEtDisable(profile_et_mobile)
        setEtDisable(profile_et_about)

        isEnable = false
    }

    //Enable edittext for editing and change background
    private fun setEtEnable(et: EditText) {
        et.isEnabled = true
        et.background = context?.let { ContextCompat.getDrawable(it, R.drawable.rounded_et_active) }
    }

    //Disable edittext for editing and change background
    private fun setEtDisable(et: EditText) {
        et.isEnabled = false
        et.background = context?.let { ContextCompat.getDrawable(it, R.drawable.rounded_et_deactive) }
    }

    private fun addToFollowed(userInfo: User) {
        val followed = FirebaseDatabase.getInstance().getReference("/friends/${mUser.uid}/followeds/${userInfo.uid}")
        followed.setValue(userInfo)
                .addOnSuccessListener {
                    profile_btn_follow_save.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.tick_ico, 0, 0, 0)
                    profile_btn_follow_save.text = "Followed"
                    userInfo.isFollowed = true
                }
                .addOnFailureListener {
                    Log.d("UsersFragment", "there is a problem : $it")
                }

        val follower = FirebaseDatabase.getInstance().getReference("/friends/${userInfo.uid}/followers/${mUser.uid}")
        follower.setValue(mUser)
                .addOnSuccessListener {
                }
                .addOnFailureListener {
                    Log.d("UsersFragment", "there is a problem : $it")
                }
    }

    private fun removeFollowed(userInfo: User) {
        val followed = FirebaseDatabase.getInstance().getReference("/friends/${mUser.uid}/followeds/${userInfo.uid}")
        followed.removeValue()
                .addOnSuccessListener {
                    profile_btn_follow_save.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.add_ico, 0, 0, 0)
                    profile_btn_follow_save.text = "Follow"
                    userInfo.isFollowed = false
                }
                .addOnFailureListener {
                    Log.d("UsersFragment", "there is a problem : $it")
                }

        val follower = FirebaseDatabase.getInstance().getReference("/friends/${userInfo.uid}/followers/${mUser.uid}")
        follower.removeValue()
                .addOnSuccessListener {

                }
                .addOnFailureListener {
                    Log.d("UsersFragment", "there is a problem : $it")
                }
    }

    //Firebase'e resim upload eden metod
    private fun uploadProfileImage() {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().reference.child("UserProfileImage/$filename")

        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener { img ->
                    Log.d(logTAG, "Image Successfully Uploaded : ${img.metadata?.path}")

                    Glide.with(context!!).load(selectedPhotoUri).into(circleimg_profile)

                    //Dosyanın konumunu getirir
                    ref.downloadUrl.addOnSuccessListener { imgUri ->
                        Log.d(logTAG, "File Location : $imgUri")
                        newProfileUrl = imgUri.toString()
                    }
                }
                .addOnFailureListener {

                    Log.d(logTAG, "There is An Error While Uploading Image : ${it.message}")
                }
    }

    private var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Select Photo kısmı bittiğinde buraya gelir ve eğer requestCode 0 ise data da boş değilse ve Başarılı bir şekilde bir resim seçildiyse bunları yap diyor
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterActivity", "Photo Selected")

            selectedPhotoUri = data.data

            if (selectedPhotoUri != null) {
                uploadProfileImage()
            }
        }
    }
}
