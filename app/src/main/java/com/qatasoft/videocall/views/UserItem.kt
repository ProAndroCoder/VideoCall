package com.qatasoft.videocall.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.R
import com.qatasoft.videocall.bottomFragments.MessagesFragment.Companion.USER_KEY
import com.qatasoft.videocall.bottomFragments.ProfileFragment
import com.qatasoft.videocall.messages.ChatLogActivity
import com.qatasoft.videocall.models.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.item_user.view.*

class UserItem(private val userList: ArrayList<User>, private val secim: Int, private val mUser: User, private val manager: FragmentManager) : RecyclerView.Adapter<UserItem.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        Log.d("UsersFragment", "--- : ${userList.size}")
        return UserViewHolder(view, secim, mUser, manager)
    }

    override fun getItemCount(): Int {
        Log.d("UsersFragment", "Count : ${userList.size}")
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        Log.d("UsersFragment", "Bind : ${userList.size}")
        holder.bindItems(userList[position])
    }

    class UserViewHolder(val view: View, private val secim: Int, private val mUser: User, private val manager: FragmentManager) : RecyclerView.ViewHolder(view) {
        val username: TextView = view.txt_username
        private val userImg: CircleImageView = view.circleimg_user
        private val tickAddImg: ImageView = view.img_tick_add
        private val relativeUser: RelativeLayout = view.relative_user

        fun bindItems(item: User) {
            username.text = item.username
            Picasso.get().load(item.profileImageUrl).into(userImg)

            //SetOnItemClickListener !!!
            relativeUser.setOnClickListener {
                val intent = Intent(view.context, ChatLogActivity::class.java)
                intent.putExtra(USER_KEY, item)
                view.context.startActivity(intent)
            }

            userImg.setOnClickListener {
                val nav = MainActivity.nav
                nav!!.selectedItemId = R.id.navigation_profile
                nav.visibility = View.GONE

                val transaction = manager.beginTransaction()
                val fragment = ProfileFragment()
                val args = Bundle()
                args.putBoolean(MainActivity.IsOwnerInfo, false)
                args.putParcelable(MainActivity.OwnerInfo, item)
                fragment.arguments = args
                transaction.replace(R.id.fragmentHolder, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }

            Log.d("UsersFragment", "--- : ${item.username}")

            //Design tickAddImg according to isFollowed
            if (secim == 0) {
                tickAddImg.visibility = View.VISIBLE
                if (item.isFollowed) {
                    item.isFollowed = false
                    followedImgProcess(item)
                } else {
                    item.isFollowed = true
                    unFollowedImgProcess(item)
                }
            } else if (secim == 1) {
                tickAddImg.visibility = View.GONE
            }
        }

        private fun followedImgProcess(item: User) {
            tickAddImg.setImageResource(R.drawable.tick_ico)
            item.isFollowed = true

            tickAddImg.setOnClickListener {

                removeFollowed(item)
                unFollowedImgProcess(item)
            }
        }

        private fun unFollowedImgProcess(item: User) {
            tickAddImg.setImageResource(R.drawable.add_ico)
            item.isFollowed = false

            tickAddImg.setOnClickListener {
                addToFollowed(item)
                followedImgProcess(item)
            }
        }

        private fun addToFollowed(user: User) {
            val followed = FirebaseDatabase.getInstance().getReference("/friends/${mUser.uid}/followeds/${user.uid}")
            followed.setValue(user)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener {
                        Log.d("UsersFragment", "there is a problem : $it")
                    }

            val follower = FirebaseDatabase.getInstance().getReference("/friends/${user.uid}/followers/${mUser.uid}")
            follower.setValue(mUser)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener {
                        Log.d("UsersFragment", "there is a problem : $it")
                    }
        }

        private fun removeFollowed(user: User) {
            val followed = FirebaseDatabase.getInstance().getReference("/friends/${mUser.uid}/followeds/${user.uid}")
            followed.removeValue()
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {
                        Log.d("UsersFragment", "there is a problem : $it")
                    }

            val follower = FirebaseDatabase.getInstance().getReference("/friends/${user.uid}/followers/${mUser.uid}")
            follower.removeValue()
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {
                        Log.d("UsersFragment", "there is a problem : $it")
                    }
        }
    }
}