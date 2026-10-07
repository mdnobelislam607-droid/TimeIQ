package com.timeiq.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import java.util.Calendar

class MonitorService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var warnedDay = -1
    private var limitDay = -1

    private val tick = object : Runnable {
        override fun run() {
            check()
            handler.postDelayed(this, 30000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel("monitor", "TimeIQ monitor", NotificationManager.IMPORTANCE_MIN)
            )
            nm.createNotificationChannel(
                NotificationChannel("alerts", "TimeIQ alerts", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(this, "monitor")
            .setContentTitle("TimeIQ is watching your screen time")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentIntent(open)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, n)
        }

        handler.removeCallbacks(tick)
        handler.post(tick)
        return START_STICKY
    }

    private fun check() {
        val prefs = getSharedPreferences("timeiq", MODE_PRIVATE)
        val limitMin = prefs.getInt("limit_min", 0)
        if (limitMin <= 0) return

        val usedMs = Usage.todayTotalMs(this)
        val limitMs = limitMin * 60000L
        val remaining = limitMs - usedMs
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        if (remaining in 1..60000L && warnedDay != today) {
            warnedDay = today
            notify(2, "1 minute left", "You will reach your daily limit in 1 minute.")
        }
        if (remaining <= 0L && limitDay != today) {
            limitDay = today
            notify(3, "Daily limit reached", "You have used your TimeIQ daily limit.")
        }
    }

    private fun notify(id: Int, title: String, text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(this, "alerts")
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        nm.notify(id, n)
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

object Usage {
    fun todayTotalMs(ctx: Context): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = usm.queryAndAggregateUsageStats(cal.timeInMillis, System.currentTimeMillis())
        val pm = ctx.packageManager
        var sum = 0L
        for ((pkg, s) in stats) {
            if (pkg == ctx.packageName) continue
            if (pm.getLaunchIntentForPackage(pkg) == null) continue
            sum += s.totalTimeInForeground
        }
        return sum
    }
}
