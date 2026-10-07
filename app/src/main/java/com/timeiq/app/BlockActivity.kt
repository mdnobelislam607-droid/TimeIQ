package com.timeiq.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest

class BlockActivity : AppCompatActivity() {

    private val bg = Color.parseColor("#0F172A")
    private val card = Color.parseColor("#1E293B")
    private val accent = Color.parseColor("#38BDF8")
    private val textMain = Color.parseColor("#F1F5F9")
    private val textDim = Color.parseColor("#94A3B8")
    private val danger = Color.parseColor("#F87171")

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

    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg

        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(bg)
        root.setPadding(dp(28), dp(40), dp(28), dp(40))
        setContentView(root)
        showBlocked()
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = size
        t.setTextColor(color)
        t.gravity = Gravity.CENTER
        if (bold) t.setTypeface(null, Typeface.BOLD)
        t.setPadding(0, 0, 0, dp(12))
        return t
    }

    private fun button(text: String, fill: Int, textColor: Int): Button {
        val b = Button(this)
        b.text = text
        b.setTextColor(textColor)
        b.background = round(fill, 12)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52))
        lp.topMargin = dp(12)
        b.layoutParams = lp
        return b
    }

    private fun input(hint: String, number: Boolean): EditText {
        val e = EditText(this)
        e.hint = hint
        e.setTextColor(textMain)
        e.setHintTextColor(textDim)
        e.background = round(card, 10)
        e.setPadding(dp(16), dp(12), dp(16), dp(12))
        if (number) {
            e.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(12)
        e.layoutParams = lp
        return e
    }

    private fun showBlocked() {
        root.removeAllViews()
        root.addView(label("Time limit reached", 28f, danger, true))
        root.addView(label("You have used your daily limit. This app is blocked for today.", 16f, textMain))

        val unlock = button("Request unlock", accent, bg)
        unlock.setOnClickListener { showReason() }
        val home = button("Go to home screen", card, textMain)
        home.setOnClickListener { goHome() }
        root.addView(unlock)
        root.addView(home)
    }

    private fun showReason() {
        root.removeAllViews()
        root.addView(label("Why do you need access?", 22f, textMain, true))
        root.addView(label("Write a reason (at least 5 characters).", 14f, textDim))
        val reason = input("Your reason", false)
        root.addView(reason)

        val next = button("Continue", accent, bg)
        next.setOnClickListener {
            val r = reason.text.toString().trim()
            if (r.length < 5) {
                Toast.makeText(this, "Please write at least 5 characters", Toast.LENGTH_SHORT).show()
            } else {
                showCode(r)
            }
        }
        val back = button("Back", card, textMain)
        back.setOnClickListener { showBlocked() }
        root.addView(next)
        root.addView(back)
    }

    private fun showCode(reason: String) {
        val prefs = getSharedPreferences("timeiq", MODE_PRIVATE)
        val stored = prefs.getString("code_hash", null)

        root.removeAllViews()
        if (stored == null) {
            root.addView(label("Create a Management Code", 22f, textMain, true))
            root.addView(label("Choose 4 to 8 digits. You will need it to unlock apps. Remember it.", 14f, textDim))
        } else {
            root.addView(label("Enter Management Code", 22f, textMain, true))
        }
        val code = input("Management Code", true)
        root.addView(code)

        val ok = button(if (stored == null) "Save code and unlock" else "Unlock", accent, bg)
        ok.setOnClickListener {
            val c = code.text.toString()
            if (stored == null) {
                if (c.length < 4 || c.length > 8) {
                    Toast.makeText(this, "Code must be 4 to 8 digits", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                prefs.edit().putString("code_hash", hash(c)).apply()
                grant(reason)
            } else if (hash(c) == stored) {
                grant(reason)
            } else {
                Toast.makeText(this, "Wrong code", Toast.LENGTH_SHORT).show()
            }
        }
        val back = button("Back", card, textMain)
        back.setOnClickListener { showReason() }
        root.addView(ok)
        root.addView(back)
    }

    private fun grant(reason: String) {
        val prefs = getSharedPreferences("timeiq", MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val log = prefs.getString("unlock_log", "") ?: ""
        val line = now.toString() + "|" + reason.replace("\n", " ").replace("|", "/")
        prefs.edit()
            .putLong("unlock_until", now + 15 * 60000L)
            .putString("unlock_log", log + line + "\n")
            .apply()
        Toast.makeText(this, "Unlocked for 15 minutes", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun goHome() {
        val i = Intent(Intent.ACTION_MAIN)
        i.addCategory(Intent.CATEGORY_HOME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        goHome()
    }
}
