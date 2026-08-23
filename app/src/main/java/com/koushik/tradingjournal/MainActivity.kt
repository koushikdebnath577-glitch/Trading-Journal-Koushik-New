package com.koushik.tradingjournal

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this)
        textView.text = "Trading Journal App Running Successfully!"
        textView.textSize = 22f
        textView.setPadding(60, 60, 60, 60)

        setContentView(textView)
    }
}
