package com.qatasoft.videocall

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.qatasoft.videocall.videoCallRequests.GetVideoRequest
import com.qatasoft.videocall.videoCallRequests.SendVideoRequest
import com.qatasoft.videocall.models.User

class BackgroundService : Service() {
    companion object {
        val TAG = "BackgroundService"
        val uid = FirebaseAuth.getInstance().uid
    }


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Background Log")

        val ref = FirebaseDatabase.getInstance().getReference("/videorequests/$uid")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach() {
                    val user = it.getValue(User::class.java)
                    Log.d(TAG, "User Info")


                    if (user != null && user.uid != uid) {
                        Log.d(TAG, "Getting")

                        val intent = Intent(applicationContext, GetVideoRequest::class.java)
                        intent.putExtra(SendVideoRequest.TEMP_TOKEN, user)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "There is an error while fetching user datas.")
            }
        })

//        val ref = FirebaseDatabase.getInstance().getReference("/videorequests")
//        ref.addChildEventListener(object : ChildEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//                Log.d(TAG, "Child Cancelled")
//            }
//
//            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
//                Log.d(TAG, "Child Moved")
//            }
//
//            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
//                Toast.makeText(applicationContext,"Child Changed",Toast.LENGTH_LONG).show()
//            }
//
//            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//                Toast.makeText(applicationContext,"Child Added",Toast.LENGTH_LONG).show()
//
//            }
//
//            override fun onChildRemoved(p0: DataSnapshot) {
//                Toast.makeText(applicationContext,"Child Removed",Toast.LENGTH_LONG).show()
//
//            }
//
//        })


    }
}
