package com.qatasoft.videocall.views

import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.qatasoft.videocall.MyPreference
import com.qatasoft.videocall.R
import com.qatasoft.videocall.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_user_new_message.view.*

class UserItem(val user: User,val secim:Int,val myUser: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_new_message.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.circle_imageview_user)

        viewHolder.itemView.img_item1.visibility= View.VISIBLE

        if(secim==1){
            viewHolder.itemView.setOnClickListener(View.OnClickListener {
                val uid = FirebaseAuth.getInstance().uid ?: ""
                val ref = FirebaseDatabase.getInstance().getReference("/friends/$uid/${user.uid}")

                ref.setValue(user).addOnSuccessListener {
                    Log.d("SearchActivity", user.username+" is adding to friends")
                }

                val ref2 = FirebaseDatabase.getInstance().getReference("/friends/${user.uid}/$uid")

                ref2.setValue(myUser).addOnSuccessListener {
                    Log.d("SearchActivity", user.username+" is adding to friends")
                }
            })
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_user_new_message
    }
}