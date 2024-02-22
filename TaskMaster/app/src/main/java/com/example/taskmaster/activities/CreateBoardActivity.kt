package com.example.taskmaster.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.taskmaster.R
import com.example.taskmaster.firebase.FirebaseClass
import com.example.taskmaster.models.TaskBoardModel
import com.example.taskmaster.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class CreateBoardActivity : MainExtendActivity() {

    private var mSelectedImageFileUri : Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageURL: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)


        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }
        setupActionBar()

        val ivProfileImage: CircleImageView = findViewById(R.id.iv_board_image)
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
        val btnCreateBoard: Button = findViewById(R.id.btn_create)
        btnCreateBoard.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadBoardImage()
            }else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    private fun createBoard(){
        val assignedUserArrayList: ArrayList<String> = ArrayList()
        assignedUserArrayList.add(getCurrentUserID())
        val etBoardName: EditText = findViewById(R.id.et_board_name)

        var board = TaskBoardModel(
            etBoardName.text.toString(),
            mBoardImageURL,
            mUserName,
            assignedUserArrayList
        )

        FirebaseClass().createBoard(this, board)
    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        val sRef: StorageReference = FirebaseStorage
            .getInstance().reference
            .child(
                "BOARD_IMAGE"
                        + System.currentTimeMillis()
                        + "." + Constants.getFileExstension(this, mSelectedImageFileUri)
            )

        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
            Log.i(
                "Firebase Board Image URL",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
            )

            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                Log.i("Downloadable Image URL", uri.toString())
                mBoardImageURL = uri.toString()

                createBoard()
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    exception.message,
                    Toast.LENGTH_SHORT
                ).show()

                hideProgressDialog()
            }
        }
    }

        fun boardCreatedSuccessfully() {
            hideProgressDialog()

            setResult(Activity.RESULT_OK)
            finish()
        }


        private fun setupActionBar() {
            val toolbarMA: Toolbar = findViewById(R.id.toolbar_create_board_activity)
            setSupportActionBar(toolbarMA)
            val actionBar = supportActionBar
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_white_ios_24)
                actionBar.title = resources.getString(R.string.create_board_title)
            }
            toolbarMA.setNavigationOnClickListener { onBackPressed() }

        }


        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == Constants.READ_STORAGE_PERMISION_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Constants.showImageChooser(this)
                } else {
                }
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISION_CODE
                )
            } else {
                Constants.showImageChooser(this)
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
                && data!!.data != null
            ) {
                mSelectedImageFileUri = data.data
                val navUserImage: CircleImageView = findViewById(R.id.iv_board_image)
                try {
                    Glide
                        .with(this)
                        .load(mSelectedImageFileUri)
                        .centerCrop()
                        .placeholder(R.drawable.picture)
                        .into(navUserImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
}