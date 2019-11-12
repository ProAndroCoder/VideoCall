package com.qatasoft.videocall.views

import com.qatasoft.videocall.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.qatasoft.videocall.R
import kotlinx.android.synthetic.main.item_chatfromrow_chatlog.view.*
import kotlinx.android.synthetic.main.item_chattorow_chatlog.view.*

class ChatFromItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.txt_message_from_chatlog.text = text

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.circle_imageview_user_from_chatlog)
    }

    override fun getLayout(): Int {
        return R.layout.item_chatfromrow_chatlog
    }
}

class ChatToItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.txt_message_to_chatlog.text = text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.circle_imageview_user_to_chatlog)
    }

    override fun getLayout(): Int {
        return R.layout.item_chattorow_chatlog
    }
}