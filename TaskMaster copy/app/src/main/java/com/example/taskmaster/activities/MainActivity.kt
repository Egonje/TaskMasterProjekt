package com.example.taskmaster.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.R
import com.example.taskmaster.adapters.ItemsOfBoardAdapter
import com.example.taskmaster.firebase.FirebaseClass
import com.example.taskmaster.models.TaskBoardModel
import com.example.taskmaster.models.UserModel
import com.example.taskmaster.utils.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

@Suppress("DEPRECATION")
class MainActivity : MainExtendActivity() {

    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }

    private lateinit var mUserName: String
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomNavigationView()
        setupActionBar()

        mSharedPreferences = this.getSharedPreferences(
            Constants.TASKMASTER_PREFERENCES, Context.MODE_PRIVATE
        )

        val tokenUpdated = mSharedPreferences
            .getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseClass().loadUserData(this, true)
        } else {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                updateFCMToken(token)
            }.addOnFailureListener { exception ->
                Log.e("FCM", "Not working: ${exception.message}")
            }
        }
        FirebaseClass().loadUserData(this, true)

        val fabCreateBoard: FloatingActionButton = findViewById(R.id.fab_create_board)
        fabCreateBoard.setOnClickListener {
            val intent = Intent(
                this,
                CreateBoardActivity::class.java
            )
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)

        }

    }

    @SuppressLint("CutPasteId")
    fun populateBoardsListToUI(boardsList: ArrayList<TaskBoardModel>) {
        hideProgressDialog()
        if (boardsList.size > 0) {
            findViewById<RecyclerView>(R.id.re_boards_list).visibility = View.VISIBLE
            findViewById<TextView>(R.id.noBoardsAvaible).visibility = View.GONE

            val recyclerView = findViewById<RecyclerView>(R.id.re_boards_list)
            recyclerView.layoutManager = GridLayoutManager(this, 2) // Postavljamo GridLayoutManager s 2 stupca
            recyclerView.setHasFixedSize(true)

            val adapter = ItemsOfBoardAdapter(this, boardsList)
            recyclerView.adapter = adapter

            adapter.setOnClickListener(object : ItemsOfBoardAdapter.OnClickListener {
                override fun onClick(position: Int, model: TaskBoardModel) {
                    val intent = Intent(this@MainActivity, ListOfTasksActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        } else {
            findViewById<RecyclerView>(R.id.re_boards_list).visibility = View.GONE
            findViewById<TextView>(R.id.noBoardsAvaible).visibility = View.VISIBLE
        }
    }

    private fun setupActionBar() {
        val toolbarMA: Toolbar = findViewById(R.id.toolbar_main_activity)
        setSupportActionBar(toolbarMA)
        toolbarMA.setNavigationIcon(R.drawable.baseline_menu_24)

        toolbarMA.setNavigationOnClickListener {
            showBottomDialog()
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    private fun showBottomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_layout)

        val videoLayout: LinearLayout = dialog.findViewById(R.id.layoutVideo)
        val shortsLayout: LinearLayout = dialog.findViewById(R.id.layoutShorts)
        val liveLayout: LinearLayout = dialog.findViewById(R.id.layoutLive)
        val cancelButton: ImageView = dialog.findViewById(R.id.cancelButton)

        videoLayout.setOnClickListener {
            startActivityForResult(
                Intent(
                    this,
                    ProfileActivity::class.java
                ),
                MY_PROFILE_REQUEST_CODE
            )
        }

        shortsLayout.setOnClickListener {
            val intent = Intent(this@MainActivity, AboutAppActivity::class.java)
            startActivity(intent)
        }

        liveLayout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            mSharedPreferences.edit().clear().apply()

            val intent = Intent(this, SingUpOrSingInActivity::class.java)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)

            finish()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }


    fun updateNavigationUserDetails(user: UserModel, readBoardsList: Boolean) {
        hideProgressDialog()
        mUserName = user.name

        if (readBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseClass().getBoardsList(this)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirebaseClass().loadUserData(this)
        } else if (resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FirebaseClass().getBoardsList(this)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }
    fun tokenUpdateSuccess() {

        hideProgressDialog()

        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()


        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().loadUserData(this@MainActivity, true)
    }

    private fun setupBottomNavigationView() {
        bottomNavigationView = findViewById(R.id.bnv_menu)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    startActivityForResult(
                        Intent(
                            this,
                            MainActivity::class.java
                        ),
                        MY_PROFILE_REQUEST_CODE
                    )
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.feedback -> {
                    dialogForFeedback()
                }

                R.id.statistics -> {
                    startActivityForResult(
                        Intent(
                            this,
                            StatiscsActivity::class.java
                        ),
                        MY_PROFILE_REQUEST_CODE
                    )
                }

                R.id.nav_sign_out_bottom -> {
                    FirebaseAuth.getInstance().signOut()
                    mSharedPreferences.edit().clear().apply()
                    val intent = Intent(this, SingUpOrSingInActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
    }

    private fun updateFCMToken(token: String) {

        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().updateProfileDataOfUser(this@MainActivity, userHashMap)
    }
    private fun dialogForFeedback() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Feedback")
        val message = "Please send your feedback to: <font size='30sp'><b>egon.tomic@skole.hr</b></font>"
        builder.setMessage(Html.fromHtml(message))
        builder.setIcon(R.drawable.fedback)

        builder.setNegativeButton("CLOSE") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.dialog_roundend_bg)
        alertDialog.show()
    }
}