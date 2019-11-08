package com.qatasoft.videocall.views

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.qatasoft.videocall.Fragments.NewMessageFragment
import com.qatasoft.videocall.R
import com.qatasoft.videocall.messages.ChatLogActivity
import com.qatasoft.videocall.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_user_new_message.view.*

class UserItem(val user: User, private val secim: Int, private val myUser: User, private val mContext: FragmentActivity?) : Item<ViewHolder>() {

    public interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_new_message.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.circle_imageview_user)

        viewHolder.itemView.img_tick_add.setOnClickListener {
            Log.d("UsersFragment", "Adding User To Friends")
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_user_new_message
    }
}