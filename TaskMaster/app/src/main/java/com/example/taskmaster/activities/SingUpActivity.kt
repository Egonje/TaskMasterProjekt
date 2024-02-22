package com.example.taskmaster.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.taskmaster.R
import com.example.taskmaster.firebase.FirebaseClass
import com.example.taskmaster.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Suppress("DEPRECATION")
class SingUpActivity : MainExtendActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up)
        setupActionBar()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this,
            "you have " + "successfully registered",
            Toast.LENGTH_LONG
        )
            .show()
            hideProgressDialog()
        finish()

    }

    private fun setupActionBar() {
        val toolbarSignUpActivity: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_sign_up_activity)
        setSupportActionBar(toolbarSignUpActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24)
        }
        toolbarSignUpActivity.setNavigationOnClickListener {
            onBackPressed()
        }

        val btnSingUp: Button = findViewById(R.id.btn_sign_up)
        btnSingUp.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val etName: EditText = findViewById(R.id.et_name)
        val name: String = etName.text.toString().trim { it <= ' '}
        val etEmail: EditText = findViewById(R.id.et_email)
        val email: String = etEmail.text.toString().trim { it <= ' '}
        val etPassword: EditText = findViewById(R.id.et_password)
        val password: String = etPassword.text.toString().trim { it <= ' '}

        if (validateForm(name, email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val fireBaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = fireBaseUser.email!!
                        val user = UserModel(fireBaseUser.uid, name, registeredEmail)
                        FirebaseClass().registerUser(this, user)
                    } else {
                        Toast.makeText(
                            this,
                            task.exception!!.message, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }


    private fun validateForm(name: String,
                             email: String, password: String) : Boolean{
        return when {
            TextUtils.isEmpty(name) ->{
                showErrorSnackBar("Please enter a name")
                false
            } TextUtils.isEmpty(email) ->{
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