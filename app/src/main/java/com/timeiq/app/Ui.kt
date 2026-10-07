package com.timeiq.app

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Process
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

object Ui {
    val bg = Color.parseColor("#0F172A")
    val cardC = Color.parseColor("#1E293B")
    val accent = Color.parseColor("#38BDF8")
    val orange = Color.parseColor("#FB923C")
    val green = Color.parseColor("#4ADE80")
    val textMain = Color.parseColor("#F1F5F9")
    val textDim = Color.parseColor("#94A3B8")

    fun dp(ctx: Context, v: Int): Int = (v * ctx.resources.displayMetrics.density).toInt()

    fun round(ctx: Context, color: Int, r: Int): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(color)
        g.cornerRadius = dp(ctx, r).toFloat()
        return g
    }

    fun text(ctx: Context, t: String, size: Float, color: Int, bold: Boolean = false): TextView {
        val v = TextView(ctx)
        v.text = t
        v.textSize = size
        v.setTextColor(color)
        if (bold) v.setTypeface(null, Typeface.BOLD)
        return v
    }

    fun card(ctx: Context): LinearLayout {
        val c = LinearLayout(ctx)
        c.orientation = LinearLayout.VERTICAL
        c.background = round(ctx, cardC, 16)
        c.setPadding(dp(ctx, 20), dp(ctx, 20), dp(ctx, 20), dp(ctx, 20))
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.topMargin = dp(ctx, 16)
        c.layoutParams = lp
        return c
    }

    fun button(ctx: Context, t: String, fill: Int, tc: Int): Button {
        val b = Button(ctx)
        b.text = t
        b.setTextColor(tc)
        b.background = round(ctx, fill, 12)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(ctx, 52))
        lp.topMargin = dp(ctx, 12)
        b.layoutParams = lp
        return b
    }

    fun numberInput(ctx: Context, hint: String): EditText {
        val e = EditText(ctx)
        e.hint = hint
        e.inputType = InputType.TYPE_CLASS_NUMBER
        e.setTextColor(textMain)
        e.setHintTextColor(textDim)
        e.background = round(ctx, bg, 10)
        e.setPadding(dp(ctx, 14), dp(ctx, 10), dp(ctx, 14), dp(ctx, 10))
        e.gravity = Gravity.CENTER
        val lp = LinearLayout.LayoutParams(0, dp(ctx, 48), 1f)
        lp.marginEnd = dp(ctx, 8)
        e.layoutParams = lp
        return e
    }

    fun hasUsageAccess(ctx: Context): Boolean {
        val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            ctx.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun startMonitor(ctx: Context) {
        if (!hasUsageAccess(ctx)) return
        val i = Intent(ctx, MonitorService::class.java)
        if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i) else ctx.startService(i)
    }

    fun fmtMs(ms: Long): String {
        val minutes = ms / 60000L
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    fun fmtMin(min: Int): String = fmtMs(min * 60000L)
}
