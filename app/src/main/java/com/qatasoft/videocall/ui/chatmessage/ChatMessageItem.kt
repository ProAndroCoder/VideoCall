package com.qatasoft.videocall.ui.chatmessage

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.github.abdularis.buttonprogress.DownloadButtonProgress
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.qatasoft.videocall.R
import com.qatasoft.videocall.data.db.entities.ChatMessage
import com.qatasoft.videocall.data.db.entities.Tools
import com.qatasoft.videocall.request.FBaseControl
import com.qatasoft.videocall.ui.chatmessage.ChatFromItem.Companion.isMultiSelectActive
import com.qatasoft.videocall.ui.chatmessage.ChatFromItem.Companion.selectedList
import com.qatasoft.videocall.ui.chatmessage.ChatFromItem.Companion.selectedPositions
import com.qatasoft.videocall.ui.chatmessage.ChatFromItem.Companion.selectedViews
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_chatfromrow_chatlog.view.*
import kotlinx.android.synthetic.main.item_chattorow_chatlog.view.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ChatFromItem(var chatMessage: ChatMessage, val context: Context, var viewModel: ChatMessageViewModel, var listener: OnChatItemClickListener) : Item<ViewHolder>() {
    val logTag = "ChatFromviewLog"
    var isviewSelect = false
    lateinit var itemLayout: View
    lateinit var viewHolder: ViewHolder

    companion object {
        var isMultiSelectActive = false
        var selectedList = ArrayList<ChatMessage>()
        var selectedViews = ArrayList<View>()
        var selectedPositions = ArrayList<Int>()
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        itemLayout = viewHolder.itemView
        this.viewHolder = viewHolder

        if (chatMessage.text.isEmpty()) {

            when (chatMessage.attachmentType) {
                Tools.Image -> {
                    itemLayout.txt_message_from_chatlog.visibility = View.GONE
                    itemLayout.img_from_chatlog.visibility = View.VISIBLE

                    setImage(chatMessage.fileUri, itemLayout.img_from_chatlog)

                    if (chatMessage.attachmentUrl.isEmpty()) {
                        sendAttachment(viewHolder)
                    } else {
                        //Show Image in ViewActivity
                        viewSettings(viewHolder)
                    }
                }

                Tools.Video -> {
                    itemLayout.txt_message_from_chatlog.visibility = View.GONE
                    itemLayout.img_from_chatlog.visibility = View.VISIBLE

                    setImage(chatMessage.fileUri, itemLayout.img_from_chatlog)

                    if (chatMessage.attachmentUrl.isEmpty()) {
                        sendAttachment(viewHolder)
                    } else {
                        //Show video in ViewActivity
                        itemLayout.linear_progress_from_chatlog.visibility = View.VISIBLE
                        itemLayout.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                        itemLayout.progress_from_chatlog.setIdle()

                        setImage(chatMessage.fileUri, itemLayout.img_from_chatlog)

                        viewSettings(viewHolder)
                    }
                }

                Tools.Audio, Tools.Document -> {
                    itemLayout.linear_progress_from_chatlog.visibility = View.VISIBLE

                    //Set position of Progress Button
                    val lp = itemLayout.linear_progress_from_chatlog.layoutParams as RelativeLayout.LayoutParams
                    lp.addRule(RelativeLayout.END_OF, R.id.linear_from_chatlog)
                    itemLayout.linear_progress_from_chatlog.layoutParams = lp

                    itemLayout.txt_attachmentType_from_chatlog.visibility = View.VISIBLE
                    itemLayout.img_attachmentType_from_chatlog.visibility = View.VISIBLE

                    itemLayout.txt_message_from_chatlog.text = chatMessage.attachmentName
                    itemLayout.txt_attachmentType_from_chatlog.text = chatMessage.attachmentType

                    itemLayout.img_attachmentType_from_chatlog.setImageResource(R.drawable.ic_document)

                    if (chatMessage.attachmentUrl.isEmpty()) {
                        sendAttachment(viewHolder)
                    } else {
                        //Send Source to ViewActivity for Showing
                        viewSettings(viewHolder)
                    }
                }
            }
        } else {
            itemLayout.txt_message_from_chatlog.text = chatMessage.text
        }

        //Glide.with(context).load(user.profileImageUrl).into(itemLayout.circleimg_from_chatlog)

        itemLayout.txt_date_from_chatlog.text = chatMessage.sendingTime

        itemLayout.setOnClickListener {
            viewMultiSelectClick()
        }

        itemLayout.card_from_chatlog.setOnClickListener {
            viewMultiSelectClick()
        }

        itemLayout.setOnLongClickListener {
            viewMultiSelectLongClick()
        }

        itemLayout.img_from_chatlog.setOnLongClickListener {
            viewMultiSelectLongClick()
        }

        itemLayout.card_from_chatlog.setOnLongClickListener {
            viewMultiSelectLongClick()
        }

        itemLayout.progress_from_chatlog.setOnLongClickListener {
            viewMultiSelectLongClick()
        }
    }

    private fun viewMultiSelectLongClick(): Boolean {
        if (!isMultiSelectActive) {
            Log.d(logTag, "Long Clicked : ${viewHolder.adapterPosition}")
            itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorChatSelected))
            selectedList.add(chatMessage)
            selectedViews.add(itemLayout)
            selectedPositions.add(viewHolder.adapterPosition)
            isMultiSelectActive = true
            isviewSelect = true

            listener.onContextualState(isMultiSelectActive)

            return true
        }
        return false
    }

    fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getItemViewType(position: Int): Int {
        return position
    }

    private fun viewMultiSelectClick() {
        if (isMultiSelectActive) {
            if (!isviewSelect) {
                selectedList.add(chatMessage)
                selectedViews.add(itemLayout)
                selectedPositions.add(viewHolder.adapterPosition)
                isviewSelect = true
                itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorChatSelected))
            } else {
                selectedList.remove(chatMessage)
                selectedPositions.add(viewHolder.adapterPosition)
                selectedViews.remove(itemLayout)
                isviewSelect = false
                itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAero))
                if (selectedList.size <= 0) {
                    isMultiSelectActive = false
                }
            }
            listener.onContextualState(isMultiSelectActive)
            Log.d(logTag, "Selected Other : ${selectedList.size}")
        }
    }

    private fun sendAttachment(viewHolder: ViewHolder) {
        copyFileToPath()

        itemLayout.linear_progress_from_chatlog.visibility = View.VISIBLE

        itemLayout.progress_from_chatlog.setDeterminate()
        itemLayout.progress_from_chatlog.maxProgress = 100

        val filename = UUID.randomUUID().toString()//Maybe we should change this

        val mStorageRef = FirebaseStorage.getInstance().getReference("Attachments/${chatMessage.attachmentType}/$filename")

        val uploadTask = mStorageRef.putFile(Uri.parse(chatMessage.fileUri))

        uploadTask.addOnProgressListener {
            val progress = (100 * it.bytesTransferred) / it.totalByteCount
            itemLayout.progress_from_chatlog.currentProgress = progress.toInt()

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

                val firebaseControl = FBaseControl()

                if (firebaseControl.performSendMessage(chatMessage)) {
                    viewModel.update(chatMessage)
                }

                itemLayout.progress_from_chatlog.setFinish()


                when (chatMessage.attachmentType) {
                    Tools.Image -> {
                        //Show Image in ViewActivity
                        viewSettings(viewHolder)
                    }

                    Tools.Video -> {
                        itemLayout.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                        itemLayout.progress_from_chatlog.setIdle()

                        //VideoView Activity
                        viewSettings(viewHolder)
                    }

                    Tools.Audio, Tools.Document -> {
                        viewSettings(viewHolder)
                    }
                }
            }
        }.addOnFailureListener {
            itemLayout.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_upload)
            itemLayout.progress_from_chatlog.setIdle()
            Log.d(logTag, "Problem Attachment Can't Send ${it.message}")
        }

        itemLayout.progress_from_chatlog.addOnClickListener(object : DownloadButtonProgress.OnClickListener {
            override fun onFinishButtonClick(view: View?) {
                Log.d(logTag, "Finish Clicked")
            }

            override fun onCancelButtonClick(view: View?) {
                Log.d(logTag, "Cancel Clicked")
                uploadTask.pause()
                itemLayout.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_upload)
                itemLayout.progress_from_chatlog.setIdle()
            }

            override fun onIdleButtonClick(view: View?) {
                Log.d(logTag, "Idle Clicked")
                uploadTask.resume()
                itemLayout.progress_from_chatlog.setDeterminate()
            }
        })
    }

    private fun copyFileToPath() {
        try {
            val copyFile = File(chatMessage.fileUri)

            val targetFile = File(Tools.getAbsolutePath(context, chatMessage.attachmentType) + "/Sent/" + chatMessage.attachmentName)

            copyFile.copyTo(targetFile, true)

            val uri = targetFile.toURI().toString()
            chatMessage.fileUri = uri

        } catch (e: Throwable) {
            getText(e.message.toString())
        }
    }

    private fun getText(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    //Send image,video and doc's source to ViewActivity for Showing
    private fun viewSettings(viewHolder: ViewHolder) {
        when (chatMessage.attachmentType) {
            Tools.Image, Tools.Video -> {
                itemLayout.img_from_chatlog.setOnClickListener {
                    if (!isMultiSelectActive) {
                        listener.onItemClick(chatMessage, viewHolder.adapterPosition, itemLayout.img_from_chatlog)
                    } else {
                        viewMultiSelectClick()
                    }
                }

                itemLayout.progress_from_chatlog.setOnClickListener {
                    if (!isMultiSelectActive) {
                        listener.onItemClick(chatMessage, viewHolder.adapterPosition, itemLayout.img_from_chatlog)
                    } else {
                        viewMultiSelectClick()
                    }
                }
            }
            Tools.Document -> {
                itemLayout.card_from_chatlog.setOnClickListener {
                    if (!isMultiSelectActive) {
                        listener.onItemClick(chatMessage, viewHolder.adapterPosition, itemLayout.card_from_chatlog)
                    } else {
                        viewMultiSelectClick()
                    }
                }
            }
            Tools.Audio -> {
                itemLayout.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                itemLayout.progress_from_chatlog.setIdle()

                val mp = MediaPlayer.create(context, chatMessage.fileUri.toUri())

                itemLayout.progress_from_chatlog.setOnClickListener {
                    if (!isMultiSelectActive) {
                        Log.d(logTag, "Sound Play From Uri")
                        if (mp.isPlaying) {
                            mp.pause()
                            itemLayout.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                            itemLayout.progress_from_chatlog.setIdle()
                        } else {
                            mp.start()
                            itemLayout.progress_from_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_info)
                            itemLayout.progress_from_chatlog.setIdle()
                        }
                    } else {
                        viewMultiSelectClick()
                    }
                }
            }
        }
    }

    override fun getId(): Long {
        return super.getId()
    }


    private fun setImage(imageUrl: String, image: ImageView) {
        val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(ObjectKey(imageUrl))
                .override(100, 100) // resize does not respect aspect ratio

        Glide.with(context).load(imageUrl).apply(requestOptions).into(image)
    }

    override fun getLayout(): Int {
        return R.layout.item_chatfromrow_chatlog
    }
}

