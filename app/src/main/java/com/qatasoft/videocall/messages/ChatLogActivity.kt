package com.qatasoft.videocall.messages

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.util.Log
import android.view.Menu
import android.util.Pair
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.qatasoft.videocall.R
import com.qatasoft.videocall.models.ChatMessage
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.views.ChatFromItem
import com.qatasoft.videocall.views.ChatToItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import com.qatasoft.videocall.bottomFragments.MessagesFragment.Companion.USER_KEY
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.ViewActivity
import com.qatasoft.videocall.models.Tools
import com.qatasoft.videocall.request.FBaseControl
import com.qatasoft.videocall.videoCallRequests.SendVideoRequest
import com.qatasoft.videocall.views.OnChatItemClickListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_chat_log.toolbar
import kotlinx.android.synthetic.main.item_chatfromrow_chatlog.view.*
import kotlinx.android.synthetic.main.item_chattorow_chatlog.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatLogActivity : AppCompatActivity() {
    companion object {
        const val logTAG = "ChatLogActivityLogs"
    }

    private lateinit var mFiles: ArrayList<MediaFile>
    private lateinit var toId: String
    private lateinit var user: User
    private lateinit var fromId: String

    private var firebaseControl = FBaseControl()
    private val FILE_REQUEST_CODE = 24
    private val maxSize = 200000000
    val adapter = GroupAdapter<ViewHolder>()
    var mUser = MainActivity.mUser

    private var attachmentUrl: String = ""
    private var attachmentName: String = ""
    private var attachmentType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        setSupportActionBar(toolbar)

        recyclerview_chatlog.adapter = adapter

        getChatInfo()

        chat_videocall.setOnClickListener {
            val intent = Intent(this, SendVideoRequest::class.java)
            intent.putExtra(USER_KEY, user)
            startActivity(intent)
        }

        btn_send_chatlog.setOnClickListener {
            performSendMessage()
        }

        img_attachment_chatlog.setOnClickListener {
            performSendAttachment()
        }
    }

    private fun getChatInfo() {
        //Parcelable Nesne Alma
        user = intent.getParcelableExtra(USER_KEY)!!

        fromId = FirebaseAuth.getInstance().uid!!
        toId = user.uid

        Glide.with(this).load(user.profileImageUrl).into(chat_userImage)

        chat_username.text = user.username

        fetchMessages()

        //changeEnterExitTransition()
    }

    private fun performSendAttachment() {
        val intent = Intent(this, FilePickerActivity::class.java)
        intent.putExtra(FilePickerActivity.CONFIGS, Configurations.Builder()
                .setCheckPermission(true)
                .enableVideoCapture(true)
                .setShowImages(true)
                .setShowVideos(true)
                .setShowAudios(true)
                .setShowFiles(true)
                .enableImageCapture(true)
                .setMaxSelection(10)
                .setSkipZeroSizeFiles(true)
                .build())
        startActivityForResult(intent, FILE_REQUEST_CODE)
    }

    private fun performSendMessage() {
        val text = et_message_chatlog.text.toString()

        val sendingTime = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault()).format(Date())

        val chatMessage = ChatMessage(text, fromId, toId, sendingTime)

        if (fromId.isEmpty() || text.isEmpty() || toId.isEmpty()) {
            Log.d(logTAG, "There is an error while sending message")
            return
        }

        if (firebaseControl.performSendMessage(chatMessage, false)) {
            //Mesaj kısmını boşaltma
            et_message_chatlog.text.clear()
            //En Son atılan mesaja odaklanma
            recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
        } else {
            Toast.makeText(this, "Send Message Error!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchMessages() {
        adapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener, OnChatItemClickListener {
            override fun onItemClick(item: ChatMessage, position: Int, view: View) {
                Log.d(logTAG, "Click Info : ${item.attachmentType} ${item.attachmentName} $position")

                when (item.attachmentType) {
                    Tools.Image, Tools.Video -> {
                        val sharedIntent = Intent(applicationContext, ViewActivity::class.java)

                        val transition = item.attachmentType + "Transition"

                        val pairs = Pair<View, String>(view, transition)

                        val options = ActivityOptions.makeSceneTransitionAnimation(this@ChatLogActivity, pairs)

                        sharedIntent.putExtra(MainActivity.keyViewActivityUri, item.fileUri)
                        sharedIntent.putExtra(MainActivity.keyViewActivityType, item.attachmentType)

                        startActivity(sharedIntent, options.toBundle())
                    }
                    Tools.Document -> {

                    }
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    val currentUser = mUser

                    if (fromId == chatMessage.fromId && user.uid == chatMessage.toId) {
                        adapter.add(ChatFromItem(chatMessage, currentUser, applicationContext, this))
                    } else if (fromId == chatMessage.toId && user.uid == chatMessage.fromId) {
                        adapter.add(ChatToItem(chatMessage, user, applicationContext, this))
                    }
                }
                recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            FILE_REQUEST_CODE -> {

                if (data == null) return

                mFiles = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES)!!
                var sumSize: Long = 0
                mFiles.forEach {
                    sumSize += it.size
                }

                if (sumSize <= maxSize) {
                    Toast.makeText(this, "Size of Files : $sumSize", Toast.LENGTH_SHORT).show()

                    mFiles.forEach { item ->
                        sumSize += item.size

                        Log.d(logTAG, "OK : " + item.mimeType + "  " + item.size + "  " + item.mediaType + "  " + item.name + " path: " + item.path + "  " + item.uri + "  " + sumSize)

                        attachmentType = getTypeOfFile(item.mimeType)
                        attachmentName = item.name

                        if (fromId.isEmpty() || attachmentName.isEmpty() || attachmentType.isEmpty() || item.uri == null) {
                            Log.d(logTAG, "There is an error while sending attachment")
                            return
                        }

                        val sendingTime = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault()).format(Date())

                        val chatMessage = ChatMessage("", fromId, toId, sendingTime, attachmentUrl, attachmentName, attachmentType, item.path)

                        firebaseControl.performSendMessage(chatMessage, true)
                    }

                } else {
                    Toast.makeText(this, "Files are bigger than 200 MB", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getTypeOfFile(mimeType: String): String {
        return when {
            mimeType.contains(Tools.image) -> {
                Tools.Image
            }
            mimeType.contains(Tools.video) -> {
                Tools.Video
            }
            mimeType.contains(Tools.audio) -> {
                Tools.Audio
            }
            else -> {
                Tools.Document
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.remove_chatlog -> {
                val ref = FirebaseDatabase.getInstance().getReference("/user-messages/${mUser.uid}/${user.uid}")

                ref.removeValue()

                fetchMessages()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeEnterExitTransition() {
        val enter = ChangeBounds()
        enter.duration = 5000

        val exit = ChangeBounds()
        exit.interpolator = DecelerateInterpolator()
        exit.duration = 5000

        window.sharedElementEnterTransition = enter
        window.sharedElementReturnTransition = exit
    }
}