package com.qatasoft.videocall.views

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.qatasoft.videocall.R
import com.qatasoft.videocall.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_user_new_message.view.*

class UserItem(val user: User, private var isFollowed: Int, private val myUser: User, private val mContext: Context?) : Item<ViewHolder>() {

    public interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_new_message.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.circle_imageview_user)

        if (isFollowed == 1) {
            viewHolder.itemView.img_tick_add.visibility = View.VISIBLE
            followedImgProcess(viewHolder.itemView.img_tick_add)
        } else if (isFollowed == 0) {
            viewHolder.itemView.img_tick_add.visibility = View.VISIBLE
            unFollowedImgProcess(viewHolder.itemView.img_tick_add)
        } else if (isFollowed == 2) {
            viewHolder.itemView.img_tick_add.visibility = View.GONE
        }
    }

    private fun followedImgProcess(img_tick_add: ImageView) {
        img_tick_add.setImageDrawable(ContextCompat.getDrawable(this.mContext!!, R.drawable.tick_ico))

        Log.d("UsersFragment", "isFollowed : $isFollowed")

        img_tick_add.setOnClickListener {
            isFollowed = 0

            removeFollowed()
            unFollowedImgProcess(img_tick_add)
        }
    }

    private fun unFollowedImgProcess(img_tick_add: ImageView) {
        img_tick_add.setImageDrawable(ContextCompat.getDrawable(this.mContext!!, R.drawable.add_ico))

        Log.d("UsersFragment", "-isFollowed : $isFollowed")

        img_tick_add.setOnClickListener {
            addToFollowed()
            isFollowed = 1
            followedImgProcess(img_tick_add)
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_user_new_message
    }

    private fun addToFollowed() {
        val followed = FirebaseDatabase.getInstance().getReference("/friends/${myUser.uid}/followeds/${user.uid}")
        followed.setValue(user)
                .addOnSuccessListener {
                }
                .addOnFailureListener {
                    Log.d("UsersFragment", "there is a problem : $it")
                }

        val follower = FirebaseDatabase.getInstance().getReference("/friends/${user.uid}/followers/${myUser.uid}")
        follower.setValue(myUser)
                .addOnSuccessListener {
                }
                .addOnFailureListener {
                    Log.d("UsersFragment", "there is a problem : $it")
                }
    }

    private fun removeFollowed() {
        val followed = FirebaseDatabase.getInstance().getReference("/friends/${myUser.uid}/followeds/${user.uid}")
        followed.removeValue()
                .addOnSuccessListener {

                }
                .addOnFailureListener {
                    Log.d("UsersFragment", "there is a problem : $it")
                }

        val follower = FirebaseDatabase.getInstance().getReference("/friends/${user.uid}/followers/${myUser.uid}")
        follower.removeValue()
                .addOnSuccessListener {

                }
                .addOnFailureListener {
                    Log.d("UsersFragment", "there is a problem : $it")
                }


    }
}