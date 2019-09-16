package com.qatasoft.videocall

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.qatasoft.videocall.VideoCallRequest.GetVideoRequest
import com.qatasoft.videocall.VideoCallRequest.SendVideoRequest
import com.qatasoft.videocall.models.User

class BackgroundService : Service() {
    companion object{
        val TAG="BackgroundService"
        val uid=FirebaseAuth.getInstance().uid
    }





    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {

        super.onCreate()
        StopTheTask.isStop=false
        Log.d("RunBackground", "Service started")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId);
        if(!StopTheTask.isStop){
            onTaskRemoved(intent)

            Log.d(TAG,"BAckv Uses")

            val ref = FirebaseDatabase.getInstance().getReference("/videorequests/$uid")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    p0.children.forEach() {
                        val user = it.getValue(User::class.java)

                        if (user != null && user.uid != uid) {
                            stopService(Intent(applicationContext,BackgroundService::class.java))
                            StopTheTask.isStop=true

                            val intent=Intent(applicationContext,GetVideoRequest::class.java)
                            intent.putExtra(SendVideoRequest.TEMP_TOKEN,user)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.d(TAG, "There is an error while fetching user datas.")
                }
            })
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        StopTheTask.isStop=false
        val broadcastIntent = Intent("ac.in.ActivityRecognition.RestartSensor")
        sendBroadcast(broadcastIntent)
        //StopTheTask.isStop=true
        Log.d("RunBackground", "Service stopped")
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }
}

class StopTheTask{
    companion object{
        var isStop=false
    }
}
