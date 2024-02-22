@file:Suppress("DEPRECATION")

package com.example.taskmaster.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.R
import com.example.taskmaster.adapters.FriendListItemsAdapter
import com.example.taskmaster.firebase.FirebaseClass
import com.example.taskmaster.models.TaskBoardModel
import com.example.taskmaster.models.UserModel
import com.example.taskmaster.utils.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

@Suppress("DEPRECATION")
class ActivityForFriends : MainExtendActivity() {
    private var anyChangesMade: Boolean = false
    private lateinit var mBoardDetails: TaskBoardModel
    private lateinit var mFriendsListAssigned:ArrayList<UserModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        actionBar()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().getFriendsAssigne(this,
            mBoardDetails.assignedTo)

    }

    @SuppressLint("CutPasteId")
    fun setupOfFriendsList(list: ArrayList<UserModel>){
        mFriendsListAssigned = list
        hideProgressDialog()

        findViewById<RecyclerView>(R.id.rv_friends_list).layoutManager = LinearLayoutManager(this)
        findViewById<RecyclerView>(R.id.rv_friends_list).setHasFixedSize(true)

        val adapter = FriendListItemsAdapter(this, list)
        findViewById<RecyclerView>(R.id.rv_friends_list).adapter = adapter
    }

    fun detailsOfFriend(user: UserModel) {

        mBoardDetails.assignedTo.add(user.id)

        FirebaseClass().assignFriendToBoard(this@ActivityForFriends, mBoardDetails, user)

    }

    private fun actionBar() {

        setSupportActionBar(findViewById(R.id.toolbar_members_activity))

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_white_ios_24)
        }

        findViewById<Toolbar>(R.id.toolbar_members_activity).setNavigationOnClickListener { onBackPressed() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_friend, menu)
        return super.onCreateOptionsMenu(menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_add_member ->{
                dialogSearchFriend()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchFriend() {
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_search_friend)
        dialog.findViewById<TextView>(R.id.tv_add).setOnClickListener {

            val email = dialog.findViewById<EditText>(R.id.et_email_search_member).text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirebaseClass().getDetailsOFFriends(this, email)
            } else {
                Toast.makeText(
                    this@ActivityForFriends,
                    "Please enter friends email address.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        dialog.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }
    fun friendsAssignSuccess(user: UserModel) {

        hideProgressDialog()

        mFriendsListAssigned.add(user)

        anyChangesMade = true

        setupOfFriendsList(mFriendsListAssigned)

        SendNotificationToUserAsyncTask(mBoardDetails.name, user.fcmToken).execute()
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SendNotificationToUserAsyncTask(val boardName: String, val token: String) :
        AsyncTask<Any, Void, String>() {


        @Deprecated("Deprecated in Java")
        override fun onPreExecute() {
            super.onPreExecute()


            showProgressDialog(resources.getString(R.string.please_wait))
        }


        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Any): String {
            var result: String


            var connection: HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection


                connection.doOutput = true
                connection.doInput = true


                connection.instanceFollowRedirects = false


                connection.requestMethod = "POST"


                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")


                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )

                connection.useCaches = false


                val wr = DataOutputStream(connection.outputStream)

                val jsonRequest = JSONObject()


                val dataObject = JSONObject()

                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the Board $boardName")

                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    "You have been assigned to the new board by ${mFriendsListAssigned[0].name}"
                )


                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)


                wr.writeBytes(jsonRequest.toString())
                wr.flush()

                val httpResult: Int =
                    connection.responseCode

                if (httpResult == HttpURLConnection.HTTP_OK) {


                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var line: String?
                    try {

                        while (reader.readLine().also { line = it } != null) {
                            sb.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {
                    result = connection.responseMessage
                }

            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }


            return result
        }


        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            hideProgressDialog()


            Log.e("JSON Response Result", result)
        }
    }
}

