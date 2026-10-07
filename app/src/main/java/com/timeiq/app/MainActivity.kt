package com.timeiq.app

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private lateinit var total: TextView
    private lateinit var listBox: LinearLayout
    private lateinit var button: Button

    private val bg = Color.parseColor("#0F172A")
    private val card = Color.parseColor("#1E293B")
    private val accent = Color.parseColor("#38BDF8")
    private val textMain = Color.parseColor("#F1F5F9")
    private val textDim = Color.parseColor("#94A3B8")

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun cardBg(): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(card)
        g.cornerRadius = dp(16).toFloat()
        return g
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(bg)
        root.setPadding(dp(20), dp(40), dp(20), dp(24))

        val title = TextView(this)
        title.text = "TimeIQ"
        title.textSize = 30f
        title.setTextColor(accent)
        title.setTypeface(null, android.graphics.Typeface.BOLD)

        val sub = TextView(this)
        sub.text = "Your screen time, under control"
        sub.textSize = 14f
        sub.setTextColor(textDim)
        sub.setPadding(0, dp(2), 0, dp(20))

        val totalCard = LinearLayout(this)
        totalCard.orientation = LinearLayout.VERTICAL
        totalCard.background = cardBg()
        totalCard.setPadding(dp(20), dp(20), dp(20), dp(20))

        val totalLabel = TextView(this)
        totalLabel.text = "TODAY"
        totalLabel.textSize = 12f
        totalLabel.setTextColor(textDim)

        total = TextView(this)
        total.textSize = 38f
        total.setTextColor(textMain)
        total.setTypeface(null, android.graphics.Typeface.BOLD)
        total.text = "--"

        status = TextView(this)
        status.textSize = 13f
        status.setTextColor(textDim)

        totalCard.addView(totalLabel)
        totalCard.addView(total)
        totalCard.addView(status)

        button = Button(this)
        button.text = "Grant Usage Access"
        button.setTextColor(bg)
        val bb = GradientDrawable()
        bb.setColor(accent)
        bb.cornerRadius = dp(12).toFloat()
        button.background = bb
        button.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        val bp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(52)
        )
        bp.topMargin = dp(16)
        button.layoutParams = bp

        val appsTitle = TextView(this)
        appsTitle.text = "Apps used today"
        appsTitle.textSize = 16f
        appsTitle.setTextColor(textMain)
        appsTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        appsTitle.setPadding(0, dp(24), 0, dp(10))

        listBox = LinearLayout(this)
        listBox.orientation = LinearLayout.VERTICAL

        root.addView(title)
        root.addView(sub)
        root.addView(totalCard)
        root.addView(button)
        root.addView(appsTitle)
        root.addView(listBox)

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)
        scroll.addView(root)
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        if (hasUsageAccess()) {
            status.text = "Usage Access: granted"
            button.visibility = View.GONE
            showUsage()
        } else {
            status.text = "Usage Access: not granted"
            button.visibility = View.VISIBLE
            total.text = "--"
            listBox.removeAllViews()
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

    private fun showUsage() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = System.currentTimeMillis()

        val usm = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = usm.queryAndAggregateUsageStats(start, end)

        val pm = packageManager
        val rows = ArrayList<Pair<String, Long>>()
        var sum = 0L

        for ((pkg, s) in stats) {
            val ms = s.totalTimeInForeground
            if (ms < 60000L) continue
            if (pkg == packageName) continue
            if (pm.getLaunchIntentForPackage(pkg) == null) continue
            val name = try {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                pkg
            }
            rows.add(Pair(name, ms))
            sum += ms
        }

        rows.sortByDescending { it.second }
        total.text = format(sum)

        listBox.removeAllViews()
        if (rows.isEmpty()) {
            val empty = TextView(this)
            empty.text = "No usage recorded yet today."
            empty.setTextColor(textDim)
            listBox.addView(empty)
            return
        }

        for (r in rows) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.background = cardBg()
            row.setPadding(dp(16), dp(14), dp(16), dp(14))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = dp(8)
            row.layoutParams = lp

            val n = TextView(this)
            n.text = r.first
            n.textSize = 15f
            n.setTextColor(textMain)
            n.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val t = TextView(this)
            t.text = format(r.second)
            t.textSize = 15f
            t.setTextColor(accent)

            row.addView(n)
            row.addView(t)
            listBox.addView(row)
        }
    }

    private fun format(ms: Long): String {
        val minutes = ms / 60000L
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}
