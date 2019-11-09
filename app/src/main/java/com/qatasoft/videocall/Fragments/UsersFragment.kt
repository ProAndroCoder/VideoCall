package com.qatasoft.videocall.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.qatasoft.videocall.MainActivity

import com.qatasoft.videocall.R
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.views.UserItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_messages.*

class UsersFragment : Fragment(), SearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            adapter.clear()
            searchText = newText.toString()
            fetchAll()
        }
        return true
    }

    var followedsAdapter = ArrayList<User>()
    var followersAdapter = ArrayList<User>()
    var allAdapter = ArrayList<User>()
    var tabIndex = 0
    val logTAG = "UsersFragment"
    var searchText = ""
    val adapter = GroupAdapter<ViewHolder>()

    var mUser = User("", "", "", "", "", "")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mUser = MainActivity.mUser

        recyclerview_messages.adapter = adapter

        tabLayout.getTabAt(0)?.text = "All Users"
        tabLayout.getTabAt(1)?.text = "Followers"
        tabLayout.addTab(tabLayout.newTab().setText("Followeds"))

        fetchAll()

        messages_searchview.setOnQueryTextListener(this)

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {

                Log.d(logTAG, tab?.position.toString())
                when (tab?.position) {
                    0 -> {
                        adapter.clear()
                        followedsAdapter.clear()
                        tabIndex = 0
                        fetchAll()
                    }

                    1 -> {
                        adapter.clear()
                        tabIndex = 1
                        fetchFollowers()
                    }

                    2 -> {
                        adapter.clear()
                        tabIndex = 2
                        fetchFolloweds()
                    }
                }
            }
        })
    }

    //Fetching all users which contains searchtext value
    private fun fetchAll() {
        fetchFolloweds()

        val all = FirebaseDatabase.getInstance().getReference("/users")

        all.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    val user = it.getValue(User::class.java)

                    if (user != null && user.uid != mUser.uid && user.username.contains(searchText)) {
                        var isFollowed = 0
                        followedsAdapter.forEach { it1 ->
                            if (it1.uid == user.uid) {
                                isFollowed = 1
                            }
                        }

                        adapter.add(UserItem(user, isFollowed, mUser, context))
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(logTAG, "There is an error while fetching user datas.")
            }
        })
    }

    //Fetching all follower users which contains searchtext value
    private fun fetchFollowers() {
        fetchFolloweds()

        val follower = FirebaseDatabase.getInstance().getReference("/friends/${mUser.uid}/followers")
        follower.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    val user = it.getValue(User::class.java)

                    if (user != null && user.uid != mUser.uid && user.username.contains(searchText)) {
                        var isFollowed = 0
                        followedsAdapter.forEach { it1 ->
                            if (it1.uid == user.uid) {
                                isFollowed = 1
                            }
                        }

                        adapter.add(UserItem(user, isFollowed, mUser, context))
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(logTAG, "There is an error while fetching user datas.")
            }
        })
    }

    //Fetching all followed users which contains searchtext value
    private fun fetchFolloweds() {
        val followed = FirebaseDatabase.getInstance().getReference("/friends/${mUser.uid}/followeds")
        followed.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    val user = it.getValue(User::class.java)

                    if (user != null && user.uid != mUser.uid && user.username.contains(searchText)) {
                        if (tabIndex == 2) {
                            adapter.add(UserItem(user, 2, mUser, context))
                        } else {
                            followedsAdapter.add(user)
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(logTAG, "There is an error while fetching user datas.")
            }
        })
    }
}
