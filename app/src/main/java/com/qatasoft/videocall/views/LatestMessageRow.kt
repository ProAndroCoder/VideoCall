package com.qatasoft.videocall.views

import android.util.Log
import com.qatasoft.videocall.R
import com.qatasoft.videocall.models.ChatMessage
import com.qatasoft.videocall.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.qatasoft.videocall.messages.LatestMessagesActivity
import kotlinx.android.synthetic.main.item_latest_messages.view.*

class LatestMessageRow(val chatMessage: ChatMessage) : Item<ViewHolder>() {
    var chatPartnerUser: User? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {
        //viewHolder.itemView.txt_username_latest_messages.text = chatMessage
        viewHolder.itemView.txt_message_latest_messages.text = chatMessage.text

        var chatPartnerId: String
        if (FirebaseAuth.getInstance().uid == chatMessage.fromId) {
            chatPartnerId = chatMessage.toId
        } else {
            chatPartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java)

                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(viewHolder.itemView.circle_imageview_latest_messages)

                viewHolder.itemView.txt_username_latest_messages.text = chatPartnerUser?.username
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(LatestMessagesActivity.TAG, "There is a problem while fetching User Info : ${p0.message}")
            }
        })
        viewHolder.itemView.circle_imageview_latest_messages
    }

    override fun getLayout(): Int {
        return R.layout.item_latest_messages
    }

}