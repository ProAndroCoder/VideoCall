package com.qatasoft.videocall

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log


class SensorRestarterBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("BroadcastReceiver", "Service Stops! Oops!!!!")
        context.startService(Intent(context, BackgroundService::class.java))

    }
}