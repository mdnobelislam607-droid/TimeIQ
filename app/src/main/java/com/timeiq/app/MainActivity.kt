package com.timeiq.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this)
        text.text = "TimeIQ is running"
        text.textSize = 24f
        text.setPadding(48, 96, 48, 48)

        setContentView(text)
    }
}
