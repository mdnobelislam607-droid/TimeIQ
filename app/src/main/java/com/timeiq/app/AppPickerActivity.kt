package com.timeiq.app

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AppPickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Ui.bg
        window.navigationBarColor = Ui.bg

        val prefs = getSharedPreferences("timeiq", MODE_PRIVATE)
        val chosen = HashSet(prefs.getStringSet("blocked", emptySet()) ?: emptySet())

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Ui.bg)
        root.setPadding(Ui.dp(this, 20), Ui.dp(this, 40), Ui.dp(this, 20), Ui.dp(this, 24))

                root.addView(Ui.backBar(this, "Select apps to control"))

        val launch = Intent(Intent.ACTION_MAIN)
        launch.addCategory(Intent.CATEGORY_LAUNCHER)
        val pm = packageManager
        val apps = pm.queryIntentActivities(launch, 0)
            .filter { it.activityInfo.packageName != packageName }
            .distinctBy { it.activityInfo.packageName }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        for (ri in apps) {
            val pkg = ri.activityInfo.packageName

            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8))

            val icon = ImageView(this)
            val ilp = LinearLayout.LayoutParams(Ui.dp(this, 40), Ui.dp(this, 40))
            ilp.marginEnd = Ui.dp(this, 14)
            icon.layoutParams = ilp
            icon.setImageDrawable(ri.loadIcon(pm))

            val name = Ui.text(this, ri.loadLabel(pm).toString(), 16f, Ui.textMain)
            name.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val cb = CheckBox(this)
            cb.isChecked = chosen.contains(pkg)
            cb.setOnCheckedChangeListener { _, on ->
                if (on) chosen.add(pkg) else chosen.remove(pkg)
                prefs.edit().putStringSet("blocked", HashSet(chosen)).apply()
            }

            row.setOnClickListener { cb.toggle() }
            row.addView(icon)
            row.addView(name)
            row.addView(cb)
            root.addView(row)
        }

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Ui.bg)
        scroll.addView(root)
        setContentView(scroll)
    }
}
