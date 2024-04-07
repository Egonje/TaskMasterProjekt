package com.example.taskmaster.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.example.taskmaster.R

class AllCompletedTasks : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    var tvValue: TextView? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_completed_tasks)
        AnimationUtils.loadAnimation(this, R.anim.top_animation)
        val bottomAnimate = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)
        val tvAppName: TextView = findViewById(R.id.allCompletedTextView)
        tvValue = findViewById(R.id.allCompletedTextViewValue)
        val floatValue1 = intent.getFloatExtra("floatValue1", 0.0f)
        val floatValue2 = intent.getFloatExtra("floatValue2", 0.0f)
        tvValue?.text = "$floatValue1 / $floatValue2"
        val tvAppNameGoBack: TextView = findViewById(R.id.allCompletedTextViewBack)
        Handler().postDelayed({
            tvAppNameGoBack.visibility = View.VISIBLE
            tvAppNameGoBack.startAnimation(bottomAnimate)
        }, 2500)
        tvAppNameGoBack.setOnClickListener {
            finish()
        }
        tvValue?.animation = bottomAnimate
        val animationView1 = findViewById<LottieAnimationView>(R.id.animateViewAllCompleted)
        tvAppName.animation = bottomAnimate

        animationView1.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                animationView1.postDelayed({
                    animationView1.playAnimation()
                }, 1500)
            }
        })
    }
}