package com.qatasoft.videocall.Fragments


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
class FriendsFragment : Fragment() {
    companion object {
        var TAG = "FriendsActivity"
    }

    var mUser = User("", "", "", "")
    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val myPreference = MyPreference(activity)
        mUser = myPreference.getUserInfo()

        getFriends()

        // Inflate the layout for this fragment
        return view
    }

    fun getFriends() {
        Log.d(TAG, "My User Id : ${mUser.uid}")
        val ref = FirebaseDatabase.getInstance().getReference("/friends/${mUser.uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach() {
                    val user = it.getValue(User::class.java)

                    Log.d(TAG, "User Info : ${user?.username}")
                    if (user != null && user.uid != FirebaseAuth.getInstance().uid) {
                        adapter.add(UserItem(user, 3, mUser, activity))
                    }
                }

                Log.d(TAG, "Logme : " + adapter.getItemCount().toString())

                view?.recyclerview_home?.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "There is an error while fetching user datas.")
            }
        })
    }
}
