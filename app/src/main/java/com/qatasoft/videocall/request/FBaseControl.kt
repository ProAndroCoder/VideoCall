package com.qatasoft.videocall.request

import android.util.Log
import com.google.firebase.database.*
import com.qatasoft.videocall.MainActivity.Companion.mUser
import com.qatasoft.videocall.models.ChatMessage
import com.qatasoft.videocall.models.Tools
import com.qatasoft.videocall.models.User

class FBaseControl {
    private val logTag = "FirebaseControlLogs"

    fun performSendMessage(chatMessage: ChatMessage, isAttachment: Boolean): Boolean {
        val mDatabase = FirebaseDatabase.getInstance().getReference("/user-messages")
        val lDatabase = FirebaseDatabase.getInstance().getReference("/latest-messages")

        var isSend = true

        try {
            //From Messages
            val fromRef = mDatabase.child(chatMessage.fromId).child(chatMessage.toId)
            val latestFromRef = lDatabase.child(chatMessage.fromId).child(chatMessage.toId)

            val key = if (chatMessage.refKey.isEmpty()) fromRef.push().key else chatMessage.refKey

            if (key == null) {
                Log.d(logTag, "Reference key error")
                return false
            } else {
                chatMessage.refKey = key
                Log.d(logTag, "From Key : $key")
            }

            fromRef.child(key).setValue(chatMessage).addOnFailureListener { isSend = false }
            latestFromRef.setValue(chatMessage).addOnFailureListener { isSend = false }

            //If message an attachment then first time do not send toRef to Firebase when attachment sends then we send toRef too
            if (!isAttachment) {
                //To Messages
                val tempFileUri = chatMessage.fileUri
                chatMessage.fileUri = ""
                val toRef = mDatabase.child(chatMessage.toId).child(chatMessage.fromId)
                val latestToRef = lDatabase.child(chatMessage.toId).child(chatMessage.fromId)

                toRef.child(key).setValue(chatMessage).addOnFailureListener { isSend = false }
                latestToRef.setValue(chatMessage).addOnFailureListener { isSend = false }

                chatMessage.fileUri = tempFileUri
            }
        } catch (e: Exception) {
            Log.d(logTag, e.toString())
            isSend = false
        }
        return isSend
    }

    //Fetching user according to searchtext value
    fun fetchUsers(path: String, data: ArrayList<User>, searchText: String = ""): ArrayList<User> {
        val allRef = FirebaseDatabase.getInstance().getReference(path)

        allRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                data.clear()

                p0.children.forEach {
                    val user = it.getValue(User::class.java)

                    if (user != null && user.uid != mUser.uid && user.username.contains(searchText)) {
                        data.add(user)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(logTag, "FetchUser Error")
            }
        })

        return data
    }

    fun followedOperations(mUser: User, user: User, type: String): Boolean {
        var isSuccess = true

        val followed = FirebaseDatabase.getInstance().getReference("/friends/${mUser.uid}/followeds/${user.uid}")

        val follower = FirebaseDatabase.getInstance().getReference("/friends/${user.uid}/followers/${mUser.uid}")

        when (type) {
            Tools.removeFollowed -> {
                followed.removeValue()
                        .addOnFailureListener {
                            isSuccess = false
                        }

                follower.removeValue()
                        .addOnFailureListener {
                            isSuccess = false
                        }
            }
            Tools.addFollowed -> {
                followed.setValue(user)
                        .addOnFailureListener {
                            isSuccess = false
                        }

                follower.setValue(mUser)
                        .addOnFailureListener {
                            isSuccess = false
                        }
            }
        }
        return isSuccess
    }

    fun callRequestOperations(fromId: String, toId: String, type: String, path: String): Boolean {
        var isSend = true

        val sendingTime = Tools.getSendingTime()

        val fromRef = FirebaseDatabase.getInstance().getReference("/$path/$fromId/$toId")
        val toRef = FirebaseDatabase.getInstance().getReference("/$path/$toId/$fromId")

        val refKey = fromRef.key
        if (refKey == null) {
            Log.d(logTag, "callReqOp refkey null")
            return false
        } else {
            when (type) {
                Tools.addRequestLog -> {
                    val chatMessage = ChatMessage("OutGoing Call", fromId, toId, sendingTime, "", "", "", "", refKey)

                    fromRef.setValue(chatMessage).addOnFailureListener {
                        isSend = false
                    }

                    chatMessage.text = "Incoming Call"

                    toRef.setValue(chatMessage).addOnFailureListener {
                        isSend = false
                    }
                }

                Tools.removeRequestLog -> {
                    fromRef.removeValue().addOnFailureListener { isSend = false }
                    toRef.removeValue().addOnFailureListener { isSend = false }
                }

                Tools.addRequest -> {
                    toRef.setValue(mUser)
                }

                Tools.removeRequest -> {
                    toRef.removeValue()
                }
            }
        }
        if (!isSend) {
            Log.d(logTag, "callReqOp Problem false? Failure")
        }
        return isSend
    }
}
