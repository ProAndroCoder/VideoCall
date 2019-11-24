package com.qatasoft.videocall.bottomFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.MainActivity.Companion.mUser
import com.qatasoft.videocall.MyPreference
import com.qatasoft.videocall.R
import com.qatasoft.videocall.models.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {
    private val logTAG = "ProfileFragmentLog"
    private var isEnable = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(logTAG, "email nnn : " + mUser.email)

        getInfo()

        val isOwner = arguments!!.getBoolean(MainActivity.OwnerInfo)
        Log.d(logTAG, " Deneme : $isOwner")
        if (isOwner) {
            profile_btn_edit.setOnClickListener {
                enableEdit()
            }

            profile_btn_send_cancel.setOnClickListener {
                disableEdit()
                getInfo()
            }

            profile_btn_follow_save.setOnClickListener {
                val username = profile_et_username.text.toString()
                val email = profile_et_email.text.toString()
                val mobile = profile_et_mobile.text.toString()
                val about = profile_et_about.text.toString()

                val myPreference = MyPreference(context)
                mUser = User(mUser.profileImageUrl, mUser.uid, username, mUser.token, about, mobile, email, mUser.isFollowed)

                myPreference.setUserInfo(mUser)

                mUser = myPreference.getUserInfo()
                Log.d(logTAG, "email : " + myPreference.getUserInfo().email)
                disableEdit()
                getInfo()
            }
        } else {
            profile_img_back.visibility = View.VISIBLE

            profile_btn_follow_save.visibility = View.VISIBLE
            profile_btn_send_cancel.visibility = View.VISIBLE
            profile_btn_edit.visibility = View.GONE
        }
    }

    private fun getInfo() {
        if (mUser.profileImageUrl != "") {
            Picasso.get().load(mUser.profileImageUrl).into(circleimg_profile)
        }

        profile_username.text = mUser.username
        profile_et_username.setText(mUser.username)
        profile_et_email.setText(mUser.email)
        profile_et_mobile.setText(mUser.mobile)
        profile_et_about.setText(mUser.about)
    }

    //Enabling Edit
    private fun enableEdit() {
        profile_btn_follow_save.visibility = View.VISIBLE
        profile_btn_send_cancel.visibility = View.VISIBLE
        profile_btn_edit.visibility = View.GONE

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
}
