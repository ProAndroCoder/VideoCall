package com.qatasoft.videocall.models

import android.os.Parcelable

class LoginInfo(val email: String, val password: String){
    constructor() : this("", "")
}