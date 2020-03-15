package com.qatasoft.videocall

import android.content.Context
import com.qatasoft.videocall.data.db.entities.LoginInfo
import com.qatasoft.videocall.data.db.entities.User
import android.util.Log
import com.qatasoft.videocall.registerlogin.LoginActivity

class MyPreference(context: Context?) {
    private val preferenceKey = "PreferenceLoginInfo"
    private val profileImg = "profileImageUrl"
    val uid = "uid"
    val username = "username"
    private val token = "token"
    private val about = "about"
    private val mobile = "mobile"
    private val email = "email"
    private val isFollowed = "isFollowed"
    private val password = "password"
    private val isLogged = "isUserLoggedIn"

    private val preference = context!!.getSharedPreferences(preferenceKey, Context.MODE_PRIVATE)

    //Login bilgisini gönderme. Email ve Pass gönderme işlemi.
    fun getLoginInfo(): LoginInfo {
        val loginInfo = LoginInfo(preference.getString(email, "").toString(), preference.getString(password, "").toString())
        Log.d(LoginActivity.logTAG, "email : " + loginInfo.email + " password : " + loginInfo.password + " 3")
        return LoginInfo(preference.getString(email, "").toString(), preference.getString(password, "").toString())
    }

    //Login bilgisini değiştirme. Eğer email boş gelirse isLogged false olur.
    fun setLoginInfo(loginInfo: LoginInfo) {
        val editor = preference?.edit()

        Log.d(LoginActivity.logTAG, "email : " + loginInfo.email + " password : " + loginInfo.password + " 2")

        if (editor != null) {
            editor.putString(email, loginInfo.email)
            editor.putString(password, loginInfo.password)

            if (loginInfo.email == "") {
                editor.putBoolean(isLogged, false)
            } else {
                editor.putBoolean(isLogged, true)
            }

            Log.d(LoginActivity.logTAG, "email : " + loginInfo.email + " password : " + loginInfo.password + " 3")

            editor.apply()
        }
    }

    //Kullanıcı bilgilerini gönderme işlemi
    fun getUserInfo(): User {
        return User(preference?.getString(profileImg, "").toString(), preference?.getString(uid, "").toString(), preference?.getString(username, "").toString(), preference?.getString(token, "").toString(), preference?.getString(about, "").toString(), preference?.getString(mobile, "").toString(), preference?.getString(email, "").toString(), preference!!.getBoolean(isFollowed, false))
    }

    //Kullanıcı bilgilerini değiştirme işlemi.
    fun setUserInfo(user: User) {
        val editor = preference?.edit()

        if (editor != null) {
            editor.putString(profileImg, user.profileImageUrl)
            editor.putString(uid, user.uid)
            editor.putString(username, user.username)
            editor.putString(token, user.token)
            editor.putString(about, user.about)
            editor.putString(mobile, user.mobile)
            editor.putString(email, user.email)
            editor.putBoolean(isFollowed, user.isFollowed)

            if (user.uid == "") {
                editor.putBoolean(isLogged, false)
            } else {
                editor.putBoolean(isLogged, true)
            }

            editor.apply()
        }
    }

    // Kullanıcı önceden giriş bilgileri var mı kontrol eder. isLooged boolean döndürür.
    fun isLoggedIn(): Boolean {
        val isLogged = preference?.getBoolean(isLogged, false)

        return isLogged!!
    }
}