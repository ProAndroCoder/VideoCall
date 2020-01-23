package com.qatasoft.videocall.views

import android.view.View
import com.qatasoft.videocall.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.qatasoft.videocall.R
import com.qatasoft.videocall.models.ChatMessage
import kotlinx.android.synthetic.main.item_chatfromrow_chatlog.view.*
import kotlinx.android.synthetic.main.item_chattorow_chatlog.view.*

class ChatFromItem(private val chatMessage: ChatMessage, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val item = viewHolder.itemView

        if (chatMessage.text.isEmpty()) {
            item.txt_message_from_chatlog.text = chatMessage.attachmentName

            Picasso.get().load(user.profileImageUrl).into(item.circleimg_from_chatlog)
        } else {
            item.txt_message_from_chatlog.text = chatMessage.text

            Picasso.get().load(user.profileImageUrl).into(item.circleimg_from_chatlog)
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_chatfromrow_chatlog
    }
}

class ChatToItem(private val chatMessage: ChatMessage, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        if (chatMessage.text.isEmpty()) {
            val item = viewHolder.itemView

            when (chatMessage.attachmentType) {
                "image" -> {
                    item.txt_message_to_chatlog.visibility = View.GONE
                    item.img_to_chatlog.visibility = View.VISIBLE

                    Picasso.get().load(chatMessage.attachmentUrl).into(item.img_to_chatlog)

                    item.txt_date_to_chatlog.text = chatMessage.sendingTime
                }

                "audio" -> {

                }

                "document" -> {

                }
            }
        } else {
            viewHolder.itemView.txt_message_to_chatlog.text = chatMessage.text

            Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.circleimg_to_chatlog)
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_chattorow_chatlog
    }
}