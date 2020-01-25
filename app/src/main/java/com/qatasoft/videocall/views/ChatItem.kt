package com.qatasoft.videocall.views

import android.net.Uri
import android.util.Log
import android.view.View
import com.github.abdularis.buttonprogress.DownloadButtonProgress
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.qatasoft.videocall.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.qatasoft.videocall.R
import com.qatasoft.videocall.messages.ChatLogActivity
import com.qatasoft.videocall.models.ChatMessage
import com.qatasoft.videocall.models.FileType
import com.qatasoft.videocall.request.FirebaseControl
import kotlinx.android.synthetic.main.item_chatfromrow_chatlog.view.*
import kotlinx.android.synthetic.main.item_chattorow_chatlog.view.*
import java.util.*

class ChatFromItem(private var chatMessage: ChatMessage, val user: User) : Item<ViewHolder>() {
    val logTag = "ChatFromItemLog"
    val fileType = FileType()

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val item = viewHolder.itemView

        if (chatMessage.text.isEmpty()) {

            when (chatMessage.attachmentType) {
                fileType.IMAGE -> {
                    Log.d(logTag, "url :" + chatMessage.attachmentUrl)
                    if (chatMessage.attachmentUrl.isEmpty()) {
                        sendAttachment(viewHolder)
                    } else {
                        Picasso.get().load(chatMessage.attachmentUrl).into(item.img_from_chatlog)
                    }
                    item.txt_message_from_chatlog.visibility = View.GONE
                    item.img_from_chatlog.visibility = View.VISIBLE

                }

                fileType.VIDEO -> {
                }

                fileType.AUDIO -> {
                    if (chatMessage.attachmentUrl.isEmpty()) {
                        sendAttachment(viewHolder)
                    }
                    item.txt_attachmentType_from_chatlog.visibility = View.VISIBLE
                    item.img_attachmentType_from_chatlog.visibility = View.VISIBLE

                    item.txt_message_from_chatlog.text = chatMessage.attachmentName
                    item.txt_attachmentType_from_chatlog.text = chatMessage.attachmentType

                    item.img_attachmentType_from_chatlog.setImageResource(R.drawable.ic_document)
                }
                fileType.DOCUMENT -> {
                    if (chatMessage.attachmentUrl.isEmpty()) {
                        sendAttachment(viewHolder)
                    }
                    item.txt_attachmentType_from_chatlog.visibility = View.VISIBLE
                    item.img_attachmentType_from_chatlog.visibility = View.VISIBLE

                    item.txt_message_from_chatlog.text = chatMessage.attachmentName
                    item.txt_attachmentType_from_chatlog.text = chatMessage.attachmentType

                    item.img_attachmentType_from_chatlog.setImageResource(R.drawable.ic_document)
                }
            }
        } else {
            item.txt_message_from_chatlog.text = chatMessage.text
        }

        Picasso.get().load(user.profileImageUrl).into(item.circleimg_from_chatlog)

        item.txt_date_from_chatlog.text = chatMessage.sendingTime
    }

    private fun sendAttachment(viewHolder: ViewHolder) {
        val item = viewHolder.itemView

        item.linear_progress_from_chatlog.visibility = View.VISIBLE

        item.progress_from_chatlog.setDeterminate()

        item.progress_from_chatlog.maxProgress = 100

        val filename = UUID.randomUUID().toString()//Maybe we should change this

        val mStorageRef = FirebaseStorage.getInstance().getReference("Attachments/${chatMessage.attachmentType}/$filename")

        val uploadTask = mStorageRef.putFile(Uri.parse(chatMessage.fileUri))

        uploadTask.addOnProgressListener {
            val progress = (100 * it.bytesTransferred) / it.totalByteCount
            item.progress_from_chatlog.currentProgress = progress.toInt()

        }.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            mStorageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {

                chatMessage.attachmentUrl = task.result.toString()

                val firebaseControl = FirebaseControl()

                firebaseControl.performSendMessage(chatMessage, false)

                item.progress_from_chatlog.setFinish()

                if (chatMessage.attachmentType == "image") {
                    Log.d(logTag, "url 2 :" + chatMessage.attachmentUrl)
                    Picasso.get().load(chatMessage.attachmentUrl).into(item.img_from_chatlog)
                }
            }
        }.addOnFailureListener {
            Log.d(logTag, "Problem Attachment Can't Send ${it.message}")
        }

        item.progress_from_chatlog.addOnClickListener(object : DownloadButtonProgress.OnClickListener {
            override fun onFinishButtonClick(view: View?) {
                Log.d(logTag, "Finish Clicked")
            }

            override fun onCancelButtonClick(view: View?) {
                Log.d(logTag, "Cancel Clicked")
                uploadTask.cancel()
            }

            override fun onIdleButtonClick(view: View?) {
                Log.d(logTag, "Idle Clicked")
            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.item_chatfromrow_chatlog
    }
}

class ChatToItem(private val chatMessage: ChatMessage, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val item = viewHolder.itemView

        if (chatMessage.text.isEmpty()) {

            when (chatMessage.attachmentType) {
                "image" -> {
                    item.txt_message_to_chatlog.visibility = View.GONE
                    item.img_to_chatlog.visibility = View.VISIBLE

                    Picasso.get().load(user.profileImageUrl).into(item.circleimg_to_chatlog)

                    Picasso.get().load(chatMessage.attachmentUrl).into(item.img_to_chatlog)

                    item.txt_date_to_chatlog.text = chatMessage.sendingTime
                }

                "audio" -> {

                }

                "document" -> {

                }
            }
        } else {
            item.txt_message_to_chatlog.text = chatMessage.text
            item.txt_date_to_chatlog.text = chatMessage.sendingTime

            Picasso.get().load(user.profileImageUrl).into(item.circleimg_to_chatlog)
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_chattorow_chatlog
    }
}