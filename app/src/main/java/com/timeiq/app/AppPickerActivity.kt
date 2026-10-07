package com.timeiq.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AppPickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bg = Color.parseColor("#0F172A")
        val textMain = Color.parseColor("#F1F5F9")
        val accent = Color.parseColor("#38BDF8")
        window.statusBarColor = bg
        window.navigationBarColor = bg
        val d = resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()

        val prefs = getSharedPreferences("timeiq", MODE_PRIVATE)
        val chosen = HashSet(prefs.getStringSet("blocked", emptySet()) ?: emptySet())

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(bg)
        root.setPadding(dp(20), dp(40), dp(20), dp(24))

        val title = TextView(this)
        title.text = "Select apps to control"
        title.textSize = 22f
        title.setTextColor(accent)
        title.setTypeface(null, Typeface.BOLD)
        title.setPadding(0, 0, 0, dp(16))
        root.addView(title)

        val launch = Intent(Intent.ACTION_MAIN)
        launch.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = packageManager.queryIntentActivities(launch, 0)
            .map { it.activityInfo.packageName to it.loadLabel(packageManager).toString() }
            .filter { it.first != packageName }
            .distinctBy { it.first }
            .sortedBy { it.second.lowercase() }

        for ((pkg, name) in apps) {
            val cb = CheckBox(this)
            cb.text = name
            cb.textSize = 16f
            cb.setTextColor(textMain)
            cb.isChecked = chosen.contains(pkg)
            cb.setPadding(dp(8), dp(12), dp(8), dp(12))
            cb.setOnCheckedChangeListener { _, on ->
                if (on) chosen.add(pkg) else chosen.remove(pkg)
                prefs.edit().putStringSet("blocked", HashSet(chosen)).apply()
            }
            root.addView(cb)
        }

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)
        scroll.addView(root)
        setContentView(scroll)
    }
}
