package com.example.taskmaster.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskmaster.R
import com.example.taskmaster.adapters.ItemsOfBoardAdapter
import com.example.taskmaster.firebase.FirebaseClass
import com.example.taskmaster.models.TaskBoardModel
import com.example.taskmaster.models.UserModel
import com.example.taskmaster.utils.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView

@Suppress("DEPRECATION")
class MainActivity : MainExtendActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }
    private lateinit var  mUserName: String
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomNavigationView()

        setupActionBar()
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(
            Constants.TASKMASTER_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences
            .getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseClass().loadUserData(this, true)
        }else {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                updateFCMToken(token)
            }.addOnFailureListener { exception ->
                Log.e("FCM", "Not working: ${exception.message}")
            }
        }
        FirebaseClass().loadUserData(this, true)

        val fabCreateBoard: FloatingActionButton = findViewById(R.id.fab_create_board)
        fabCreateBoard.setOnClickListener {
            val intent = Intent(this,
                CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)

        }

    }

    @SuppressLint("CutPasteId")
    fun populateBoardsListToUI(boardsList: ArrayList<TaskBoardModel>){
        hideProgressDialog()
        if (boardsList.size > 0) {
            findViewById<RecyclerView>(R.id.re_boards_list).visibility = View.VISIBLE
            findViewById<TextView>(R.id.noBoardsAvaible).visibility = View.GONE

            findViewById<RecyclerView>(R.id.re_boards_list).layoutManager = LinearLayoutManager(this)
            findViewById<RecyclerView>(R.id.re_boards_list).setHasFixedSize(true)

            val adapter = ItemsOfBoardAdapter(this, boardsList)
            findViewById<RecyclerView>(R.id.re_boards_list).adapter = adapter

            adapter.setOnClickListener(object: ItemsOfBoardAdapter.OnClickListener{
                override fun onClick(position: Int, model: TaskBoardModel) {
                    val intent = Intent(this@MainActivity, ListOfTasksActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        }

        else {
            findViewById<RecyclerView>(R.id.re_boards_list).visibility = View.GONE
            findViewById<TextView>(R.id.noBoardsAvaible).visibility = View.VISIBLE
        }
    }
    private fun toggleDrawer() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupActionBar() {
        val toolbarMA: Toolbar = findViewById(R.id.toolbar_main_activity)
        setSupportActionBar(toolbarMA)
        toolbarMA.setNavigationIcon(R.drawable.baseline_menu_24)

        toolbarMA.setNavigationOnClickListener {
            toggleDrawer()
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

    fun updateNavigationUserDetails(user: UserModel, readBoardsList: Boolean) {
        hideProgressDialog()
        mUserName = user.name
        val navUserImage: CircleImageView = findViewById(R.id.iv_user_image)
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.user)
            .into(navUserImage)

        val tvUserName: TextView = findViewById(R.id.tv_username)
        tvUserName.text = user.name

        val tvEmail: TextView = findViewById(R.id.tv_email)
        tvEmail.text = user.email



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
        }else if(resultCode == Activity.RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirebaseClass().getBoardsList(this)
        }

        else {
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        when(item.itemId){
            R.id.nav_my_profile -> {
                startActivityForResult(
                    Intent(this,
                        ProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE)
            }
            R.id.about_app ->{
                val intent = Intent(this@MainActivity, AboutAppActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()
                val intent = Intent(this, SingUpOrSingInActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)

        return true
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
                        Intent(this,
                            MainActivity::class.java),
                        MY_PROFILE_REQUEST_CODE
                    )
                    finish()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.about_app->{
                    Toast.makeText(this, "This function will be developed in the future", Toast.LENGTH_SHORT).show()
                }
                R.id.settings ->{
                    Toast.makeText(this, "This function will be developed in the future", Toast.LENGTH_SHORT).show()
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
                // Add cases for other BottomNavigationView items as needed
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
}