package com.qatasoft.videocall.views

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.github.abdularis.buttonprogress.DownloadButtonProgress
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.qatasoft.videocall.MainActivity.Companion.keyViewActivityType
import com.qatasoft.videocall.MainActivity.Companion.keyViewActivityUri
import com.qatasoft.videocall.R
import com.qatasoft.videocall.ViewActivity
import com.qatasoft.videocall.models.ChatMessage
import com.qatasoft.videocall.models.Tools
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.request.FirebaseControl
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_chatfromrow_chatlog.view.*
import kotlinx.android.synthetic.main.item_chattorow_chatlog.view.*
import java.util.*

class ChatFromItem(var chatMessage: ChatMessage, val user: User, val context: Context) : Item<ViewHolder>() {
    val logTag = "ChatFromItemLog"

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val item = viewHolder.itemView

        if (chatMessage.text.isEmpty()) {

            when (chatMessage.attachmentType) {
                Tools.image -> {
                    item.txt_message_from_chatlog.visibility = View.GONE
                    item.img_from_chatlog.visibility = View.VISIBLE

                    setImage(chatMessage.fileUri, item.img_from_chatlog)

                    if (chatMessage.attachmentUrl.isEmpty()) {
                        sendAttachment(viewHolder)
                    } else {
                        //Show Image in ViewActivity
                        item.img_from_chatlog.setOnClickListener {
                            val intent = Intent(context, ViewActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra(keyViewActivityUri, chatMessage.fileUri)
                            intent.putExtra(keyViewActivityType, Tools.image)
                            startActivity(context, intent, null)
                        }
                    }
                }

                Tools.video -> {
                    item.txt_message_from_chatlog.visibility = View.GONE
                    item.img_from_chatlog.visibility = View.VISIBLE

                    setImage(chatMessage.fileUri, item.img_from_chatlog)

                    if (chatMessage.attachmentUrl.isEmpty()) {
                        sendAttachment(viewHolder)
                    } else {
                        //Show video in ViewActivity
                        item.linear_progress_from_chatlog.visibility = View.VISIBLE
                        item.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                        item.progress_from_chatlog.setIdle()

                        setImage(chatMessage.fileUri, item.img_from_chatlog)

                        item.img_from_chatlog.setOnClickListener {
                            //Send to ViewActivity
                            val intent = Intent(context, ViewActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra(keyViewActivityUri, chatMessage.fileUri)
                            intent.putExtra(keyViewActivityType, Tools.video)
                            startActivity(context, intent, null)
                            Log.d(logTag, "VideoView")
                        }
                    }
                }

                Tools.audio -> {
                    item.linear_progress_from_chatlog.visibility = View.VISIBLE

                    //Set position of Progress Button
                    val lp = item.linear_progress_from_chatlog.layoutParams as RelativeLayout.LayoutParams
                    lp.addRule(RelativeLayout.END_OF, R.id.linear_from_chatlog)
                    item.linear_progress_from_chatlog.layoutParams = lp


                    item.txt_attachmentType_from_chatlog.visibility = View.VISIBLE
                    item.img_attachmentType_from_chatlog.visibility = View.VISIBLE

                    item.txt_message_from_chatlog.text = chatMessage.attachmentName
                    item.txt_attachmentType_from_chatlog.text = chatMessage.attachmentType

                    item.img_attachmentType_from_chatlog.setImageResource(R.drawable.ic_document)

                    if (chatMessage.attachmentUrl.isEmpty()) {
                        sendAttachment(viewHolder)
                    } else {
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
                Tools.document -> {
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

        Glide.with(context).load(user.profileImageUrl).into(item.circleimg_from_chatlog)

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
                    Tools.image -> {
                        //Show Image in ViewActivity

                        item.img_from_chatlog.setOnClickListener {
                            val intent = Intent(context, ViewActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra(keyViewActivityUri, chatMessage.fileUri)
                            intent.putExtra(keyViewActivityType, Tools.image)
                            startActivity(context, intent, null)
                        }

                    }

                    Tools.video -> {
                        item.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                        item.progress_from_chatlog.setIdle()

                        //VideoView Activity
                        item.img_from_chatlog.setOnClickListener {
                            Log.d(logTag, "Switch to ViewActivity")

                            val intent = Intent(context, ViewActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra(keyViewActivityUri, chatMessage.fileUri)
                            intent.putExtra(keyViewActivityType, Tools.video)
                            startActivity(context, intent, null)
                        }
                    }

                    Tools.audio -> {
                        //Set position of Progress Button
                        val lp = item.linear_progress_from_chatlog.layoutParams as RelativeLayout.LayoutParams
                        lp.addRule(RelativeLayout.END_OF, R.id.linear_from_chatlog)
                        item.linear_progress_from_chatlog.layoutParams = lp

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
            item.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_upload)
            item.progress_from_chatlog.setIdle()
            Log.d(logTag, "Problem Attachment Can't Send ${it.message}")
        }

        item.progress_from_chatlog.addOnClickListener(object : DownloadButtonProgress.OnClickListener {
            override fun onFinishButtonClick(view: View?) {
                Log.d(logTag, "Finish Clicked")
            }

            override fun onCancelButtonClick(view: View?) {
                Log.d(logTag, "Cancel Clicked")
                uploadTask.pause()
                item.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_upload)
                item.progress_from_chatlog.setIdle()
            }

            override fun onIdleButtonClick(view: View?) {
                Log.d(logTag, "Idle Clicked")
                uploadTask.resume()
                item.progress_from_chatlog.setDeterminate()
            }
        })
    }

    private fun setImage(imageUrl: String, image: ImageView) {
        Glide.with(context).load(imageUrl).into(image)
    }

    override fun getLayout(): Int {
        return R.layout.item_chatfromrow_chatlog
    }
}

class ChatToItem(private val chatMessage: ChatMessage, val user: User, val context: Context) : Item<ViewHolder>() {

    private val logTag = "ChatToItemLog"

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val item = viewHolder.itemView

        if (chatMessage.text.isEmpty()) {

            Toast.makeText(context, "FileUri isEmpty:${chatMessage.fileUri.isEmpty()}", Toast.LENGTH_LONG).show()

            when (chatMessage.attachmentType) {
                Tools.image -> {
                    item.txt_message_to_chatlog.visibility = View.GONE
                    item.img_to_chatlog.visibility = View.VISIBLE

                    if (chatMessage.fileUri.isEmpty()) {
                        setImage(chatMessage.attachmentUrl, item.img_to_chatlog)
                        downloadAttachment(viewHolder)

                    } else {
                        setImage(chatMessage.fileUri, item.img_to_chatlog)

                        //Show Image in ViewActivity
                        item.img_to_chatlog.setOnClickListener {
                            val intent = Intent(context, ViewActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra(keyViewActivityUri, chatMessage.fileUri)
                            intent.putExtra(keyViewActivityType, Tools.image)
                            startActivity(context, intent, null)
                        }
                    }
                }

                Tools.video -> {
                    item.txt_message_to_chatlog.visibility = View.GONE
                    item.img_to_chatlog.visibility = View.VISIBLE

                    if (chatMessage.fileUri.isEmpty()) {

                        setImage(chatMessage.attachmentUrl, item.img_to_chatlog)

                        downloadAttachment(viewHolder)
                    } else {
                        setImage(chatMessage.fileUri, item.img_to_chatlog)

                        //Show video in ViewActivity
                        item.linear_progress_to_chatlog.visibility = View.VISIBLE
                        item.progress_to_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                        item.progress_to_chatlog.setIdle()

                        setImage(chatMessage.fileUri, item.img_to_chatlog)

                        item.img_to_chatlog.setOnClickListener {
                            //Send to ViewActivity
                            val intent = Intent(context, ViewActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra(keyViewActivityUri, chatMessage.fileUri)
                            intent.putExtra(keyViewActivityType, Tools.video)
                            startActivity(context, intent, null)
                            Log.d(logTag, "VideoView")
                        }
                    }
                }

                Tools.audio -> {
                    item.linear_progress_to_chatlog.visibility = View.VISIBLE

                    //Set position of Progress Button
                    val lp = item.linear_progress_to_chatlog.layoutParams as RelativeLayout.LayoutParams
                    lp.addRule(RelativeLayout.END_OF, R.id.linear_to_chatlog)
                    item.linear_progress_to_chatlog.layoutParams = lp


                    item.txt_attachmentType_to_chatlog.visibility = View.VISIBLE
                    item.img_attachmentType_to_chatlog.visibility = View.VISIBLE

                    item.txt_message_to_chatlog.text = chatMessage.attachmentName
                    item.txt_attachmentType_to_chatlog.text = chatMessage.attachmentType

                    item.img_attachmentType_to_chatlog.setImageResource(R.drawable.ic_document)

                    if (chatMessage.fileUri.isEmpty()) {
                        downloadAttachment(viewHolder)
                    } else {
                        item.progress_to_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                        item.progress_to_chatlog.setIdle()

                        val mp = MediaPlayer.create(context, chatMessage.fileUri.toUri())

                        item.progress_to_chatlog.setOnClickListener {
                            Log.d(logTag, "Sound Play to Uri")
                            if (mp.isPlaying) {
                                mp.pause()
                                item.progress_to_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                                item.progress_to_chatlog.setIdle()
                            } else {
                                mp.start()
                                item.progress_to_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_add_img)
                                item.progress_to_chatlog.setIdle()
                            }
                        }
                    }
                }
                Tools.document -> {
                    if (chatMessage.fileUri.isEmpty()) {
                        downloadAttachment(viewHolder)
                    }
                    item.txt_attachmentType_to_chatlog.visibility = View.VISIBLE
                    item.img_attachmentType_to_chatlog.visibility = View.VISIBLE

                    item.txt_message_to_chatlog.text = chatMessage.attachmentName
                    item.txt_attachmentType_to_chatlog.text = chatMessage.attachmentType

                    item.img_attachmentType_to_chatlog.setImageResource(R.drawable.ic_document)
                }
            }
        } else {
            item.txt_message_to_chatlog.text = chatMessage.text
        }

        Glide.with(context).load(user.profileImageUrl).into(item.circleimg_to_chatlog)

        item.txt_date_to_chatlog.text = chatMessage.sendingTime
    }

    private fun downloadAttachment(viewHolder: ViewHolder) {
        val item = viewHolder.itemView

        item.linear_progress_to_chatlog.visibility = View.VISIBLE

        item.progress_to_chatlog.setDeterminate()
        item.progress_to_chatlog.maxProgress = 100

        PRDownloader.initialize(context)

        val config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .build()


        PRDownloader.initialize(context, config)

        val downloadDirectory = Tools.getExternalDirectory(context) + "/VideoCall" + "/${chatMessage.attachmentType}"
        val fileUri = downloadDirectory + "/${chatMessage.attachmentName}".toUri()

        val downloadTask = PRDownloader.download(chatMessage.attachmentUrl, downloadDirectory, chatMessage.attachmentName).build()
        val downloadId = downloadTask.downloadId

        downloadTask.setOnProgressListener {
            val progress = (100 * it.currentBytes) / it.totalBytes
            item.progress_to_chatlog.currentProgress = progress.toInt()

        }.setOnCancelListener {
            PRDownloader.cancel(downloadId)
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()

        }.setOnPauseListener {
            PRDownloader.pause(downloadId)
            Toast.makeText(context, "Paused", Toast.LENGTH_SHORT).show()

        }.start(object : OnDownloadListener {
            override fun onDownloadComplete() {
                item.progress_to_chatlog.setFinish()
                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()

                chatMessage.fileUri = fileUri

                val mDatabase = FirebaseDatabase.getInstance().getReference("/user-messages")
                val lDatabase = FirebaseDatabase.getInstance().getReference("/latest-messages")

                val toRef = mDatabase.child(chatMessage.toId).child(chatMessage.fromId)
                val latestToRef = lDatabase.child(chatMessage.toId).child(chatMessage.fromId)

                toRef.child(chatMessage.refKey).setValue(chatMessage).addOnFailureListener {
                    Toast.makeText(context, "Error 1", Toast.LENGTH_SHORT).show()

                }
                latestToRef.setValue(chatMessage).addOnFailureListener {
                    Toast.makeText(context, "Error 2", Toast.LENGTH_SHORT).show()
                }

                when (chatMessage.attachmentType) {
                    Tools.image -> {
                        //Show Image in ViewActivity

                        item.img_to_chatlog.setOnClickListener {
                            val intent = Intent(context, ViewActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra(keyViewActivityUri, chatMessage.fileUri)
                            intent.putExtra(keyViewActivityType, Tools.image)
                            startActivity(context, intent, null)
                        }

                    }

                    Tools.video -> {
                        item.progress_to_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                        item.progress_to_chatlog.setIdle()

                        //VideoView Activity
                        item.img_to_chatlog.setOnClickListener {
                            Log.d(logTag, "Switch to ViewActivity")

                            val intent = Intent(context, ViewActivity::class.java)
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra(keyViewActivityUri, chatMessage.fileUri)
                            intent.putExtra(keyViewActivityType, Tools.video)
                            startActivity(context, intent, null)
                        }
                    }

                    Tools.audio -> {
                        //Set position of Progress Button
                        val lp = item.linear_progress_to_chatlog.layoutParams as RelativeLayout.LayoutParams
                        lp.addRule(RelativeLayout.END_OF, R.id.linear_to_chatlog)
                        item.linear_progress_to_chatlog.layoutParams = lp

                        item.progress_to_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                        item.progress_to_chatlog.setIdle()

                        val mp = MediaPlayer.create(context, chatMessage.fileUri.toUri())

                        item.progress_to_chatlog.setOnClickListener {
                            Log.d(logTag, "Sound Play To Uri")
                            if (mp.isPlaying) {
                                mp.pause()
                                item.progress_to_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                                item.progress_to_chatlog.setIdle()
                            } else {
                                mp.start()
                                item.progress_to_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_add_img)
                                item.progress_to_chatlog.setIdle()
                            }
                        }
                    }
                }
            }

            override fun onError(error: Error?) {
                Toast.makeText(context, ":) Yine Kaldın Öyle İyi mi :) ${error!!.connectionException}  ${error.isConnectionError}", Toast.LENGTH_SHORT).show()
                Log.d(logTag, "Error : ${error.connectionException}")
            }

        })

        /*val downloadManager: DownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

val downloadDirectory = "VideoCall" + "/${chatMessage.attachmentType}"
        val uri = Uri.parse(chatMessage.attachmentUrl)
        val request = DownloadManager.Request(uri)

        Toast.makeText(context, "Download Process : $downloadDirectory", Toast.LENGTH_LONG).show()

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(context, downloadDirectory, chatMessage.attachmentName)

        val id = downloadManager.enqueue(request)*/


    }

    private fun setImage(imageUrl: String, image: ImageView) {
        Glide.with(context).load(imageUrl).into(image)
    }

    override fun getLayout(): Int {
        return R.layout.item_chattorow_chatlog
    }
}