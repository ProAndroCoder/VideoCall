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

class UserItem(val user: User, val secim:Int, val myUser: User, val mContext: FragmentActivity?) : Item<ViewHolder>() {

    public interface OnItemClickListener{
        fun onItemClick(position:Int)
    }
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_new_message.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.circle_imageview_user)

        //SearchActivity için olan kısım. İstenilen kişiyi arkadaşlar listesine ekleme işlemi
        if(secim==1){
            viewHolder.itemView.img_item1.visibility= View.VISIBLE

            viewHolder.itemView.img_item1.setOnClickListener(View.OnClickListener {
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

        //FriendsActivity için olan kısım.
        if(secim==3 && mContext!=null){
            val videoImage=viewHolder.itemView.img_item1
            val chatImage=viewHolder.itemView.img_item2
            videoImage.visibility=View.VISIBLE
            chatImage.visibility=View.VISIBLE

            videoImage.setImageResource(R.drawable.ic_video_on)
            chatImage.setImageResource(R.drawable.ic_chat)

            videoImage.setOnClickListener(View.OnClickListener {
                Log.d("UserItemClass","Video Call Required "+position)
                Toast.makeText(mContext,"Video Call Required "+position,Toast.LENGTH_LONG)
            })

            chatImage.setOnClickListener(View.OnClickListener {
                Log.d("UserItemClass","Chat Required "+position)

                val intent = Intent(mContext, ChatLogActivity::class.java)
                //Başka activitye nesne gönderme Parcelable
                intent.putExtra(NewMessageFragment.USER_KEY, user)
                mContext.startActivity(intent)
            })

        }


    }

    override fun getLayout(): Int {
        return R.layout.item_user_new_message
    }
}