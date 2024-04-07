package com.example.taskmaster.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import com.example.taskmaster.R

@Suppress("DEPRECATION")
class SingUpOrSingInActivity : MainExtendActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val btnSignUp: Button = findViewById(R.id.btn_sign_up_intro)
        val btnSingIn: Button = findViewById(R.id.btn_sign_in_intro)

        btnSignUp.setOnClickListener {
            startActivity(Intent(this, SingUpActivity::class.java))
        }
        btnSingIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}