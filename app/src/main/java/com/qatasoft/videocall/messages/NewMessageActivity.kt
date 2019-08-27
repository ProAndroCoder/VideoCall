package com.qatasoft.videocall.messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.qatasoft.videocall.R
import com.qatasoft.videocall.views.UserItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import com.qatasoft.videocall.models.User
import kotlinx.android.synthetic.main.activity_new_message.*

class NewMessageActivity : AppCompatActivity() {
    var TAG = "NewMessageActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"

        fetchUsers()
    }

    //Companian Object sayesinde burada tanımlanan değerler diğer activityler tarafından da okunabilir
    companion object {
        val USER_KEY = "USER_KEY"
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
                        adapter.add(UserItem(user))
                    }
                }

                //Buradaki bilgileri diğer activity e gönderme işlemi
                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    //Başka activitye nesne gönderme Parcelable
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)
                    finish()
                }

                recyclerview_new_messages.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "There is an error while fetching user datas.")
            }
        })
    }
}

