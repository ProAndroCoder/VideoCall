package com.qatasoft.videocall.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.R
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {
    private val logTag = "ProfileFragmentLog"
    private var isEnable = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profile_username.text = MainActivity.mUser.username

        if (arguments!!.getBoolean(MainActivity.OwnerInfo)) {
            profile_btn_edit.setOnClickListener {
                enableEdit()
            }

            profile_btn_send_cancel.setOnClickListener {
                disableEdit()
            }

            profile_btn_follow_save.setOnClickListener {
                disableEdit()
            }
        } else {
            profile_img_back.visibility = View.VISIBLE

            profile_btn_follow_save.visibility = View.VISIBLE
            profile_btn_send_cancel.visibility = View.VISIBLE
            profile_btn_edit.visibility = View.GONE
        }


        //Back Button Clicked
        profile_img_back.setOnClickListener { activity?.onBackPressed() }
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
