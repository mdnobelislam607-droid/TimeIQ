package com.timeiq.app

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.ComponentName
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private lateinit var total: TextView
    private lateinit var listBox: LinearLayout
    private lateinit var button: Button
    private lateinit var limitInfo: TextView
    private lateinit var progress: ProgressBar
    private lateinit var hoursInput: EditText
    private lateinit var minutesInput: EditText
    private lateinit var blockerBtn: Button
    private lateinit var selectedInfo: TextView
    private lateinit var goalInfo: TextView
    private lateinit var targetInput: EditText
    private lateinit var stepInput: EditText
    private lateinit var weekBox: LinearLayout

    private val bg = Color.parseColor("#0F172A")
    private val card = Color.parseColor("#1E293B")
    private val accent = Color.parseColor("#38BDF8")
    private val textMain = Color.parseColor("#F1F5F9")
    private val textDim = Color.parseColor("#94A3B8")

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun fillBg(color: Int, r: Int): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(color)
        g.cornerRadius = dp(r).toFloat()
        return g
    }

    private fun cardBg(): GradientDrawable = fillBg(card, 16)

    private fun makeInput(hint: String): EditText {
        val e = EditText(this)
        e.hint = hint
        e.inputType = InputType.TYPE_CLASS_NUMBER
        e.setTextColor(textMain)
        e.setHintTextColor(textDim)
        e.background = fillBg(bg, 10)
        e.setPadding(dp(14), dp(10), dp(14), dp(10))
        e.gravity = Gravity.CENTER
        val lp = LinearLayout.LayoutParams(0, dp(48), 1f)
        lp.marginEnd = dp(8)
        e.layoutParams = lp
        return e
    }

    private fun wideButton(text: String, fill: Int, textColor: Int): Button {
        val b = Button(this)
        b.text = text
        b.setTextColor(textColor)
        b.background = fillBg(fill, 12)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52))
        lp.topMargin = dp(12)
        b.layoutParams = lp
        return b
    }

    private fun makeCard(): LinearLayout {
        val c = LinearLayout(this)
        c.orientation = LinearLayout.VERTICAL
        c.background = cardBg()
        c.setPadding(dp(20), dp(20), dp(20), dp(20))
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.topMargin = dp(16)
        c.layoutParams = lp
        return c
    }

    private fun cardTitle(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = 16f
        t.setTextColor(textMain)
        t.setTypeface(null, Typeface.BOLD)
        return t
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

        button = wideButton("Grant Usage Access", accent, bg)
        button.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        val limitCard = makeCard()
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
        save.background = fillBg(accent, 12)
        save.setOnClickListener { saveLimit() }
        save.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(48)
        )
        limitCard.addView(cardTitle("Daily limit"))
        limitCard.addView(inputRow)
        limitCard.addView(save)

        val goalCard = makeCard()
        goalInfo = TextView(this)
        goalInfo.textSize = 13f
        goalInfo.setTextColor(textDim)
        goalInfo.setPadding(0, dp(6), 0, 0)
        val goalRow = LinearLayout(this)
        goalRow.orientation = LinearLayout.HORIZONTAL
        goalRow.setPadding(0, dp(12), 0, 0)
        targetInput = makeInput("Target (min)")
        stepInput = makeInput("Cut/day (min)")
        goalRow.addView(targetInput)
        goalRow.addView(stepInput)
        val startPlan = wideButton("Start plan", accent, bg)
        startPlan.setOnClickListener { startPlan() }
        goalCard.addView(cardTitle("Gradual reduction plan"))
        goalCard.addView(goalInfo)
        goalCard.addView(goalRow)
        goalCard.addView(startPlan)

        val blockCard = makeCard()
        selectedInfo = TextView(this)
        selectedInfo.textSize = 13f
        selectedInfo.setTextColor(textDim)
        selectedInfo.setPadding(0, dp(6), 0, 0)
        val pick = wideButton("Select apps", accent, bg)
        pick.setOnClickListener {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }
        blockerBtn = wideButton("Enable Blocker", bg, textMain)
        blockerBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(
                this,
                "Open Installed apps, then TimeIQ App Blocker, then turn it on",
                Toast.LENGTH_LONG
            ).show()
        }
        blockCard.addView(cardTitle("App blocking"))
        blockCard.addView(selectedInfo)
        blockCard.addView(pick)
        blockCard.addView(blockerBtn)

        val weekCard = makeCard()
        weekBox = LinearLayout(this)
        weekBox.orientation = LinearLayout.VERTICAL
        weekBox.setPadding(0, dp(12), 0, 0)
        weekCard.addView(cardTitle("Last 7 days"))
        weekCard.addView(weekBox)

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
        root.addView(goalCard)
        root.addView(blockCard)
        root.addView(weekCard)
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
    }

    private fun saveLimit() {
        val h = hoursInput.text.toString().toIntOrNull() ?: 0
        val m = minutesInput.text.toString().toIntOrNull() ?: 0
        val minutes = h * 60 + m
        if (minutes <= 0) {
            Toast.makeText(this, "Enter a limit greater than 0", Toast.LENGTH_SHORT).show()
            return
        }
        getSharedPreferences("timeiq", MODE_PRIVATE).edit()
            .putInt("limit_min", minutes)
            .putBoolean("goal_on", false)
            .apply()
        Toast.makeText(this, "Limit saved: " + fmtMin(minutes), Toast.LENGTH_SHORT).show()
        startMonitor()
        refresh()
    }

    private fun startPlan() {
        val prefs = getSharedPreferences("timeiq", MODE_PRIVATE)
        val current = prefs.getInt("limit_min", 0)
        val target = targetInput.text.toString().toIntOrNull() ?: 0
        val step = stepInput.text.toString().toIntOrNull() ?: 0
        if (current <= 0) {
            Toast.makeText(this, "Save a daily limit first", Toast.LENGTH_SHORT).show()
            return
        }
        if (target <= 0 || target >= current) {
            Toast.makeText(this, "Target must be lower than your current limit", Toast.LENGTH_LONG).show()
            return
        }
        if (step <= 0) {
            Toast.makeText(this, "Enter minutes to cut per day", Toast.LENGTH_SHORT).show()
            return
        }
        prefs.edit()
            .putBoolean("goal_on", true)
            .putInt("goal_start", current)
            .putInt("goal_target", target)
            .putInt("goal_step", step)
            .putLong("goal_day0", Usage.epochDay())
            .apply()
        Toast.makeText(this, "Plan started", Toast.LENGTH_SHORT).show()
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
        Usage.applyGoal(this)
        refresh()
        val limit = getSharedPreferences("timeiq", MODE_PRIVATE).getInt("limit_min", 0)
        if (limit > 0) startMonitor()
    }

    private fun blockerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val me = ComponentName(this, BlockerService::class.java).flattenToString()
        return enabled.split(":").any { it.equals(me, ignoreCase = true) }
    }

    private fun refresh() {
        val prefs = getSharedPreferences("timeiq", MODE_PRIVATE)
        val limit = prefs.getInt("limit_min", 0)
        val count = (prefs.getStringSet("blocked", emptySet()) ?: emptySet()).size

        if (limit > 0) {
            hoursInput.setText((limit / 60).toString())
            minutesInput.setText((limit % 60).toString())
        }

        selectedInfo.text = if (blockerEnabled()) {
            "Blocker: ON  -  $count app(s) selected"
        } else {
            "Blocker: OFF  -  $count app(s) selected"
        }
        blockerBtn.visibility = if (blockerEnabled()) View.GONE else View.VISIBLE

        if (prefs.getBoolean("goal_on", false)) {
            goalInfo.text = "Plan: " + fmtMin(prefs.getInt("goal_start", 0)) +
                " to " + fmtMin(prefs.getInt("goal_target", 0)) +
                ", cut " + prefs.getInt("goal_step", 0) + "m per day. Today's limit: " +
                fmtMin(limit)
        } else {
            goalInfo.text = "No plan. Save a limit, then set a lower target and how many minutes to cut each day."
        }

        if (hasUsageAccess()) {
            status.text = "Usage Access: granted"
            button.visibility = View.GONE
            showUsage(limit)
            showWeek()
        } else {
            status.text = "Usage Access: not granted"
            button.visibility = View.VISIBLE
            total.text = "--"
            progress.progress = 0
            limitInfo.text = if (limit > 0) "Limit: " + fmtMin(limit) else "No limit set"
            listBox.removeAllViews()
            weekBox.removeAllViews()
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

    private fun showWeek() {
        weekBox.removeAllViews()
        val sdf = SimpleDateFormat("EEE", Locale.ENGLISH)
        val vals = LongArray(7)
        val labels = ArrayList<String>()
        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            c.add(Calendar.DAY_OF_YEAR, -i)
            val s = c.timeInMillis
            val e = if (i == 0) System.currentTimeMillis() else s + 86400000L
            vals[6 - i] = Usage.rangeTotalMs(this, s, e)
            labels.add(if (i == 0) "Today" else sdf.format(c.time))
        }

        var max = 1L
        var sum = 0L
        var days = 0
        for (v in vals) {
            if (v > max) max = v
            if (v > 0) {
                sum += v
                days++
            }
        }

        for (idx in 0 until 7) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.setPadding(0, dp(6), 0, dp(6))

            val lbl = TextView(this)
            lbl.text = labels[idx]
            lbl.textSize = 13f
            lbl.setTextColor(textDim)
            lbl.layoutParams = LinearLayout.LayoutParams(dp(52), LinearLayout.LayoutParams.WRAP_CONTENT)

            val bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            bar.max = 100
            bar.progress = ((vals[idx] * 100) / max).toInt()
            bar.progressDrawable.setTint(accent)
            val bp = LinearLayout.LayoutParams(0, dp(8), 1f)
            bp.marginStart = dp(6)
            bp.marginEnd = dp(6)
            bar.layoutParams = bp

            val v = TextView(this)
            v.text = format(vals[idx])
            v.textSize = 13f
            v.setTextColor(textMain)
            v.gravity = Gravity.END
            v.layoutParams = LinearLayout.LayoutParams(dp(64), LinearLayout.LayoutParams.WRAP_CONTENT)

            row.addView(lbl)
            row.addView(bar)
            row.addView(v)
            weekBox.addView(row)
        }

        val avg = TextView(this)
        avg.textSize = 13f
        avg.setTextColor(textDim)
        avg.setPadding(0, dp(10), 0, 0)
        avg.text = if (days > 0) "Daily average: " + format(sum / days) else "No data yet"
        weekBox.addView(avg)
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

    private fun fmtMin(min: Int): String = format(min * 60000L)

    private fun format(ms: Long): String {
        val minutes = ms / 60000L
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}
