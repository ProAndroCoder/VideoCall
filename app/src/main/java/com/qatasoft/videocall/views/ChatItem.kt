package com.qatasoft.videocall.views

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.MediaController
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
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
import kotlinx.android.synthetic.main.activity_general_info.*
import kotlinx.android.synthetic.main.item_chatfromrow_chatlog.view.*
import kotlinx.android.synthetic.main.item_chattorow_chatlog.view.*
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class ChatFromItem(private var chatMessage: ChatMessage, val user: User, val context: Context) : Item<ViewHolder>() {
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
                    Log.d(logTag, "url :" + chatMessage.attachmentUrl)
                    if (chatMessage.attachmentUrl.isEmpty()) {
                        sendAttachment(viewHolder)
                    } else {
                        item.linear_progress_from_chatlog.visibility = View.VISIBLE
                        item.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                        item.progress_from_chatlog.setIdle()

                        item.progress_from_chatlog.setOnClickListener {
                            Log.d(logTag, "Video Play From Uri")
                        }
                        //Picasso.get().load(chatMessage.attachmentUrl).into(item.img_from_chatlog)
                    }
                    Log.d(logTag, "Video Place")

                    item.txt_message_from_chatlog.visibility = View.GONE
                    item.videoView_from_chatlog.visibility = View.VISIBLE

                    item.videoView_from_chatlog.setVideoURI(chatMessage.fileUri.toUri())
                    var mediaController = MediaController(context)

                    item.videoView_from_chatlog.setMediaController(mediaController)

                    /*val metrics2 = DisplayMetrics()

                    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

                    windowManager.defaultDisplay.getMetrics(metrics2)
                    val params2 = item.videoView_from_chatlog.layoutParams as RelativeLayout.LayoutParams
                    params2.width = (300 * metrics2.density).toInt()
                    params2.height = (250 * metrics2.density).toInt()
                    params2.leftMargin = 30
                    item.videoView_from_chatlog.layoutParams = params2
*/
                    item.videoView_from_chatlog.requestFocus()

                    item.videoView_from_chatlog.seekTo(2)
                }

                fileType.AUDIO -> {
                    if (chatMessage.attachmentUrl.isEmpty()) {
                        sendAttachment(viewHolder)
                    } else {
                        item.linear_progress_from_chatlog.visibility = View.VISIBLE
                        item.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                        item.progress_from_chatlog.setIdle()

                        val mp = MediaPlayer.create(context, chatMessage.fileUri.toUri())

                        item.progress_from_chatlog.setOnClickListener {
                            Log.d(logTag, "Sound Play From Uri")
                            if (mp.isPlaying) {
                                mp.pause()
                                item.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                                item.progress_from_chatlog.setIdle()
                            } else {
                                mp.start()
                                item.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_add_img)
                                item.progress_from_chatlog.setIdle()
                            }
                        }
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

                when (chatMessage.attachmentType) {
                    fileType.IMAGE -> {
                        Log.d(logTag, "url 2 :" + chatMessage.attachmentUrl)
                        Picasso.get().load(chatMessage.attachmentUrl).into(item.img_from_chatlog)
                    }
                    fileType.VIDEO -> {
                        item.linear_progress_from_chatlog.visibility = View.VISIBLE
                        item.progress_from_chatlog.setIdle()

                        item.progress_from_chatlog.setOnClickListener {
                            Log.d(logTag, "Video Play From Uri")
                        }
                    }
                    fileType.AUDIO -> {
                        item.linear_progress_from_chatlog.visibility = View.VISIBLE
                        item.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                        item.progress_from_chatlog.setIdle()

                        val mp = MediaPlayer.create(context, chatMessage.fileUri.toUri())

                        item.progress_from_chatlog.setOnClickListener {
                            Log.d(logTag, "Sound Play From Uri")
                            if (mp.isPlaying) {
                                mp.pause()
                                item.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                                item.progress_from_chatlog.setIdle()
                            } else {
                                mp.start()
                                item.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_add_img)
                                item.progress_from_chatlog.setIdle()
                            }
                        }
                    }
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
                item.progress_from_chatlog.setIdle()
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