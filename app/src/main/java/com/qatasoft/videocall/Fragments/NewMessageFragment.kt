package com.qatasoft.videocall.Fragments


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.qatasoft.videocall.MyPreference

import com.qatasoft.videocall.R
import com.qatasoft.videocall.messages.ChatLogActivity
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.views.UserItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_home.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class NewMessageFragment : Fragment() {
    var TAG = "NewMessageActivity"
    var mUser= User("","","","","","")

    //Companian Object sayesinde burada tanımlanan değerler diğer activityler tarafından da okunabilir
    companion object {
        val USER_KEY = "USER_KEY"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view=inflater.inflate(R.layout.fragment_home, container, false)

        val myPreference= MyPreference(activity)
        mUser=myPreference.getUserInfo()

        fetchUsers()

        // Inflate the layout for this fragment
        return view
    }

    //Firebase Veritabanımızdan bilgileri çekme işlemini yapan metod
    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach() {
                    Log.d(TAG, "User Info : ${it.toString()}")
                    val user = it.getValue(User::class.java)

                    if (user != null && user.uid != FirebaseAuth.getInstance().uid) {
                        adapter.add(UserItem(user,2,mUser,activity))
                    }
                }

                //Buradaki bilgileri diğer activity e gönderme işlemi
                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    //Başka activitye nesne gönderme Parcelable
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                }

                view?.recyclerview_home?.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "There is an error while fetching user datas.")
            }
        })
    }
}
