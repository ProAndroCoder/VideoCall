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

        tabLayout.getTabAt(0)?.text = "All Users"
        tabLayout.getTabAt(1)?.text = "Followers"
        tabLayout.addTab(tabLayout.newTab().setText("Followed"))

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
                        fetchAll()
                    }

                    1 -> {
                        fetchFollowers()
                    }

                    2 -> {
                        fetchFolloweds()
                    }
                }
            }
        })
    }

    //Fetching all users which contains searchtext value
    private fun fetchAll() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    Log.d(logTAG, "User Info : $it")
                    val user = it.getValue(User::class.java)

                    if (user != null && user.uid != mUser.uid && user.username.contains(searchText)) {
                        adapter.add(UserItem(user, 1, mUser, activity))
                        recyclerview_messages.adapter = adapter
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

    }

    //Fetching all followed users which contains searchtext value
    private fun fetchFolloweds() {

    }
}
