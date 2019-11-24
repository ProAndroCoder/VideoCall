package com.qatasoft.videocall.bottomFragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.tabs.TabLayout
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
            users.clear()
            if (tabIndex == 0) {
                adapter.clear()
                searchText = newText.toString()
                fetchInfo("latest-messages")
            } else if (tabIndex == 1) {
                adapter.clear()
                searchText = newText.toString()
                fetchInfo("latest-calls")
            }
        }
        return true
    }

    companion object {
        const val logTAG = "MessagesFragment"
        const val USER_KEY = "USER_INFO_KEY"
    }

    private var searchText = ""
    var tabIndex = 0
    var users = ArrayList<String>()

    private val adapter = GroupAdapter<ViewHolder>()
    private val mUser = MainActivity.mUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (HomeFragment.isMessage) {
            tabLayout.getTabAt(0)?.select()
            fetchInfo("latest-messages")
        } else {
            tabLayout.getTabAt(1)?.select()
            fetchInfo("latest-calls")
        }

        recycler_message_user.adapter = adapter

        //Itemlar arasında ayıraç konuluyor
        recycler_message_user.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, _ ->
            val row = item as LatestMessageRow

            val intent = Intent(activity, ChatLogActivity::class.java)
            intent.putExtra(USER_KEY, row.user)
            startActivity(intent)
        }


        //Kullanıcı Giriş Yapmamış ise onu LoginActivity e geri atar. Ve Geri dönemez.
        if (mUser.uid.isEmpty()) {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        messages_searchview.setOnQueryTextListener(this)


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {

                Log.d(logTAG, tab?.position.toString())
                adapter.clear()
                when (tab?.position) {
                    0 -> {
                        tabIndex = 0
                        fetchInfo("latest-messages")
                    }

                    1 -> {
                        tabIndex = 1
                        fetchInfo("latest-calls")
                    }
                }
            }
        })

    }

    private fun fetchInfo(type: String) {
        adapter.clear()

        val ref = FirebaseDatabase.getInstance().getReference("/$type/${mUser.uid}")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val data = p0.getValue(ChatMessage::class.java) ?: return

                Log.d(logTAG, "fetchInfo username : ${data.id}")

                val chatPartnerId = if (data.fromId == mUser.uid) {
                    data.toId
                } else {
                    data.fromId
                }

                if (users.indexOf(chatPartnerId) < 0) {
                    users.add(chatPartnerId)
                    fetchUserInfo(data, chatPartnerId)
                }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val data = p0.getValue(ChatMessage::class.java) ?: return

                Log.d(logTAG, "fetchInfo username : ${data.id}")

                val chatPartnerId = if (data.fromId == mUser.uid) {
                    data.toId
                } else {
                    data.fromId
                }

                if (users.indexOf(chatPartnerId) < 0) {
                    users.add(chatPartnerId)
                    fetchUserInfo(data, chatPartnerId)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    fun fetchUserInfo(data: ChatMessage, chatPartnerId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(User::class.java)

                Log.d(logTAG, "  Size : " + users.size)

                if (user!!.username.contains(searchText) && users.indexOf(chatPartnerId) >= 0) {
                    Log.d(logTAG, user.username)
                    users.remove(chatPartnerId)
                    adapter.add(LatestMessageRow(data, user))
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(logTAG, "There is a problem while fetching User Info : ${p0.message}")
            }
        })

    }
}