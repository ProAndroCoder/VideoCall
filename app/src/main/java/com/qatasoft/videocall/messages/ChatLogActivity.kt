package com.qatasoft.videocall.messages

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.qatasoft.videocall.models.FileType
import com.qatasoft.videocall.request.FirebaseControl
import com.qatasoft.videocall.videoCallRequests.SendVideoRequest
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import java.text.SimpleDateFormat
import java.util.*

class ChatLogActivity : AppCompatActivity() {
    companion object {
        const val logTAG = "ChatLogActivity"
    }

    val fileType = FileType()

    private lateinit var mFiles: ArrayList<MediaFile>
    private lateinit var toId: String
    private lateinit var user: User
    private lateinit var fromId: String

    private var firebaseControl = FirebaseControl()
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

        Picasso.get().load(user.profileImageUrl).into(chat_userImage)

        chat_username.text = user.username

        fetchMessages()
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

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    val currentUser = mUser

                    if (fromId == chatMessage.fromId && user.uid == chatMessage.toId) {
                        adapter.add(ChatFromItem(chatMessage, currentUser))
                    } else if (fromId == chatMessage.toId && user.uid == chatMessage.fromId) {
                        adapter.add(ChatToItem(chatMessage, user))
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

                        Log.d(logTAG, "OK : " + item.mimeType + "  " + item.size + "  " + item.mediaType + "  " + item.name + "  " + item.thumbnail + "  " + item.height + "  " + sumSize)

                        attachmentType = getTypeOfFile(item.mimeType)
                        attachmentName = item.name

                        if (fromId.isEmpty() || attachmentName.isEmpty() || attachmentType.isEmpty() || item.uri == null) {
                            Log.d(logTAG, "There is an error while sending attachment")
                            return
                        }

                        val sendingTime = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault()).format(Date())

                        val chatMessage = ChatMessage("", fromId, toId, sendingTime, attachmentUrl, attachmentName, attachmentType, item.uri.toString())

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
            mimeType.contains(fileType.IMAGE) -> {
                fileType.IMAGE
            }
            mimeType.contains(fileType.VIDEO) -> {
                fileType.VIDEO
            }
            mimeType.contains(fileType.AUDIO) -> {
                fileType.AUDIO
            }
            else -> {
                fileType.DOCUMENT
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
                val ref = FirebaseDatabase.getInstance().getReference("/user-messages/${mUser.uid}/${user!!.uid}")

                ref.removeValue()

                fetchMessages()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}