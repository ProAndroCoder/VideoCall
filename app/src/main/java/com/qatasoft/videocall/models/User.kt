package com.qatasoft.videocall.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


//Bu classın nesnesinin diğer activitylere gönderilebilmesi için Parcelable olması gerekiyor. Bunu da kalıtım ve de eklentiyle yapıyoruz.Eklenti gradle app kısmında androidExtensions tagıyla yazılan fonksiyondur
@Parcelize
class User(val profileImageUrl: String, val uid: String, val username: String, val token: String, val about: String, val mobile: String, val email: String, var isFollowed: Boolean) : Parcelable {
    constructor() : this("", "", "", "", "", "","", false)
}