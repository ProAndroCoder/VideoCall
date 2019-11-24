package com.qatasoft.videocall.messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.qatasoft.videocall.bottomFragments.MessagesFragment.Companion.USER_KEY
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.videoCallRequests.SendVideoRequest
import com.squareup.picasso.NetworkPolicy
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

    val adapter = GroupAdapter<ViewHolder>()
    var mUser = MainActivity.mUser
    var user: User? = null
    var uid = FirebaseAuth.getInstance().uid

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
            Log.d(logTAG, "Attempt to send message ...")
            performSendMessage()
        }
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

                    if (FirebaseAuth.getInstance().uid == chatMessage.fromId && user?.uid == chatMessage.toId) {
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else if (FirebaseAuth.getInstance().uid == chatMessage.toId && user?.uid == chatMessage.fromId) {
                        adapter.add(ChatToItem(chatMessage.text, user!!))
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

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        if (fromId == null) return

        val sendingTime = SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.getDefault()).format(Date())

        val chatMessage = toId?.let { ChatMessage(ref.key!!, text, fromId, it, sendingTime) }

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
}


