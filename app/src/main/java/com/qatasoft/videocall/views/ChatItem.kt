package com.qatasoft.videocall.views

import android.net.Uri
import android.util.Log
import android.view.View
import com.github.abdularis.buttonprogress.DownloadButtonProgress
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.qatasoft.videocall.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.qatasoft.videocall.R
import com.qatasoft.videocall.messages.ChatLogActivity
import com.qatasoft.videocall.models.ChatMessage
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.item_chatfromrow_chatlog.view.*
import kotlinx.android.synthetic.main.item_chattorow_chatlog.view.*
import java.util.*

class ChatFromItem(private var chatMessage: ChatMessage, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val item = viewHolder.itemView

        if (chatMessage.text.isEmpty()) {

            when (chatMessage.attachmentType) {
                "image" -> {
                    item.txt_message_from_chatlog.visibility = View.GONE
                    item.img_from_chatlog.visibility = View.VISIBLE

                    Picasso.get().load(chatMessage.attachmentUrl).into(item.img_from_chatlog)
                }

                "audio" -> {

                }

                "document" -> {
                    if (chatMessage.attachmentUrl.isEmpty()) {
                        item.linear_progress_from_chatlog.visibility = View.VISIBLE

                        // ADDING ASYNCTASKS

                        item.progress_from_chatlog.setIndeterminate()

                        item.progress_from_chatlog.setDeterminate()

                        item.progress_from_chatlog.maxProgress = 100

                        sendAttachment(viewHolder)

                        item.progress_from_chatlog.addOnClickListener(object : DownloadButtonProgress.OnClickListener {
                            override fun onFinishButtonClick(view: View?) {
                                Log.d(ChatLogActivity.logTAG, "Finish Clicked")
                            }

                            override fun onCancelButtonClick(view: View?) {
                                Log.d(ChatLogActivity.logTAG, "Cancel Clicked")
                            }

                            override fun onIdleButtonClick(view: View?) {
                                Log.d(ChatLogActivity.logTAG, "Idle Clicked")

                            }
                        })
                    }
                    item.txt_attachmentType_from_chatlog.visibility = View.VISIBLE
                    item.img_attachmentType_from_chatlog.visibility = View.VISIBLE

                    item.txt_message_from_chatlog.text = chatMessage.attachmentName
                    item.txt_attachmentType_from_chatlog.text = chatMessage.attachmentType

                    item.img_attachmentType_from_chatlog.setImageResource(R.drawable.ic_document)
                }
                "video" -> {


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
        val filename = UUID.randomUUID().toString()//Maybe we should change this

        val mStorageRef = FirebaseStorage.getInstance().getReference("Attachments/${chatMessage.attachmentType}/$filename")

        mStorageRef.putFile(Uri.parse(chatMessage.fileUri)).addOnProgressListener {
            val progress = (100 * it.bytesTransferred) / it.totalByteCount
            item.progress_from_chatlog.currentProgress = progress.toInt()

        }.continueWithTask { task ->
            if (!task.isSuccessful) {
                Log.d("ChatItemLog", "Success")
            }
            mStorageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val url = task.result.toString()
                chatMessage.attachmentUrl = url
                Log.d("ChatItemLog", url)

                val ref = FirebaseDatabase.getInstance().getReference("/user-messages/${chatMessage.fromId}/${chatMessage.toId}").push()

                Log.d("ChatItemLog", ref.key!!)
                Log.d("ChatItemLog", ref.key!!)


                val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/${chatMessage.toId}/${chatMessage.fromId}").push()

                ref.setValue(chatMessage)
                        .addOnSuccessListener {
                            //recyclerview i en aşağı indirme işlemi
                            Log.d(ChatLogActivity.logTAG, "Attachment Send Success ")
                        }
                        .addOnFailureListener {
                            Log.d(ChatLogActivity.logTAG, "Attachment Cant Send ${it.message}")
                        }

                toRef.setValue(chatMessage)

                val latestMessagesRef = FirebaseDatabase.getInstance().getReference("/latest-messages/${chatMessage.fromId}/${chatMessage.toId}")
                latestMessagesRef.setValue(chatMessage)

                val latestToMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/${chatMessage.toId}/${chatMessage.fromId}")
                latestToMessageRef.setValue(chatMessage)
                item.progress_from_chatlog.setFinish()
            }
        }
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