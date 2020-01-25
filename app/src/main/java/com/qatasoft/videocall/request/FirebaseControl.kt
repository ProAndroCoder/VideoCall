package com.qatasoft.videocall.request

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.qatasoft.videocall.messages.ChatLogActivity
import com.qatasoft.videocall.models.ChatMessage

class FirebaseControl {
    private val logTag = "FirebaseControlLogs"

    private lateinit var mDatabase: DatabaseReference
    private lateinit var lDatabase: DatabaseReference

    fun performSendMessage(chatMessage: ChatMessage, isAttachment: Boolean): Boolean {
        mDatabase = FirebaseDatabase.getInstance().getReference("/user-messages")
        lDatabase = FirebaseDatabase.getInstance().getReference("/latest-messages")

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
                val toRef = mDatabase.child(chatMessage.toId).child(chatMessage.fromId)
                val latestToRef = lDatabase.child(chatMessage.toId).child(chatMessage.fromId)

                toRef.child(key).setValue(chatMessage).addOnFailureListener { isSend = false }
                latestToRef.setValue(chatMessage).addOnFailureListener { isSend = false }
            }
        } catch (e: Exception) {
            Log.d(logTag, e.toString())
            isSend = false
        }
        return isSend
    }
}