class ChatToItem(val chatMessage: ChatMessage, val context: Context, var viewModel: ChatMessageViewModel, var listener: OnChatItemClickListener) : Item<ViewHolder>() {
    private var isviewSelect = false
    private val logTag = "ChatToviewLog"

    private lateinit var itemLayout: View
    private lateinit var viewHolder: ViewHolder

    override fun bind(viewHolder: ViewHolder, position: Int) {
        this.viewHolder = viewHolder
        itemLayout = viewHolder.itemView

        if (chatMessage.text.isEmpty()) {

            when (chatMessage.attachmentType) {
                Tools.Image -> {
                    itemLayout.txt_message_to_chatlog.visibility = View.GONE
                    itemLayout.img_to_chatlog.visibility = View.VISIBLE

                    if (chatMessage.fileUri.isEmpty()) {
                        setImage(chatMessage.attachmentUrl, itemLayout.img_to_chatlog)
                        downloadAttachment()
                    } else {
                        //Show Image in ViewActivity
                        viewSettings(viewHolder)
                    }
                }

                Tools.Video -> {
                    itemLayout.txt_message_to_chatlog.visibility = View.GONE
                    itemLayout.img_to_chatlog.visibility = View.VISIBLE

                    if (chatMessage.fileUri.isEmpty()) {
                        setImage(chatMessage.attachmentUrl, itemLayout.img_to_chatlog)
                        downloadAttachment()

                    } else {
                        //Show video in ViewActivity
                        itemLayout.linear_progress_to_chatlog.visibility = View.VISIBLE
                        itemLayout.progress_to_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                        itemLayout.progress_to_chatlog.setIdle()

                        setImage(chatMessage.fileUri, itemLayout.img_to_chatlog)

                        viewSettings(viewHolder)
                    }
                }

                Tools.Audio, Tools.Document -> {
                    itemLayout.linear_progress_to_chatlog.visibility = View.VISIBLE

                    //Set position of Progress Button
                    val lp = itemLayout.linear_progress_to_chatlog.layoutParams as RelativeLayout.LayoutParams
                    lp.addRule(RelativeLayout.END_OF, R.id.linear_to_chatlog)
                    itemLayout.linear_progress_to_chatlog.layoutParams = lp

                    itemLayout.txt_attachmentType_to_chatlog.visibility = View.VISIBLE
                    itemLayout.img_attachmentType_to_chatlog.visibility = View.VISIBLE

                    itemLayout.txt_message_to_chatlog.text = chatMessage.attachmentName
                    itemLayout.txt_attachmentType_to_chatlog.text = chatMessage.attachmentType

                    itemLayout.img_attachmentType_to_chatlog.setImageResource(R.drawable.ic_document)

                    if (chatMessage.fileUri.isEmpty()) {
                        downloadAttachment()
                    } else {
                        //Send Source to ViewActivity for Showing
                        viewSettings(viewHolder)
                    }
                }
            }
        } else {
            itemLayout.txt_message_to_chatlog.text = chatMessage.text
        }

        //Glide.with(context).load(user.profileImageUrl).into(itemLayout.circleimg_to_chatlog)

        itemLayout.txt_date_to_chatlog.text = chatMessage.sendingTime

        itemLayout.setOnClickListener {
            viewMultiSelectClick()
        }

        itemLayout.card_to_chatlog.setOnClickListener {
            viewMultiSelectClick()
        }

        itemLayout.setOnLongClickListener {
            viewMultiSelectLongClick()
        }

        itemLayout.img_to_chatlog.setOnLongClickListener {
            viewMultiSelectLongClick()
        }

        itemLayout.card_to_chatlog.setOnLongClickListener {
            viewMultiSelectLongClick()
        }

        itemLayout.progress_to_chatlog.setOnLongClickListener {
            viewMultiSelectLongClick()
        }
    }

