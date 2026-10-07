package com.timeiq.app

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var todayText: TextView
    private lateinit var blockedText: TextView
    private lateinit var progress: ProgressBar
    private lateinit var limitInfo: TextView
    private lateinit var protTitle: TextView
    private lateinit var protSub: TextView
    private lateinit var usageBtn: Button
    private lateinit var blockerBtn: Button
    private lateinit var gridBox: LinearLayout

    private fun dp(v: Int): Int = Ui.dp(this, v)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Ui.bg
        window.navigationBarColor = Ui.bg

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Ui.bg)
        root.setPadding(dp(20), dp(40), dp(20), dp(24))

        // Header with logo
        val header = LinearLayout(this)
        header.orientation = LinearLayout.HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL
        val logo = ImageView(this)
        val llp = LinearLayout.LayoutParams(dp(56), dp(56))
        llp.marginEnd = dp(14)
        logo.layoutParams = llp
        logo.setImageResource(R.drawable.timeiq_logo)
        val titleBox = LinearLayout(this)
        titleBox.orientation = LinearLayout.VERTICAL
        titleBox.addView(Ui.text(this, "TimeIQ", 28f, Ui.accent, true))
        titleBox.addView(Ui.text(this, "Less Screen Time  -  A Better You", 13f, Ui.textDim))
        header.addView(logo)
        header.addView(titleBox)
        root.addView(header)

        // Stats card
        val stats = Ui.card(this)
        val statRow = LinearLayout(this)
        statRow.orientation = LinearLayout.HORIZONTAL

        val left = LinearLayout(this)
        left.orientation = LinearLayout.VERTICAL
        left.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        left.addView(Ui.text(this, "Screen time today", 12f, Ui.textDim))
        todayText = Ui.text(this, "--", 30f, Ui.textMain, true)
        left.addView(todayText)

        val right = LinearLayout(this)
        right.orientation = LinearLayout.VERTICAL
        right.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        right.addView(Ui.text(this, "Apps controlled", 12f, Ui.textDim))
        blockedText = Ui.text(this, "0", 30f, Ui.green, true)
        right.addView(blockedText)

        statRow.addView(left)
        statRow.addView(right)

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        progress.max = 100
        progress.progressDrawable.setTint(Ui.accent)
        val pp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(8))
        pp.topMargin = dp(14)
        progress.layoutParams = pp

        limitInfo = Ui.text(this, "", 13f, Ui.textDim)
        limitInfo.setPadding(0, dp(8), 0, 0)

        stats.addView(statRow)
        stats.addView(progress)
        stats.addView(limitInfo)
        root.addView(stats)

        // Protection card
        val prot = Ui.card(this)
        protTitle = Ui.text(this, "Protection is OFF", 22f, Ui.orange, true)
        protSub = Ui.text(this, "", 13f, Ui.textDim)
        protSub.setPadding(0, dp(4), 0, 0)
        usageBtn = Ui.button(this, "Grant Usage Access", Ui.accent, Ui.bg)
        usageBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        blockerBtn = Ui.button(this, "Enable Blocker", Ui.bg, Ui.textMain)
        blockerBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(
                this,
                "Open Installed apps, then TimeIQ App Blocker, then turn it on",
                Toast.LENGTH_LONG
            ).show()
        }
        prot.addView(protTitle)
        prot.addView(protSub)
        prot.addView(usageBtn)
        prot.addView(blockerBtn)
        root.addView(prot)

        // Features
        val ft = Ui.text(this, "Features", 18f, Ui.textMain, true)
        ft.setPadding(0, dp(24), 0, dp(8))
        root.addView(ft)
        gridBox = LinearLayout(this)
        gridBox.orientation = LinearLayout.VERTICAL
        root.addView(gridBox)

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Ui.bg)
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

    override fun onResume() {
        super.onResume()
        Usage.applyGoal(this)
        refresh()
        val limit = getSharedPreferences("timeiq", MODE_PRIVATE).getInt("limit_min", 0)
        if (limit > 0) Ui.startMonitor(this)
    }

    private fun blockerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val me = ComponentName(this, BlockerService::class.java).flattenToString()
        return enabled.split(":").any { it.equals(me, ignoreCase = true) }
    }

    private fun open(type: String) {
        val i = Intent(this, FeatureActivity::class.java)
        i.putExtra("type", type)
        startActivity(i)
    }

    private fun tile(emoji: String, title: String, sub: String, onClick: () -> Unit): LinearLayout {
        val t = LinearLayout(this)
        t.orientation = LinearLayout.VERTICAL
        t.background = Ui.round(this, Ui.cardC, 16)
        t.setPadding(dp(16), dp(16), dp(16), dp(16))
        t.minimumHeight = dp(120)
        val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        lp.setMargins(dp(5), dp(5), dp(5), dp(5))
        t.layoutParams = lp
        t.addView(Ui.text(this, emoji, 26f, Ui.textMain))
        val tt = Ui.text(this, title, 15f, Ui.textMain, true)
        tt.setPadding(0, dp(8), 0, 0)
        t.addView(tt)
        t.addView(Ui.text(this, sub, 12f, Ui.textDim))
        t.setOnClickListener { onClick() }
        return t
    }

    private fun refresh() {
        val p = getSharedPreferences("timeiq", MODE_PRIVATE)
        val limit = p.getInt("limit_min", 0)
        val count = (p.getStringSet("blocked", emptySet()) ?: emptySet()).size
        val usageOk = Ui.hasUsageAccess(this)
        val blockerOn = blockerEnabled()

        blockedText.text = count.toString()

        if (usageOk) {
            val used = Usage.todayTotalMs(this)
            todayText.text = Ui.fmtMs(used)
            if (limit > 0) {
                val limitMs = limit * 60000L
                val pct = ((used * 100) / limitMs).toInt()
                progress.progress = if (pct > 100) 100 else pct
                val left = limitMs - used
                limitInfo.text = if (left > 0) {
                    "Limit " + Ui.fmtMs(limitMs) + "  -  " + Ui.fmtMs(left) + " left"
                } else {
                    "Limit " + Ui.fmtMs(limitMs) + "  -  limit reached"
                }
            } else {
                progress.progress = 0
                limitInfo.text = "No limit set"
            }
        } else {
            todayText.text = "--"
            progress.progress = 0
            limitInfo.text = if (limit > 0) "Limit: " + Ui.fmtMin(limit) else "No limit set"
        }

        val missing = ArrayList<String>()
        if (!usageOk) missing.add("Usage Access")
        if (!blockerOn) missing.add("Blocker")
        if (limit <= 0) missing.add("a daily limit")
        if (count == 0) missing.add("apps to control")
        if (missing.isEmpty()) {
            protTitle.text = "Protection is ON"
            protTitle.setTextColor(Ui.green)
            protSub.text = "TimeIQ is watching and will block your selected apps after the limit."
        } else {
            protTitle.text = "Protection is OFF"
            protTitle.setTextColor(Ui.orange)
            protSub.text = "Still needed: " + missing.joinToString(", ")
        }
        usageBtn.visibility = if (usageOk) View.GONE else View.VISIBLE
        blockerBtn.visibility = if (blockerOn) View.GONE else View.VISIBLE

        gridBox.removeAllViews()
        val tiles = listOf(
            tile("\u23F1", "Daily limit", if (limit > 0) Ui.fmtMin(limit) else "Not set") { open("limit") },
            tile("\uD83D\uDCC9", "Reduction plan", if (p.getBoolean("goal_on", false)) "Active" else "Not started") { open("plan") },
            tile("\uD83D\uDEAB", "Block apps", "$count selected") {
                startActivity(Intent(this, AppPickerActivity::class.java))
            },
            tile("\uD83D\uDD12", "Security", "Code and unlock history") {
                startActivity(Intent(this, SecurityActivity::class.java))
            },
            tile("\uD83D\uDCCA", "Last 7 days", "Weekly report") { open("week") },
            tile("\uD83D\uDCF1", "Apps today", "See what you used") { open("today") }
        )
        var i = 0
        while (i < tiles.size) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.addView(tiles[i])
            row.addView(tiles[i + 1])
            gridBox.addView(row)
            i += 2
        }
    }
}
