package com.timeiq.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class BlockerService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        val prefs = getSharedPreferences("timeiq", MODE_PRIVATE)
        val blocked = prefs.getStringSet("blocked", emptySet()) ?: emptySet()
        if (!blocked.contains(pkg)) return

        val limitMin = prefs.getInt("limit_min", 0)
        if (limitMin <= 0) return

        val until = prefs.getLong("unlock_until", 0L)
        if (System.currentTimeMillis() < until) return

        val used = Usage.todayTotalMs(this)
        if (used < limitMin * 60000L) return

        val i = Intent(this, BlockActivity::class.java)
        i.putExtra("pkg", pkg)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(i)
    }

    override fun onInterrupt() {}
}
