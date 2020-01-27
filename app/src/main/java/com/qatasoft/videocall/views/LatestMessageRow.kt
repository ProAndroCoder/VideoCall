package com.qatasoft.videocall.views

import android.content.Context
import com.bumptech.glide.Glide
import com.qatasoft.videocall.R
import com.qatasoft.videocall.models.ChatMessage
import com.qatasoft.videocall.models.User
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_messages.view.*

class LatestMessageRow(private val chatMessage: ChatMessage, val user: User,val context: Context) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        Glide.with(context).load(user.profileImageUrl).into(viewHolder.itemView.circle_imageview_latest_messages)

        viewHolder.itemView.txt_username_latest_messages.text = user.username

        viewHolder.itemView.txt_message_latest_messages.text = chatMessage.text

        viewHolder.itemView.txt_time_latest_messages.text = chatMessage.sendingTime
    }

    override fun getLayout(): Int {
        return R.layout.item_messages
    }
}