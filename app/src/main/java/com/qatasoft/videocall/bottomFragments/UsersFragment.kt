package com.qatasoft.videocall.bottomFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.android.synthetic.main.fragment_messages.*

class UsersFragment : Fragment(), SearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onQueryTextChange(newText: String?): Boolean {
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
        }
        return true
    }

    //Arraylist All, Followers and Followeds
    var all = ArrayList<User>()
    var followers = ArrayList<User>()
    var followeds = ArrayList<User>()


    var tabIndex = 0
    val logTAG = "UsersFragment"
    var searchText = ""

    var allAdapter: UserItem? = null
    var followersAdapter: UserItem? = null
    var followedsAdapter: UserItem? = null

    var mUser = User("", "", "", "", "", "", "", false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val manager = activity!!.supportFragmentManager

        //Adapters All, Followers, Followeds
        allAdapter = UserItem(all, 0, MainActivity.mUser, manager,context!!)
        followersAdapter = UserItem(followers, 0, MainActivity.mUser, manager,context!!)
        followedsAdapter = UserItem(followeds, 1, MainActivity.mUser, manager,context!!)

        mUser = MainActivity.mUser

        recycler_message_user.layoutManager = LinearLayoutManager(this.context)
        recycler_message_user.adapter = allAdapter

        tabLayout.getTabAt(0)?.text = "All Users"
        tabLayout.getTabAt(1)?.text = "Followers"
        tabLayout.addTab(tabLayout.newTab().setText("Followeds"))

        fetchAll()

        messages_searchview.setOnQueryTextListener(this)

        tabOnSelect()
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

    //Fetching all users which contains searchtext value
    private fun fetchAll() {
        fetchFolloweds()

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
                allAdapter?.notifyDataSetChanged()

            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(logTAG, "There is an error while fetching user datas.")
            }
        })
    }

    //Fetching all follower users which contains searchtext value
    private fun fetchFollowers() {
        fetchFolloweds()

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
                followersAdapter?.notifyDataSetChanged()

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
                followedsAdapter?.notifyDataSetChanged()

            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(logTAG, "There is an error while fetching user datas.")
            }
        })
    }

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }
}