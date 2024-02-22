package com.example.taskmaster.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.taskmaster.R
import com.example.taskmaster.firebase.FirebaseClass
import com.example.taskmaster.models.UserModel
import com.example.taskmaster.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

@Suppress("DEPRECATION")
class ProfileActivity : MainExtendActivity() {

    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserDetails: UserModel
    private var mProfileImageURL : String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)


        FirebaseClass().loadUserData(this)

        val ivProfileImage: CircleImageView = findViewById(R.id.iR_user_mage)
        ivProfileImage.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
              == PackageManager.PERMISSION_GRANTED ){
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISION_CODE
                )
            }
        }

        val toolbarSignInActivity: ImageView = findViewById(R.id.back_arrow)
        toolbarSignInActivity.setOnClickListener {
            onBackPressed()
        }

        val btnUpdate: Button = findViewById(R.id.btn_update)
        btnUpdate.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))

                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Constants.showImageChooser(this)
            } else {
                // TODO
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                Constants.READ_STORAGE_PERMISION_CODE
            )
        } else {
            Constants.showImageChooser(this)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null){
            mSelectedImageFileUri = data.data
            val navUserImage: CircleImageView = findViewById(R.id.iR_user_mage)
            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.user)
                    .into(navUserImage)
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }


    fun setUserDataInUI(user: UserModel){

        mUserDetails = user
        val navUserImage: CircleImageView = findViewById(R.id.iR_user_mage)
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.user)
            .into(navUserImage)

        val tvUserName: EditText = findViewById(R.id.ett_name)
        val etEmail: EditText = findViewById(R.id.ete_email)
        val etNumber: EditText = findViewById(R.id.et_mobile)
        tvUserName.setText(user.name)
        etEmail.setText(user.email)
        if (user.mobile != 0L) {
            etNumber.setText(user.mobile.toString())
        }
    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }
        val etName: EditText = findViewById(R.id.ett_name)
        val etMobile: EditText = findViewById(R.id.et_mobile)

        if (etName.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = etName.text.toString()
        }
        val mobileText = etMobile.text.toString()
        if (mobileText.isNotEmpty() && mobileText != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = mobileText.toLong()
        }
        FirebaseClass().updateProfileDataOfUser(this, userHashMap)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage
                .getInstance().reference
                .child("USER_IMAGE"
                        + System.currentTimeMillis()
                        + "." + Constants.getFileExstension(this, mSelectedImageFileUri))

                sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                    taskSnapshot ->
                    Log.i(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri->
                        Log.i("Downloadable Image URL", uri.toString())
                        mProfileImageURL = uri.toString()
                        updateUserProfileData()
                    }.addOnFailureListener {
                        exception ->
                        Toast.makeText(
                            this@ProfileActivity,
                            exception.message,
                            Toast.LENGTH_SHORT
                        ).show()

                        hideProgressDialog()
                    }
                }
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }
}