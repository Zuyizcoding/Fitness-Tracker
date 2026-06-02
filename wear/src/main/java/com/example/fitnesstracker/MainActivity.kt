package com.example.fitnesstracker

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.text = "Fitness Watch Face installed! Long press the watch face to apply it."
        setContentView(textView)
    }
}
