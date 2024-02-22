package com.example.taskmaster.activities

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.taskmaster.R

class AboutAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_app)
        setupActionBar()
    }

    private fun setupActionBar() {
        val toolbarMA: Toolbar = findViewById(R.id.toolbar_about_app)
        setSupportActionBar(toolbarMA)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_white_ios_24)
            actionBar.title = "About app"
        }
        toolbarMA.setNavigationOnClickListener { onBackPressed() }
    }
}