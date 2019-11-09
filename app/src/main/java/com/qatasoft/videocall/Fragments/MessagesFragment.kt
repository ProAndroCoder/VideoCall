package com.qatasoft.videocall.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.qatasoft.videocall.MainActivity
import com.qatasoft.videocall.R
import com.qatasoft.videocall.messages.ChatLogActivity
import com.qatasoft.videocall.models.ChatMessage
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.registerlogin.LoginActivity
import com.qatasoft.videocall.views.LatestMessageRow
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_messages.*

class MessagesFragment : Fragment(), SearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            if (newText.isNotEmpty()) {
                adapter.clear()
                searchText = newText.toString()
                fetchLatestMessages()
                Log.d(logTAG, "TextChanged " + searchText)
            } else {
                adapter.clear()
                fetchLatestMessages()
                Log.d(logTAG, "TextChanged Else " + searchText)
            }
        }
        return true
    }

    companion object {
        const val logTAG = "MessagesFragment"
        const val USER_KEY = "USER_INFO_KEY"
    }

    var searchText = ""

    private val adapter = GroupAdapter<ViewHolder>()
    private val latestMessagesMap = HashMap<String, ChatMessage>()
    private val mUser = MainActivity.mUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerview_messages.adapter = adapter

        //Itemlar arasında ayıraç konuluyor
        recyclerview_messages.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, _ ->
            val row = item as LatestMessageRow

            val intent = Intent(activity, ChatLogActivity::class.java)
            intent.putExtra(USER_KEY, row.user)
            startActivity(intent)
        }

        fetchLatestMessages()

        //Kullanıcı Giriş Yapmamış ise onu LoginActivity e geri atar. Ve Geri dönemez.
        if (mUser.uid.isEmpty()) {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        messages_searchview.setOnQueryTextListener(this)
    }

    private fun fetchLatestMessages() {
        val uid = mUser.uid

        Log.d(logTAG, "UID : $uid")
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$uid")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                adapter.clear()

                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return

                val chatPartnerId = if (chatMessage.fromId == uid) {
                    chatMessage.toId
                } else {
                    chatMessage.fromId
                }
                val ref2 = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")

                ref2.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        Log.d(logTAG, chatPartnerId)

                        val user = p0.getValue(User::class.java)
                        if (user!!.username.contains(searchText)) {
                            adapter.add(LatestMessageRow(chatMessage, user))
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Log.d(logTAG, "There is a problem while fetching User Info : ${p0.message}")
                    }
                })
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                adapter.clear()
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return

                latestMessagesMap[p0.key!!] = chatMessage
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }
}