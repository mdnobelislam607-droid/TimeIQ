package com.timeiq.app

import android.app.usage.UsageStatsManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FeatureActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Ui.bg
        window.navigationBarColor = Ui.bg
        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Ui.bg)
        root.setPadding(Ui.dp(this, 20), Ui.dp(this, 40), Ui.dp(this, 20), Ui.dp(this, 24))
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Ui.bg)
        scroll.addView(root)
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        root.removeAllViews()
        when (intent.getStringExtra("type")) {
            "limit" -> showLimit()
            "plan" -> showPlan()
            "week" -> showWeek()
            else -> showToday()
        }
    }

    private fun header(title: String, sub: String) {
        root.addView(Ui.text(this, title, 28f, Ui.accent, true))
        val s = Ui.text(this, sub, 14f, Ui.textDim)
        s.setPadding(0, Ui.dp(this, 2), 0, Ui.dp(this, 8))
        root.addView(s)
    }

    private fun prefs() = getSharedPreferences("timeiq", MODE_PRIVATE)

    private fun showLimit() {
        header("Daily limit", "How long you can use your phone each day")
        val p = prefs()
        val cur = p.getInt("limit_min", 0)
        val c = Ui.card(this)
        c.addView(
            Ui.text(
                this,
                if (cur > 0) "Current limit: " + Ui.fmtMin(cur) else "No limit set",
                16f, Ui.textMain, true
            )
        )
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, Ui.dp(this, 12), 0, 0)
        val hIn = Ui.numberInput(this, "Hours")
        val mIn = Ui.numberInput(this, "Minutes")
        if (cur > 0) {
            hIn.setText((cur / 60).toString())
            mIn.setText((cur % 60).toString())
        }
        row.addView(hIn)
        row.addView(mIn)
        val save = Ui.button(this, "Save limit", Ui.accent, Ui.bg)
        save.setOnClickListener {
            val mins = (hIn.text.toString().toIntOrNull() ?: 0) * 60 +
                (mIn.text.toString().toIntOrNull() ?: 0)
            if (mins <= 0) {
                Toast.makeText(this, "Enter a limit greater than 0", Toast.LENGTH_SHORT).show()
            } else {
                p.edit().putInt("limit_min", mins).putBoolean("goal_on", false).apply()
                Toast.makeText(this, "Limit saved: " + Ui.fmtMin(mins), Toast.LENGTH_SHORT).show()
                Ui.startMonitor(this)
                render()
            }
        }
        c.addView(row)
        c.addView(save)
        root.addView(c)
    }

    private fun showPlan() {
        header("Reduction plan", "Lower your daily limit step by step")
        val p = prefs()
        val cur = p.getInt("limit_min", 0)
        val c = Ui.card(this)

        val info = if (p.getBoolean("goal_on", false)) {
            "Plan: " + Ui.fmtMin(p.getInt("goal_start", 0)) + " to " +
                Ui.fmtMin(p.getInt("goal_target", 0)) + ", cut " +
                p.getInt("goal_step", 0) + "m per day.\nToday's limit: " + Ui.fmtMin(cur)
        } else {
            "No plan yet. Save a daily limit first, then choose a lower target."
        }
        c.addView(Ui.text(this, info, 14f, Ui.textDim))
        c.addView(Ui.text(this, "Both boxes are in MINUTES. Example: target 90, cut 10.", 13f, Ui.orange).also {
            it.setPadding(0, Ui.dp(this, 10), 0, 0)
        })

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, Ui.dp(this, 12), 0, 0)
        val tIn = Ui.numberInput(this, "Target (min)")
        val sIn = Ui.numberInput(this, "Cut/day (min)")
        row.addView(tIn)
        row.addView(sIn)

        val start = Ui.button(this, "Start plan", Ui.accent, Ui.bg)
        start.setOnClickListener {
            val target = tIn.text.toString().toIntOrNull() ?: 0
            val step = sIn.text.toString().toIntOrNull() ?: 0
            val now = p.getInt("limit_min", 0)
            if (now <= 0) {
                Toast.makeText(this, "Save a daily limit first", Toast.LENGTH_SHORT).show()
            } else if (target < 10 || target >= now) {
                Toast.makeText(
                    this,
                    "Target must be at least 10 and lower than your limit (" + now + ")",
                    Toast.LENGTH_LONG
                ).show()
            } else if (step <= 0) {
                Toast.makeText(this, "Enter minutes to cut per day", Toast.LENGTH_SHORT).show()
            } else {
                p.edit()
                    .putBoolean("goal_on", true)
                    .putInt("goal_start", now)
                    .putInt("goal_target", target)
                    .putInt("goal_step", step)
                    .putLong("goal_day0", Usage.epochDay())
                    .apply()
                Toast.makeText(this, "Plan started", Toast.LENGTH_SHORT).show()
                Ui.startMonitor(this)
                render()
            }
        }
        c.addView(row)
        c.addView(start)

        if (p.getBoolean("goal_on", false)) {
            val stop = Ui.button(this, "Stop plan", Ui.bg, Ui.textMain)
            stop.setOnClickListener {
                p.edit().putBoolean("goal_on", false).apply()
                render()
            }
            c.addView(stop)
        }
        root.addView(c)
    }

    private fun showWeek() {
        header("Last 7 days", "Your daily screen time")
        if (!Ui.hasUsageAccess(this)) {
            root.addView(Ui.text(this, "Usage Access is not granted. Go back and tap Grant Usage Access.", 15f, Ui.textDim))
            return
        }
        val c = Ui.card(this)
        val sdf = SimpleDateFormat("EEE", Locale.ENGLISH)
        val vals = LongArray(7)
        val labels = ArrayList<String>()
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val s = cal.timeInMillis
            val e = if (i == 0) System.currentTimeMillis() else s + 86400000L
            vals[6 - i] = Usage.rangeTotalMs(this, s, e)
            labels.add(if (i == 0) "Today" else sdf.format(cal.time))
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
            row.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8))

            val lbl = Ui.text(this, labels[idx], 13f, Ui.textDim)
            lbl.layoutParams = LinearLayout.LayoutParams(Ui.dp(this, 52), LinearLayout.LayoutParams.WRAP_CONTENT)

            val bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            bar.max = 100
            bar.progress = ((vals[idx] * 100) / max).toInt()
            bar.progressDrawable.setTint(Ui.accent)
            val bp = LinearLayout.LayoutParams(0, Ui.dp(this, 8), 1f)
            bp.marginStart = Ui.dp(this, 6)
            bp.marginEnd = Ui.dp(this, 6)
            bar.layoutParams = bp

            val v = Ui.text(this, Ui.fmtMs(vals[idx]), 13f, Ui.textMain)
            v.gravity = Gravity.END
            v.layoutParams = LinearLayout.LayoutParams(Ui.dp(this, 64), LinearLayout.LayoutParams.WRAP_CONTENT)

            row.addView(lbl)
            row.addView(bar)
            row.addView(v)
            c.addView(row)
        }
        val avg = Ui.text(
            this,
            if (days > 0) "Daily average: " + Ui.fmtMs(sum / days) else "No data yet",
            13f, Ui.textDim
        )
        avg.setPadding(0, Ui.dp(this, 10), 0, 0)
        c.addView(avg)
        root.addView(c)
    }

    private fun showToday() {
        header("Apps used today", "Apps you used for more than 1 minute")
        if (!Ui.hasUsageAccess(this)) {
            root.addView(Ui.text(this, "Usage Access is not granted. Go back and tap Grant Usage Access.", 15f, Ui.textDim))
            return
        }
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val usm = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = usm.queryAndAggregateUsageStats(cal.timeInMillis, System.currentTimeMillis())
        val pm = packageManager
        val rows = ArrayList<Triple<String, String, Long>>()
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
            rows.add(Triple(name, pkg, ms))
        }
        rows.sortByDescending { it.third }

        if (rows.isEmpty()) {
            root.addView(Ui.text(this, "No usage recorded yet today.", 15f, Ui.textDim))
            return
        }
        for (r in rows) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.background = Ui.round(this, Ui.cardC, 16)
            row.setPadding(Ui.dp(this, 16), Ui.dp(this, 12), Ui.dp(this, 16), Ui.dp(this, 12))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = Ui.dp(this, 8)
            row.layoutParams = lp

            val icon = ImageView(this)
            val ilp = LinearLayout.LayoutParams(Ui.dp(this, 36), Ui.dp(this, 36))
            ilp.marginEnd = Ui.dp(this, 12)
            icon.layoutParams = ilp
            try {
                icon.setImageDrawable(pm.getApplicationIcon(r.second))
            } catch (e: PackageManager.NameNotFoundException) {
            }

            val n = Ui.text(this, r.first, 15f, Ui.textMain)
            n.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            val t = Ui.text(this, Ui.fmtMs(r.third), 15f, Ui.accent)

            row.addView(icon)
            row.addView(n)
            row.addView(t)
            root.addView(row)
        }
    }
}
