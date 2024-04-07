package com.example.taskmaster.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.example.taskmaster.R
import com.example.taskmaster.firebase.FirebaseClass

@Suppress("DEPRECATION")
class ActivitySplash : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_activty)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val tvAppName: TextView = findViewById(R.id.tv_app_name)
        val typeFace: Typeface = Typeface.createFromAsset(assets, "Bangers-Regular.ttf")

        AnimationUtils.loadAnimation(this, R.anim.top_animation)
        val bottomAnimate = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        tvAppName.animation = bottomAnimate

        tvAppName.typeface = typeFace

        Handler().postDelayed({

            val currentUserID = FirebaseClass().getCurrentUserId()

            if (currentUserID.isNotEmpty()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, SingUpOrSingInActivity::class.java))
            }
            finish()
        }, 2500)
    }
}