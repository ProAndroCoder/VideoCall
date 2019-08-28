package com.qatasoft.videocall

import android.content.Context
import com.qatasoft.videocall.models.User

class MyPreference(context: Context?){
    val PREFERENCE_NAME="PreferenceLoginInfo"
    val profileImg="profileImageUrl"
    val uid="uid"
    val username="username"

    val preference= context?.getSharedPreferences(PREFERENCE_NAME,Context.MODE_PRIVATE)

    fun getLoginInfo():User{
        val user=User(preference?.getString(profileImg,"").toString(), preference?.getString(uid,"").toString(), preference?.getString(username,"").toString())

        return user
    }

    fun setLoginInfo(user:User){
        val editor= preference?.edit()

        if (editor != null) {
            editor.putString(profileImg,user.profileImageUrl)
            editor.putString(uid,user.uid)
            editor.putString(username,user.username)

            editor.apply()
        }

    }
}