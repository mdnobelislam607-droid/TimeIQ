package com.timeiq.app

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SecurityActivity : AppCompatActivity() {

    private val bg = Color.parseColor("#0F172A")
    private val card = Color.parseColor("#1E293B")
    private val accent = Color.parseColor("#38BDF8")
    private val textMain = Color.parseColor("#F1F5F9")
    private val textDim = Color.parseColor("#94A3B8")

    private val waitMs = 24L * 60L * 60L * 1000L

    private lateinit var root: LinearLayout

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun round(color: Int, r: Int): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(color)
        g.cornerRadius = dp(r).toFloat()
        return g
    }

    private fun hash(code: String): String {
        val b = MessageDigest.getInstance("SHA-256").digest(("timeiq:" + code).toByteArray())
        return b.joinToString("") { "%02x".format(it) }
    }

    private fun prefs() = getSharedPreferences("timeiq", MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg

        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(bg)
        root.setPadding(dp(20), dp(40), dp(20), dp(24))

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)
        scroll.addView(root)
        setContentView(scroll)
        render()
    }

    private fun text(t: String, size: Float, color: Int, bold: Boolean = false): TextView {
        val v = TextView(this)
        v.text = t
        v.textSize = size
        v.setTextColor(color)
        if (bold) v.setTypeface(null, Typeface.BOLD)
        v.setPadding(0, 0, 0, dp(8))
        return v
    }

    private fun cardBox(): LinearLayout {
        val c = LinearLayout(this)
        c.orientation = LinearLayout.VERTICAL
        c.background = round(card, 16)
        c.setPadding(dp(20), dp(20), dp(20), dp(20))
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.topMargin = dp(16)
        c.layoutParams = lp
        return c
    }

    private fun input(hint: String): EditText {
        val e = EditText(this)
        e.hint = hint
        e.setTextColor(textMain)
        e.setHintTextColor(textDim)
        e.background = round(bg, 10)
        e.setPadding(dp(16), dp(12), dp(16), dp(12))
        e.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.topMargin = dp(10)
        e.layoutParams = lp
        return e
    }

    private fun btn(t: String, fill: Int, tc: Int): Button {
        val b = Button(this)
        b.text = t
        b.setTextColor(tc)
        b.background = round(fill, 12)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48))
        lp.topMargin = dp(12)
        b.layoutParams = lp
        return b
    }

    private fun render() {
        root.removeAllViews()
        root.addView(Ui.backBar(this, "Security"))
        root.addView(text("Management Code and unlock history", 14f, textDim))

        val p = prefs()
        val stored = p.getString("code_hash", null)
        val resetAt = p.getLong("reset_at", 0L)
        val now = System.currentTimeMillis()

        val codeCard = cardBox()
        codeCard.addView(text("Management Code", 16f, textMain, true))

        if (stored == null) {
            codeCard.addView(text("No code yet. It is created the first time you unlock an app.", 13f, textDim))
        } else if (resetAt > 0L && now >= resetAt) {
            codeCard.addView(text("Reset is ready. Choose a new code (4 to 8 digits).", 13f, textDim))
            val n = input("New code")
            codeCard.addView(n)
            val ok = btn("Set new code", accent, bg)
            ok.setOnClickListener {
                val c = n.text.toString()
                if (c.length < 4 || c.length > 8) {
                    Toast.makeText(this, "Code must be 4 to 8 digits", Toast.LENGTH_SHORT).show()
                } else {
                    p.edit().putString("code_hash", hash(c)).putLong("reset_at", 0L).apply()
                    Toast.makeText(this, "New code saved", Toast.LENGTH_SHORT).show()
                    render()
                }
            }
            codeCard.addView(ok)
        } else {
            val old = input("Current code")
            val n = input("New code (4 to 8 digits)")
            codeCard.addView(old)
            codeCard.addView(n)
            val change = btn("Change code", accent, bg)
            change.setOnClickListener {
                val c = n.text.toString()
                if (hash(old.text.toString()) != stored) {
                    Toast.makeText(this, "Current code is wrong", Toast.LENGTH_SHORT).show()
                } else if (c.length < 4 || c.length > 8) {
                    Toast.makeText(this, "New code must be 4 to 8 digits", Toast.LENGTH_SHORT).show()
                } else {
                    p.edit().putString("code_hash", hash(c)).apply()
                    Toast.makeText(this, "Code changed", Toast.LENGTH_SHORT).show()
                    render()
                }
            }
            codeCard.addView(change)

            if (resetAt > 0L) {
                val hoursLeft = ((resetAt - now) / 3600000L) + 1
                codeCard.addView(text("Reset requested. Ready in about $hoursLeft hour(s).", 13f, textDim))
                val cancel = btn("Cancel reset", bg, textMain)
                cancel.setOnClickListener {
                    p.edit().putLong("reset_at", 0L).apply()
                    render()
                }
                codeCard.addView(cancel)
            } else {
                val forgot = btn("I forgot my code (24 hour wait)", bg, textMain)
                forgot.setOnClickListener {
                    p.edit().putLong("reset_at", System.currentTimeMillis() + waitMs).apply()
                    Toast.makeText(this, "You can set a new code in 24 hours", Toast.LENGTH_LONG).show()
                    render()
                }
                codeCard.addView(forgot)
            }
        }
        root.addView(codeCard)

        val logCard = cardBox()
        logCard.addView(text("Unlock history", 16f, textMain, true))
        val raw = p.getString("unlock_log", "") ?: ""
        val lines = raw.split("\n").filter { it.isNotBlank() }.reversed()
        if (lines.isEmpty()) {
            logCard.addView(text("No unlocks yet.", 13f, textDim))
        } else {
            val fmt = SimpleDateFormat("d MMM, h:mm a", Locale.ENGLISH)
            for (line in lines.take(50)) {
                val idx = line.indexOf('|')
                if (idx <= 0) continue
                val ts = line.substring(0, idx).toLongOrNull() ?: continue
                val reason = line.substring(idx + 1)
                logCard.addView(text(fmt.format(Date(ts)), 12f, accent))
                logCard.addView(text(reason, 15f, textMain))
            }
        }
        root.addView(logCard)
    }
}
