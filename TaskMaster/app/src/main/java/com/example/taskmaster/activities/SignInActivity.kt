package com.example.taskmaster.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.taskmaster.R
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class SignInActivity : MainExtendActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val btnSingIn: Button = findViewById(R.id.btn_sign_in)
        btnSingIn.setOnClickListener {
            signInRegisteredUser()
        }
        setupTheActionBard()
    }

    fun signInSuccess() {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    private fun setupTheActionBard() {
        val toolbarSignInActivity: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_sign_in_activity)
        setSupportActionBar(toolbarSignInActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        }
        toolbarSignInActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }


    private fun signInRegisteredUser() {
        val etEmail: EditText = findViewById(R.id.et_email_ding)
        val email: String = etEmail.text.toString().trim { it <= ' '}
        val etPassword: EditText = findViewById(R.id.et_password_ding)
        val password: String = etPassword.text.toString().trim { it <= ' '}

        if (validateFormOfSignInActivity(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        Log.d("Sign in", "createUserWithEmail:success")
                        auth.currentUser
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        Log.w("Sign in", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
    }
    private fun validateFormOfSignInActivity(email: String, password: String) : Boolean{
        return when {
             TextUtils.isEmpty(email) ->{
                showErrorSnackBar("Please enter an email address")
                false
            }
            TextUtils.isEmpty(password) ->{
                showErrorSnackBar("Please enter a password")
                false
            } else -> {
                true
            }
        }
    }
}