    private fun viewMultiSelectLongClick(): Boolean {
        if (!isMultiSelectActive) {
            Log.d(logTag, "Long Clicked : ${viewHolder.adapterPosition}")
            itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorChatSelected))
            selectedList.add(chatMessage)
            selectedViews.add(itemLayout)
            selectedPositions.add(viewHolder.adapterPosition)
            isMultiSelectActive = true
            isviewSelect = true

            listener.onContextualState(isMultiSelectActive)

            return true
        }
        return false
    }

    private fun viewMultiSelectClick() {
        if (isMultiSelectActive) {
            if (!isviewSelect) {
                selectedList.add(chatMessage)
                selectedViews.add(itemLayout)
                selectedPositions.add(viewHolder.adapterPosition)
                isviewSelect = true
                itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorChatSelected))
            } else {
                selectedList.remove(chatMessage)
                selectedViews.remove(itemLayout)
                selectedPositions.remove(viewHolder.adapterPosition)
                isviewSelect = false
                itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAero))
                if (selectedList.size <= 0) {
                    isMultiSelectActive = false
                }
            }
            listener.onContextualState(isMultiSelectActive)
            Log.d(logTag, "Selected Other : ${selectedList.size}")
        }
    }

    private fun downloadAttachment() {
        itemLayout.linear_progress_to_chatlog.visibility = View.VISIBLE

        itemLayout.progress_to_chatlog.setDeterminate()
        itemLayout.progress_to_chatlog.maxProgress = 100

        PRDownloader.initialize(context)

        val config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .build()


        PRDownloader.initialize(context, config)

        val downloadDirectory = Tools.getAbsolutePath(context, chatMessage.attachmentType)
        //val downloadDirectory = Tools.getPath(chatMessage.attachmentType)
        val fileUri = File(downloadDirectory + "/${chatMessage.attachmentName}").toURI().toString()

        val downloadTask = PRDownloader.download(chatMessage.attachmentUrl, downloadDirectory, chatMessage.attachmentName).build()
        val downloadId = downloadTask.downloadId

        downloadTask.setOnProgressListener {
            val progress = (100 * it.currentBytes) / it.totalBytes
            itemLayout.progress_to_chatlog.currentProgress = progress.toInt()

        }.setOnCancelListener {
            PRDownloader.cancel(downloadId)
            Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()

        }.setOnPauseListener {
            PRDownloader.pause(downloadId)
            Toast.makeText(context, "Paused", Toast.LENGTH_SHORT).show()

        }.start(object : OnDownloadListener {
            override fun onDownloadComplete() {
                itemLayout.progress_to_chatlog.setFinish()
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
                    Tools.Image -> {
                        //Show Image in ViewActivity
                        viewSettings(viewHolder)
                    }

                    Tools.Video -> {
                        itemLayout.progress_to_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                        itemLayout.progress_to_chatlog.setIdle()

                        //VideoView Activity
                        viewSettings(viewHolder)
                    }

                    Tools.Audio, Tools.Document -> {
                        viewSettings(viewHolder)
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

    //Send image,video and doc's source to ViewActivity for Showing
    private fun viewSettings(viewHolder: ViewHolder) {

        when (chatMessage.attachmentType) {
            Tools.Image, Tools.Video -> {
                setImage(chatMessage.fileUri, itemLayout.img_to_chatlog)

                itemLayout.img_to_chatlog.setOnClickListener {
                    if (!isMultiSelectActive) {
                        listener.onItemClick(chatMessage, viewHolder.adapterPosition, itemLayout.img_to_chatlog)
                    } else {
                        viewMultiSelectClick()
                    }
                }

                itemLayout.progress_to_chatlog.setOnClickListener {
                    if (!isMultiSelectActive) {
                        listener.onItemClick(chatMessage, viewHolder.adapterPosition, itemLayout.img_to_chatlog)
                    } else {
                        viewMultiSelectClick()
                    }
                }
            }
            Tools.Document -> {
                itemLayout.card_to_chatlog.setOnClickListener {
                    if (!isMultiSelectActive) {
                        listener.onItemClick(chatMessage, viewHolder.adapterPosition, itemLayout.img_to_chatlog)
                    } else {
                        viewMultiSelectClick()
                    }
                }
            }
            Tools.Audio -> {
                itemLayout.progress_to_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                itemLayout.progress_to_chatlog.setIdle()

                val mp = MediaPlayer.create(context, chatMessage.fileUri.toUri())

                itemLayout.progress_to_chatlog.setOnClickListener {
                    if (!isMultiSelectActive) {
                        Log.d(logTag, "Sound Play From Uri")
                        if (mp.isPlaying) {
                            mp.pause()
                            itemLayout.progress_to_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_play)
                            itemLayout.progress_to_chatlog.setIdle()
                        } else {
                            mp.start()
                            itemLayout.progress_to_chatlog.idleIcon = ContextCompat.getDrawable(context, R.drawable.ic_info)
                            itemLayout.progress_to_chatlog.setIdle()
                        }
                    } else {
                        viewMultiSelectClick()
                    }
                }
            }
        }
    }

    private fun getText(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    private fun setImage(imageUrl: String, image: ImageView) {
        Glide.with(context).load(imageUrl).into(image)
    }

    override fun getLayout(): Int {
        return R.layout.item_chattorow_chatlog
    }
}

interface OnChatItemClickListener {
    fun onItemClick(item: ChatMessage, position: Int, view: View)
    fun onContextualState(isActive: Boolean)
}