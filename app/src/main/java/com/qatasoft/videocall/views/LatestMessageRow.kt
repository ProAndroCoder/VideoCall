package com.qatasoft.videocall.views

import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import com.qatasoft.videocall.R
import com.qatasoft.videocall.models.ChatMessage
import com.qatasoft.videocall.models.User
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_messages.view.*

class LatestMessageRow(private val chatMessage: ChatMessage, val user: User, val context: Context) : Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val item = viewHolder.itemView

        Glide.with(context).load(user.profileImageUrl).into(item.circle_imageview_latest_messages)

        item.txt_username_latest_messages.text = user.username

        //If it is an attachment
        if (chatMessage.attachmentName.isNotEmpty()) {
            item.img_file_latest_messages.visibility = View.VISIBLE
            item.txt_message_latest_messages.text = chatMessage.attachmentName

            if (chatMessage.fileUri.isEmpty()) {
                //Showing recycle icon for attachment
                item.img_file_latest_messages.setImageResource(R.drawable.btn_voice)

            } else {
                //Showing file icon
                item.img_file_latest_messages.setImageResource(R.drawable.add_ico)
            }
        } else {
            item.txt_message_latest_messages.text = chatMessage.text
        }
        item.txt_time_latest_messages.text = chatMessage.sendingTime
    }

    override fun getLayout(): Int {
        return R.layout.item_messages
    }
}