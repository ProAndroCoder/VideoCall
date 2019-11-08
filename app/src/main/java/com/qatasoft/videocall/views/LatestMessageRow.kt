package com.qatasoft.videocall.views

import com.qatasoft.videocall.R
import com.qatasoft.videocall.models.ChatMessage
import com.qatasoft.videocall.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_messages.view.*

class LatestMessageRow(private val chatMessage: ChatMessage, val user: User) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.circle_imageview_latest_messages)

        viewHolder.itemView.txt_username_latest_messages.text = user.username

        viewHolder.itemView.txt_message_latest_messages.text = chatMessage.text

        viewHolder.itemView.txt_time_latest_messages.text = chatMessage.sendingTime
    }

    override fun getLayout(): Int {
        return R.layout.item_messages
    }
}