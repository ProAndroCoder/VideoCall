package com.qatasoft.videocall.messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.qatasoft.videocall.Fragments.HomeFragment
import com.qatasoft.videocall.Fragments.NewMessageFragment
import com.qatasoft.videocall.MyPreference
import com.qatasoft.videocall.VideoChatViewActivity
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.item_user_new_message.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    var myUser= User("","","")
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val myPreference= MyPreference(this)
        myUser=myPreference.getLoginInfo()

        recyclerview_chatlog.adapter = adapter

        //Parcelable Nesne Alma
        user = intent.getParcelableExtra<User>(NewMessageFragment.USER_KEY)?:null

        Picasso.get().load(user!!.profileImageUrl).into(chat_userImage)

        chat_username.text= user!!.username

        chat_videocall.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this,VideoChatViewActivity::class.java))
            finish()
        })

        fetchMessages()

        btn_send_chatlog.setOnClickListener {
            Log.d(TAG, "Attempt to send message ...")
            performSendMessage()
        }
    }

    private fun fetchMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)
                    val currentUser = myUser

                    if (FirebaseAuth.getInstance().uid == chatMessage.fromId && user?.uid == chatMessage.toId) {
                        adapter.add(ChatFromItem(chatMessage.text, currentUser!!))
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
        val user = intent.getParcelableExtra<User>(NewMessageFragment.USER_KEY)
        val toId = user?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        if (fromId == null) return

        val chatMessage = toId?.let { ChatMessage(ref.key!!, text, fromId, it, System.currentTimeMillis() / 1000) }

        ref.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "Chat Message Saved : ${ref.key}")
                    //Edittext i boşaltma işlemi
                    et_message_chatlog.text.clear()
                    //recyclerview i en aşağı indirme işlemi
                    recyclerview_chatlog.scrollToPosition(adapter.itemCount - 1)
                }
                .addOnFailureListener {
                    Log.d(TAG, "Message Cant Send ${it.message}")
                }

        toRef.setValue(chatMessage)

        val latestMessagesRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessagesRef.setValue(chatMessage)

        val latestToMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestToMessageRef.setValue(chatMessage)
    }

    companion object {
        val TAG = "ChatLogActivity"
    }
}


