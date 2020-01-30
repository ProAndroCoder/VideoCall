package com.qatasoft.videocall.models

import android.content.Context
import android.os.Parcelable
import android.widget.Toast
import kotlinx.android.parcel.Parcelize
import java.io.File

class ChatMessage(val text: String, val fromId: String, val toId: String, val sendingTime: String, var attachmentUrl: String = "", val attachmentName: String = "", val attachmentType: String = "", var fileUri: String = "", var refKey: String = "") {
    constructor() : this("", "", "", "")
}

class LoginInfo(val email: String, val password: String) {
    constructor() : this("", "")
}

data class Token(
        val token: String
)

//Bu classın nesnesinin diğer activitylere gönderilebilmesi için Parcelable olması gerekiyor. Bunu da kalıtım ve de eklentiyle yapıyoruz.Eklenti gradle app kısmında androidExtensions tagıyla yazılan fonksiyondur
@Parcelize
class User(val profileImageUrl: String, val uid: String, val username: String, val token: String, val about: String, val mobile: String, val email: String, var isFollowed: Boolean) : Parcelable {
    constructor() : this("", "", "", "", "", "", "", false)
}

@Parcelize
class GeneralInfo(val img_general_url: String, val title: String, val text: String) : Parcelable {
    constructor() : this("", "", "")
}

class Tools {
    companion object {
        const val image = "Image"
        const val video = "Video"
        const val document = "Document"
        const val audio = "Audio"

        //Values for MessagesFragment
        const val messageType = "latest-messages"
        const val callType = "latest-calls"

        //Values for UsersFragment
        const val userAll = "USER_TYPE_ALL"
        const val userFollower = "USER_TYPE_FOLLOWER"
        const val userFollowed = "USER_TYPE_FOLLOWED"
        const val addFollowed = "ADD_FOLLOWEDS"
        const val removeFollowed = "REMOVE_FOLLOWEDS"

        //ProjectName for using Path

        fun getPath(pathType: String): String = "/VideoCall/$pathType/"

        fun getExternalPath(context: Context): String = context.getExternalFilesDir(null).toString()

        fun getAbsolutePath(context: Context, pathType: String): String = getExternalPath(context) + getPath(pathType)

        //Creates Main Directories If Not Exists
        fun createDirectories(context: Context) {
            val directories = ArrayList<File>()

            val externalPath = getExternalPath(context)

            directories.add(File(externalPath, getPath(image) + "Sent"))
            directories.add(File(externalPath, getPath(video) + "Sent"))
            directories.add(File(externalPath, getPath(document) + "Sent"))
            directories.add(File(externalPath, getPath(audio) + "Sent"))

            directories.forEach { file ->
                try {
                    if (!file.exists()) {
                        file.mkdirs()
                    }

                } catch (e: Exception) {
                    Toast.makeText(context, "Error : $e", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}