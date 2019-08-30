package com.qatasoft.videocall

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class BackgroundService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {

        Log.d("RunBackground", "Service started")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("RunBackground", StopTheTask.isStop.toString())
        if(!StopTheTask.isStop){
            onTaskRemoved(intent)

            Log.d("RunBackground", "Hello")
            Log.d("RunBackground", "Hellongfbgdfbgdgbdr")
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        StopTheTask.isStop=true
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
