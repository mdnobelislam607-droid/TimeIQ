package com.timeiq.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val limit = context.getSharedPreferences("timeiq", Context.MODE_PRIVATE)
            .getInt("limit_min", 0)
        if (limit <= 0) return
        try {
            val i = Intent(context, MonitorService::class.java)
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        } catch (e: Exception) {
        }
    }
}
