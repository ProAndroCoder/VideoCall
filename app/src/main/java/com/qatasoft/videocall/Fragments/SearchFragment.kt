package com.qatasoft.videocall.Fragments


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.qatasoft.videocall.MyPreference
import com.qatasoft.videocall.R
import com.qatasoft.videocall.messages.ChatLogActivity
import com.qatasoft.videocall.models.User
import com.qatasoft.videocall.views.LatestMessageRow
import com.qatasoft.videocall.views.UserItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_home.view.recyclerview_home
import kotlinx.android.synthetic.main.fragment_search.view.*


class SearchFragment : Fragment(), SearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            if (newText.isNotEmpty()) {
                adapter.clear()
                searchText = newText.toString()
                fetchUsers()
            } else {
                adapter.clear()
            }
        }
        return true
    }

    var TAG = "SearchActivity"
    var searchText = ""
    val adapter = GroupAdapter<ViewHolder>()

    var mUser = User("", "", "", "", "", "")

    //Companian Object sayesinde burada tanımlanan değerler diğer activityler tarafından da okunabilir
    companion object {
        val USER_KEY = "USER_KEY"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val searchview = view.searchview_et

        val myPreference = MyPreference(activity)
        mUser = myPreference.getUserInfo()


        searchview.setOnQueryTextListener(this)

        adapter.setOnItemClickListener { item, _ ->
            val row = item as UserItem

            val intent = Intent(activity, ChatLogActivity::class.java)
            intent.putExtra(NewMessageFragment.USER_KEY, row.user)
            startActivity(intent)
        }

        // Inflate the layout for this fragment
        return view
    }

    //Firebase Veritabanımızdan bilgileri çekme işlemini yapan metod
    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    Log.d(TAG, "User Info : $it")
                    val user = it.getValue(User::class.java)

                    if (user != null) {
                        if (user.uid != FirebaseAuth.getInstance().uid && user.username.contains(searchText)) {
                            adapter.add(UserItem(user, 1, mUser, activity))
                        }
                    }
                }
                if (searchText.isNotEmpty()) {
                    view?.recyclerview_home?.adapter = adapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "There is an error while fetching user datas.")
            }
        })
    }


}
