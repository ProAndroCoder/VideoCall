package com.qatasoft.videocall

import android.content.Context
import com.qatasoft.videocall.models.LoginInfo
import com.qatasoft.videocall.models.User

class MyPreference(context: Context?){
    val PREFERENCE_NAME="PreferenceLoginInfo"
    val profileImg="profileImageUrl"
    val uid="uid"
    val username="username"
    val email="email"
    val password="password"
    private val isLogged="isUserLoggedIn"

    val preference= context?.getSharedPreferences(PREFERENCE_NAME,Context.MODE_PRIVATE)

    //Login bilgisini gönderme. Email ve Pass gönderme işlemi.
    fun getLoginInfo(): LoginInfo {
        val loginInfo=LoginInfo(preference?.getString(email,"").toString(), preference?.getString(password,"").toString())

        return loginInfo
    }

    //Login bilgisini değiştirme. Eğer email boş gelirse isLogged false olur.
    fun setLoginInfo(loginInfo:LoginInfo){
        val editor= preference?.edit()

        if (editor != null) {
            editor.putString(email,loginInfo.email)
            editor.putString(password,loginInfo.password)

            if(loginInfo.email.equals("")){
                editor.putBoolean(isLogged,false)
            }
            else{
                editor.putBoolean(isLogged,true)
            }

            editor.apply()
        }
    }

    //Kullanıcı bilgilerini gönderme işlemi
    fun getUserInfo(): User {
        val user=User(preference?.getString(profileImg,"").toString(), preference?.getString(uid,"").toString(), preference?.getString(username,"").toString(),"","","")

        return user
    }

    //Kullanıcı bilgilerini değiştirme işlemi.
    fun setUserInfo(user:User){
        val editor= preference?.edit()

        if (editor != null) {
            editor.putString(profileImg,user.profileImageUrl)
            editor.putString(uid,user.uid)
            editor.putString(username,user.username)

            if(user.uid.equals("")){
                editor.putBoolean(isLogged,false)
            }
            else{
                editor.putBoolean(isLogged,true)
            }

            editor.apply()
        }
    }

    // Kullanıcı önceden giriş bilgileri var mı kontrol eder. isLooged boolean döndürür.
    fun isLoggedIn():Boolean{
        val isLogged= preference?.getBoolean(isLogged,false)

        return isLogged!!
    }
}