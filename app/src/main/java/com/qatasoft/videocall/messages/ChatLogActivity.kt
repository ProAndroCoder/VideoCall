package com.qatasoft.videocall.messages

import android.content.Intent
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import com.qatasoft.videocall.bottomFragments.MessagesFragment.Companion.USER_KEY
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.videoCallRequests.SendVideoRequest
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_chat_log.*
import java.text.SimpleDateFormat
import java.util.*

class ChatLogActivity : AppCompatActivity() {
    companion object {
        const val logTAG = "ChatLogActivity"
    }

    lateinit var mStorageRef: StorageReference
    lateinit var mAlertDialog: android.app.AlertDialog
    lateinit var mFiles: ArrayList<MediaFile>


    val FILE_REQUEST_CODE = 24
    val PICK_IMAGE_CODE = 23
    val maxSize = 200000000
    val adapter = GroupAdapter<ViewHolder>()
    var mUser = MainActivity.mUser
    var user: User? = null
    var uid = FirebaseAuth.getInstance().uid

    var attachmentUrl: String = ""
    var attachmentName: String = ""
    var attachmentType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chatlog.adapter = adapter

        //Parcelable Nesne Alma
        user = intent.getParcelableExtra(USER_KEY) ?: null

        setSupportActionBar(toolbar)

        Picasso.get()
                .load(user!!.profileImageUrl)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(chat_userImage)

        // Picasso.get().load(user!!.profileImageUrl).into(chat_userImage)

        chat_username.text = user!!.username

        chat_videocall.setOnClickListener {
            val intent = Intent(this, SendVideoRequest::class.java)
            intent.putExtra(USER_KEY, user)
            startActivity(intent)
        }

        fetchMessages()

        btn_send_chatlog.setOnClickListener {
            performSendMessage()
        }

        img_attachment_chatlog.setOnClickListener {
            selectFiles()
        }
    }

    private fun selectFiles() {
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

    private fun fetchMessages() {
        adapter.clear()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(logTAG, chatMessage.text)
                    val currentUser = mUser

                    if (fromId == chatMessage.fromId && user?.uid == chatMessage.toId) {
                        adapter.add(ChatFromItem(chatMessage, currentUser))
                    } else if (fromId == chatMessage.toId && user?.uid == chatMessage.fromId) {
                        adapter.add(ChatToItem(chatMessage, user!!))
                    }

                    recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
                }
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

    private fun performSendMessage() {
        val text = et_message_chatlog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(USER_KEY)
        val toId = user?.uid

        if (fromId == null || text.isEmpty() || user == null || toId == null) {
            Log.d(logTAG, "There is an error while sending message")
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val sendingTime = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault()).format(Date())

        val chatMessage = ChatMessage(ref.key!!, text, fromId, toId, sendingTime, "", "", "")

        ref.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(logTAG, "Chat Message Saved : ${ref.key}")
                    //Edittext i boşaltma işlemi
                    et_message_chatlog.text.clear()
                    //recyclerview i en aşağı indirme işlemi
                    recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
                }
                .addOnFailureListener {
                    Log.d(logTAG, "Message Cant Send ${it.message}")
                }

        toRef.setValue(chatMessage)

        val latestMessagesRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessagesRef.setValue(chatMessage)

        val latestToMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestToMessageRef.setValue(chatMessage)
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

    private fun performSendAttachment() {
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(USER_KEY)
        val toId = user?.uid

        if (fromId == null || attachmentName.isEmpty() || attachmentType.isEmpty() || attachmentUrl.isEmpty() || user == null || toId == null) {
            Log.d(logTAG, "There is an error while sending attachment")
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val sendingTime = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault()).format(Date())

        val chatMessage = ChatMessage(ref.key!!, "", fromId, toId, sendingTime, attachmentUrl, attachmentName, attachmentType)

        ref.setValue(chatMessage)
                .addOnSuccessListener {
                    //Edittext i boşaltma işlemi
                    et_message_chatlog.text.clear()
                    //recyclerview i en aşağı indirme işlemi
                    recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
                }
                .addOnFailureListener {
                    Log.d(logTAG, "Attachment Cant Send ${it.message}")
                }

        toRef.setValue(chatMessage)

        val latestMessagesRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessagesRef.setValue(chatMessage)

        val latestToMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestToMessageRef.setValue(chatMessage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(logTAG, "message")
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            FILE_REQUEST_CODE -> {
                Log.d(logTAG, "message2")

                mFiles = data!!.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES)!!
                var sumSize: Long = 0
                mFiles.forEach { item ->
                    sumSize += item.size

                    Log.d(logTAG, "OK : " + item.mimeType + "  " + item.size + "  " + item.mediaType + "  " + item.name + "  " + item.thumbnail + "  " + item.height + "  " + sumSize)

                    attachmentType = getTypeOfFile(item.mimeType)
                    attachmentName = item.name

                    val filename = UUID.randomUUID().toString()//Maybe we should change this

                    mAlertDialog = SpotsDialog.Builder().setContext(this).build()
                    mStorageRef = FirebaseStorage.getInstance().getReference("Attachments/$attachmentType/$filename")

                    mAlertDialog.show()
                    val uploadTask = mStorageRef.putFile(item.uri).continueWithTask { task ->
                        if (!task.isSuccessful) {
                            Toast.makeText(this@ChatLogActivity, "Fail", Toast.LENGTH_SHORT).show()
                        }
                        mStorageRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val url = task.result.toString()
                            attachmentUrl = url
                            Log.d(logTAG, url)
                            mAlertDialog.dismiss()
                            performSendAttachment()
                        }
                    }
                }

                if (sumSize <= maxSize) {
                    Toast.makeText(this, "Size of Files : $sumSize", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Files are bigger than 200 MB", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getTypeOfFile(mimeType: String): String {
        return when {
            mimeType.contains("image") -> {
                "image"
            }
            mimeType.contains("video") -> {
                "video"
            }
            mimeType.contains("audio") -> {
                "audio"
            }
            else -> {
                "document"
            }
        }
    }
}


