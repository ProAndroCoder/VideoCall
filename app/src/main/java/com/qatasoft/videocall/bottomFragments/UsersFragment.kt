package com.qatasoft.videocall.bottomFragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.qatasoft.videocall.MainActivity.Companion.mUser
import com.qatasoft.videocall.R
import com.qatasoft.videocall.models.Tools
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.request.FBaseControl
import com.qatasoft.videocall.views.UserItem
import kotlinx.android.synthetic.main.fragment_messages.*

class UsersFragment : Fragment(), SearchView.OnQueryTextListener {
    var all = ArrayList<User>()
    var followers = ArrayList<User>()
    var followeds = ArrayList<User>()

    private val allText = "All Users"
    private val followersText = "Followers"
    private val followedsText = "Followeds"

    var tabIndex = 0
    val logTAG = "UsersFragmentLog"
    var searchText = ""

    private lateinit var allAdapter: UserItem
    private lateinit var followersAdapter: UserItem
    private lateinit var followedsAdapter: UserItem

    private val fBaseControl = FBaseControl()

    private val allPath = "/users"
    val followersPath = "/friends/${mUser.uid}/followers"
    private val followedsPath = "/friends/${mUser.uid}/followeds"

    private val handler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getInfo()

        messages_searchview.setOnQueryTextListener(this)

        tabOnSelect()
    }

    private fun getInfo() {
        val manager = activity!!.supportFragmentManager

        //Adapters All, Followers, Followeds
        allAdapter = UserItem(all, Tools.userAll, mUser, manager, context!!)
        followersAdapter = UserItem(followers, Tools.userFollower, mUser, manager, context!!)
        followedsAdapter = UserItem(followeds, Tools.userFollowed, mUser, manager, context!!)

        recycler_message_user.layoutManager = LinearLayoutManager(this.context)
        recycler_message_user.adapter = allAdapter

        //Change name of tabs and create one more tab for followeds
        tabLayout.getTabAt(0)?.text = allText
        tabLayout.getTabAt(1)?.text = followersText
        tabLayout.addTab(tabLayout.newTab().setText(followedsText))

        //fetchAllUsers()
        fetchAll()
    }

    private fun fetchAllUsers() {
        all.clear()
        all = fBaseControl.fetchUsers(allPath, all, searchText)

        followeds.clear()
        followeds = fBaseControl.fetchUsers(followedsPath, followeds, searchText)

        //Check which user is followeds by user
        all.forEach { all ->
            followeds.forEach { followed ->
                if (followed.uid == all.uid) {
                    all.isFollowed = true
                }
            }
            Log.d(logTAG, all.username + " ${all.isFollowed} ${followeds.size}")
        }

        Log.d(logTAG, "Size Info:  ${all.size} ${followeds.size}")

        allAdapter.notifyDataSetChanged()

        //handler.postDelayed({

        //}, 500)
    }

    //Fetching all users which contains searchtext value
    private fun fetchAll() {
        followeds = fBaseControl.fetchUsers(followedsPath, followeds, searchText)
        //fetchFolloweds()

        Log.d(logTAG, "Followeds Size : ${followeds.size}")

        val allRef = FirebaseDatabase.getInstance().getReference("/users")

        allRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                all.clear()

                p0.children.forEach {
                    val user = it.getValue(User::class.java)

                    if (user != null && user.uid != mUser.uid && user.username.contains(searchText)) {
                        followeds.forEach { it1 ->
                            if (it1.uid == user.uid) {
                                user.isFollowed = true
                            }
                        }

                        all.add(user)
                        Log.d(logTAG, "All : ${user.username}  ${all.size}")
                    }
                }
                allAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(logTAG, "There is an error while fetching user datas.")
            }
        })
    }

    //Fetching all follower users which contains searchtext value
    private fun fetchFollowers() {
        followeds = fBaseControl.fetchUsers(followedsPath, followeds, searchText)
        //fetchFolloweds()

        val followerRef = FirebaseDatabase.getInstance().getReference("/friends/${mUser.uid}/followers")

        followerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                followers.clear()

                p0.children.forEach {
                    val user = it.getValue(User::class.java)

                    if (user != null && user.uid != mUser.uid && user.username.contains(searchText)) {
                        followeds.forEach { it1 ->
                            if (it1.uid == user.uid) {
                                user.isFollowed = true
                            }
                        }
                        Log.d(logTAG, "Follower : ${user.username}  ${followers.size}")

                        followers.add(user)
                    }
                }
                followersAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(logTAG, "There is an error while fetching user datas.")
            }
        })
    }

    //Fetching all followed users which contains searchtext value
    private fun fetchFolloweds() {
        val followedRef = FirebaseDatabase.getInstance().getReference("/friends/${mUser.uid}/followeds")
        followedRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                followeds.clear()

                p0.children.forEach {
                    val user = it.getValue(User::class.java)

                    if (user != null && user.uid != mUser.uid && user.username.contains(searchText)) {
                        followeds.add(user)
                        Log.d(logTAG, "Followed : ${user.username}  ${followeds.size}")
                    }
                }
                followedsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(logTAG, "There is an error while fetching user datas.")
            }
        })
    }

    private fun searchUsersFromFBase(newText: String?): Boolean {
        if (newText != null) {
            when (tabIndex) {
                0 -> {
                    searchText = newText.toString()
                    fetchAll()
                }

                1 -> {
                    searchText = newText.toString()
                    fetchFollowers()
                }

                2 -> {
                    searchText = newText.toString()
                    fetchFolloweds()
                }
            }
            return true
        }
        return false
    }

    private fun tabOnSelect() {
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {

                Log.d(logTAG, tab?.position.toString())
                when (tab?.position) {
                    0 -> {
                        tabIndex = 0
                        recycler_message_user.adapter = allAdapter
                        fetchAll()
                    }

                    1 -> {
                        tabIndex = 1
                        recycler_message_user.adapter = followersAdapter
                        fetchFollowers()
                    }

                    2 -> {
                        tabIndex = 2
                        recycler_message_user.adapter = followedsAdapter
                        fetchFolloweds()
                    }
                }
            }
        })
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return searchUsersFromFBase(query)
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return searchUsersFromFBase(newText)
    }
}