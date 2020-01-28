package com.qatasoft.videocall.models

import android.content.Context
import android.os.Environment
import android.os.Parcelable
import android.util.Log
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

        fun getExternalDirectory(context: Context): String {
            return context.getExternalFilesDir(null).toString()
        }

        //Creates Main Directories If Not Exists
        fun createDirectories(context: Context) {
            val directories = ArrayList<File>()

            val externalPath = context.getExternalFilesDir(null)

            Toast.makeText(context, "Hello : $externalPath", Toast.LENGTH_LONG).show()

            directories.add(File(externalPath, "VideoCall/${audio}/Sent"))
            directories.add(File(externalPath, "VideoCall/${document}/Sent"))
            directories.add(File(externalPath, "VideoCall/${image}/Sent"))
            directories.add(File(externalPath, "VideoCall/${video}/Sent"))

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