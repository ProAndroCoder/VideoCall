package com.qatasoft.videocall.views

import com.qatasoft.videocall.R
import com.qatasoft.videocall.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_user_new_message.view.*

class UserItem(val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_new_message.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.circle_imageview_user)
    }

    override fun getLayout(): Int {
        return R.layout.item_user_new_message
    }
}