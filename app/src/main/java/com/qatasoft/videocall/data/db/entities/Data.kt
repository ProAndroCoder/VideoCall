package com.qatasoft.videocall.data.db.entities

import android.content.Context
import android.os.Parcelable
import android.widget.Toast
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "chat_items")
class ChatMessage(
        @ColumnInfo(name = "item_text")
        var text: String,
        @ColumnInfo(name = "item_from_id")
        val fromId: String,
        @ColumnInfo(name = "item_to_id")
        val toId: String,
        @ColumnInfo(name = "item_from_username")
        val fromUsername: String,
        @ColumnInfo(name = "item_to_username")
        val toUsername: String,
        @ColumnInfo(name = "item_sending_time")
        val sendingTime: String,
        @ColumnInfo(name = "item_attachment_url")
        var attachmentUrl: String = "",
        @ColumnInfo(name = "item_attachment_name")
        val attachmentName: String = "",
        @ColumnInfo(name = "item_attachment_type")
        val attachmentType: String = "",
        @ColumnInfo(name = "item_file_uri")
        var fileUri: String = "",
        @ColumnInfo(name = "item_ref_key")
        var refKey: String = ""
) {
    constructor() : this("", "", "", "", "", "", "", "", "", "", "")

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

class LoginInfo(val email: String, val password: String) {
    constructor() : this("", "")
}

data class Token(
        val token: String
)

//Bu classın nesnesinin diğer activitylere gönderilebilmesi için Parcelable olması gerekiyor. Bunu da kalıtım ve de eklentiyle yapıyoruz.Eklenti gradle app kısmında androidExtensions tagıyla yazılan fonksiyondur
@Parcelize
class User(var profileImageUrl: String, val uid: String, val username: String, val token: String = "", val about: String = "", val mobile: String = "", val email: String = "", var isFollowed: Boolean = false) : Parcelable {
    constructor() : this("", "", "", "", "", "", "", false)
}

@Parcelize
class GeneralInfo(val img_general_url: String, val title: String, val text: String) : Parcelable {
    constructor() : this("", "", "")
}

class Tools {
    companion object {
        const val Image = "Image"
        const val Video = "Video"
        const val Document = "Document"
        const val Audio = "Audio"

        const val image = "image"
        const val video = "video"
        const val document = "document"
        const val audio = "audio"

        //Values for MessagesFragment
        const val messageType = "latest-messages"
        const val callType = "latest-calls"

        //Values for UsersFragment
        const val userAll = "USER_TYPE_ALL"
        const val userFollower = "USER_TYPE_FOLLOWER"
        const val userFollowed = "USER_TYPE_FOLLOWED"
        const val addFollowed = "ADD_FOLLOWEDS"
        const val removeFollowed = "REMOVE_FOLLOWEDS"

        //Values for VideoRequests
        const val addRequest = "ADD_VIDEO_REQUEST"
        const val removeRequest = "REMOVE_VIDEO_REQUEST"
        const val addRequestLog = "ADD_VIDEO_REQUEST_LOG"
        const val removeRequestLog = "REMOVE_VIDEO_REQUEST_LOG"

        const val videoReqType = "videorequests"
        const val reqLogType = "latest-calls"

        //Date Information
        fun getSendingTime(): String = SimpleDateFormat("dd/M/yyyy hh:mm", Locale.getDefault()).format(Date())

        //ProjectName for using Path
        fun getPath(pathType: String): String = "/VideoCall/$pathType/"

        fun getExternalPath(context: Context): String = context.getExternalFilesDir(null).toString()

        fun getAbsolutePath(context: Context, pathType: String): String = getExternalPath(context) + getPath(pathType)

        //Creates Main Directories If Not Exists
        fun createDirectories(context: Context) {
            val directories = ArrayList<File>()

            val externalPath = getExternalPath(context)

            directories.add(File(externalPath, getPath(Image) + "Sent"))
            directories.add(File(externalPath, getPath(Video) + "Sent"))
            directories.add(File(externalPath, getPath(Document) + "Sent"))
            directories.add(File(externalPath, getPath(Audio) + "Sent"))

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