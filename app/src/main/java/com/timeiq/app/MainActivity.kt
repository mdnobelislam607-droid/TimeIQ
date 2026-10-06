package com.timeiq.app

import android.app.AppOpsManager
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(48, 96, 48, 48)

        val title = TextView(this)
        title.text = "TimeIQ"
        title.textSize = 28f

        status = TextView(this)
        status.textSize = 18f
        status.setPadding(0, 48, 0, 48)

        val button = Button(this)
        button.text = "Open Usage Access settings"
        button.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(button)
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        if (hasUsageAccess()) {
            status.text = "Usage Access: GRANTED"
        } else {
            status.text = "Usage Access: NOT GRANTED"
        }
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
