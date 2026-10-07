package com.timeiq.app

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private lateinit var total: TextView
    private lateinit var listBox: LinearLayout
    private lateinit var button: Button
    private lateinit var limitInfo: TextView
    private lateinit var progress: ProgressBar
    private lateinit var hoursInput: EditText
    private lateinit var minutesInput: EditText

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

    private fun inputBg(): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(bg)
        g.cornerRadius = dp(10).toFloat()
        return g
    }

    private fun makeInput(hint: String): EditText {
        val e = EditText(this)
        e.hint = hint
        e.inputType = InputType.TYPE_CLASS_NUMBER
        e.setTextColor(textMain)
        e.setHintTextColor(textDim)
        e.background = inputBg()
        e.setPadding(dp(14), dp(10), dp(14), dp(10))
        e.gravity = Gravity.CENTER
        val lp = LinearLayout.LayoutParams(0, dp(48), 1f)
        lp.marginEnd = dp(8)
        e.layoutParams = lp
        return e
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
        title.setTypeface(null, Typeface.BOLD)

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
        total.setTypeface(null, Typeface.BOLD)
        total.text = "--"

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        progress.max = 100
        progress.progressDrawable.setTint(accent)
        val pp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(8))
        pp.topMargin = dp(10)
        progress.layoutParams = pp

        limitInfo = TextView(this)
        limitInfo.textSize = 13f
        limitInfo.setTextColor(textDim)
        limitInfo.setPadding(0, dp(8), 0, 0)

        status = TextView(this)
        status.textSize = 12f
        status.setTextColor(textDim)
        status.setPadding(0, dp(4), 0, 0)

        totalCard.addView(totalLabel)
        totalCard.addView(total)
        totalCard.addView(progress)
        totalCard.addView(limitInfo)
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
        val bp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52))
        bp.topMargin = dp(16)
        button.layoutParams = bp

        val limitCard = LinearLayout(this)
        limitCard.orientation = LinearLayout.VERTICAL
        limitCard.background = cardBg()
        limitCard.setPadding(dp(20), dp(20), dp(20), dp(20))
        val lcp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lcp.topMargin = dp(16)
        limitCard.layoutParams = lcp

        val limitTitle = TextView(this)
        limitTitle.text = "Daily limit"
        limitTitle.textSize = 16f
        limitTitle.setTextColor(textMain)
        limitTitle.setTypeface(null, Typeface.BOLD)

        val inputRow = LinearLayout(this)
        inputRow.orientation = LinearLayout.HORIZONTAL
        inputRow.setPadding(0, dp(12), 0, dp(12))
        hoursInput = makeInput("Hours")
        minutesInput = makeInput("Minutes")
        inputRow.addView(hoursInput)
        inputRow.addView(minutesInput)

        val save = Button(this)
        save.text = "Save limit"
        save.setTextColor(bg)
        val sb = GradientDrawable()
        sb.setColor(accent)
        sb.cornerRadius = dp(12).toFloat()
        save.background = sb
        save.setOnClickListener { saveLimit() }
        save.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(48)
        )

        limitCard.addView(limitTitle)
        limitCard.addView(inputRow)
        limitCard.addView(save)

        val appsTitle = TextView(this)
        appsTitle.text = "Apps used today"
        appsTitle.textSize = 16f
        appsTitle.setTextColor(textMain)
        appsTitle.setTypeface(null, Typeface.BOLD)
        appsTitle.setPadding(0, dp(24), 0, dp(10))

        listBox = LinearLayout(this)
        listBox.orientation = LinearLayout.VERTICAL

        root.addView(title)
        root.addView(sub)
        root.addView(totalCard)
        root.addView(button)
        root.addView(limitCard)
        root.addView(appsTitle)
        root.addView(listBox)

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)
        scroll.addView(root)
        setContentView(scroll)

        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        val prefs = getSharedPreferences("timeiq", MODE_PRIVATE)
        val saved = prefs.getInt("limit_min", 0)
        if (saved > 0) {
            hoursInput.setText((saved / 60).toString())
            minutesInput.setText((saved % 60).toString())
        }
    }

    private fun saveLimit() {
        val h = hoursInput.text.toString().toIntOrNull() ?: 0
        val m = minutesInput.text.toString().toIntOrNull() ?: 0
        val total = h * 60 + m
        if (total <= 0) {
            Toast.makeText(this, "Enter a limit greater than 0", Toast.LENGTH_SHORT).show()
            return
        }
        getSharedPreferences("timeiq", MODE_PRIVATE)
            .edit().putInt("limit_min", total).apply()
        Toast.makeText(this, "Limit saved: " + format(total * 60000L), Toast.LENGTH_SHORT).show()
        startMonitor()
        refresh()
    }

    private fun startMonitor() {
        if (!hasUsageAccess()) return
        val i = Intent(this, MonitorService::class.java)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
    }

    override fun onResume() {
        super.onResume()
        refresh()
        val limit = getSharedPreferences("timeiq", MODE_PRIVATE).getInt("limit_min", 0)
        if (limit > 0) startMonitor()
    }

    private fun refresh() {
        val limit = getSharedPreferences("timeiq", MODE_PRIVATE).getInt("limit_min", 0)
        if (hasUsageAccess()) {
            status.text = "Usage Access: granted"
            button.visibility = View.GONE
            showUsage(limit)
        } else {
            status.text = "Usage Access: not granted"
            button.visibility = View.VISIBLE
            total.text = "--"
            progress.progress = 0
            limitInfo.text = if (limit > 0) "Limit: " + format(limit * 60000L) else "No limit set"
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

    private fun showUsage(limitMin: Int) {
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
            if (pkg == packageName) continue
            if (pm.getLaunchIntentForPackage(pkg) == null) continue
            sum += ms
            if (ms < 60000L) continue
            val name = try {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                pkg
            }
            rows.add(Pair(name, ms))
        }

        rows.sortByDescending { it.second }
        total.text = format(sum)

        if (limitMin > 0) {
            val limitMs = limitMin * 60000L
            val pct = ((sum * 100) / limitMs).toInt()
            progress.progress = if (pct > 100) 100 else pct
            val left = limitMs - sum
            limitInfo.text = if (left > 0) {
                "Limit " + format(limitMs) + "  -  " + format(left) + " left"
            } else {
                "Limit " + format(limitMs) + "  -  limit reached"
            }
        } else {
            progress.progress = 0
            limitInfo.text = "No limit set"
        }

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
