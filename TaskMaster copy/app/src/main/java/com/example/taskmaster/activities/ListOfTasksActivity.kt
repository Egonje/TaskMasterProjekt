package com.example.taskmaster.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.taskmaster.R
import com.example.taskmaster.models.CardModel
import com.example.taskmaster.models.TaskModel
import com.example.taskmaster.adapters.TaskListItemsAdapter
import com.example.taskmaster.firebase.FirebaseClass
import com.example.taskmaster.models.TaskBoardModel
import com.example.taskmaster.models.UserModel
import com.example.taskmaster.utils.Constants

@Suppress("DEPRECATION")
class ListOfTasksActivity : MainExtendActivity() {

    private lateinit var mBoardDetails: TaskBoardModel
    private lateinit var mDocumentBoardId: String
    lateinit var mAssingendFriendDetails: ArrayList<UserModel>
    private var SelectedImageUri: Uri? = null
    private val IMAGE_PICKER_REQUEST_CODE = 15
    private var mTaskListPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mDocumentBoardId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().getBoardDetails(this, mDocumentBoardId)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && (requestCode == FRIENDS_REQUEST_CODE || requestCode == CARD_DETAIL_REQUEST_CODE)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseClass().getBoardDetails(this, mDocumentBoardId)
        } else if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                showSelectedImage(uri)
            }
        } else {
            // TODO
        }
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this, DetailsOfCard::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssingendFriendDetails)
        startActivityForResult(intent, CARD_DETAIL_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_friends, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_friends -> {
                val intent = Intent(this, ActivityForFriends::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivityForResult(intent, FRIENDS_REQUEST_CODE)
                return true
            }
            R.id.set_background -> {
                Imagepicker()
                return true
            }
            R.id.remove_background ->{
                removeBackground()
            }
            R.id.small_top_Bar ->{
                val toolbar = findViewById<Toolbar>(R.id.toolbar_task_list_activity)
                toolbar.title = null
                val params = toolbar.layoutParams as LinearLayout.LayoutParams
                params.height = resources.getDimensionPixelSize(R.dimen.toolbar_height)
                toolbar.layoutParams = params

                Toast.makeText(this, "This function is only visually working, not saving in data base", Toast.LENGTH_SHORT).show()
            }
            R.id.big_top_Bar ->{
                val toolbar = findViewById<Toolbar>(R.id.toolbar_task_list_activity)
                toolbar.title = mBoardDetails.name
                val params = toolbar.layoutParams as LinearLayout.LayoutParams
                params.height = resources.getDimensionPixelSize(R.dimen.toolbar_height_big)
                toolbar.layoutParams = params
            }
            R.id.delete_board ->{
                startActivityForResult(
                    Intent(this,
                        MainActivity::class.java),
                    MainActivity.MY_PROFILE_REQUEST_CODE
                )
                FirebaseClass().deleteBoard(this@ListOfTasksActivity, mDocumentBoardId)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun removeBackground() {
        val linearLayout = findViewById<LinearLayout>(R.id.ll_of_rv_task_list)
        linearLayout.setBackgroundResource(0)


        mBoardDetails.backgroundImage = ""

        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().removeBoardBackgroundImage(this@ListOfTasksActivity, mDocumentBoardId)
    }

    private fun setupActionBar() {
        val toolbarMA: Toolbar = findViewById(R.id.toolbar_task_list_activity)
        setSupportActionBar(toolbarMA)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_white_ios_24)
            actionBar.title = mBoardDetails.name
        }
        toolbarMA.setNavigationOnClickListener { onBackPressed() }
    }
    fun deleteAllCardsInTask() {

        val cardsList: ArrayList<CardModel> = mBoardDetails.taskList[mTaskListPosition].cards

        cardsList.clear()

        val taskList: ArrayList<TaskModel> = mBoardDetails.taskList

        taskList.removeAt(taskList.size - 1)

        taskList[mTaskListPosition].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))

        FirebaseClass().UpdateTaskList(this, mBoardDetails)
    }

    fun boardDetails(board: TaskBoardModel) {
        mBoardDetails = board
        hideProgressDialog()
        setupActionBar()
        if (mBoardDetails.backgroundImage.isNotEmpty()) {
            val uri = Uri.parse(mBoardDetails.backgroundImage)
            showSelectedImage(uri)
        } else {
        }
        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().getFriendsAssigne(this, mBoardDetails.assignedTo)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().getBoardDetails(this, mBoardDetails.documentId)
    }


    fun createListOfTask(taskListName: String) {
        val task = TaskModel(taskListName, FirebaseClass().getCurrentUserId())
        mBoardDetails.taskList.add(0, task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().UpdateTaskList(this, mBoardDetails)
    }

    fun deleteTaskList(position: Int) {
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().UpdateTaskList(this, mBoardDetails)
    }
    
    fun updateListOfTasks(position: Int, listName: String, model: TaskModel) {
        val updatedTask = TaskModel(listName, model.createdBy, model.cards)
        mBoardDetails.taskList[position] = updatedTask
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().UpdateTaskList(this, mBoardDetails)
    }


    fun addCardToTaskList(position: Int, cardName: String) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirebaseClass().getCurrentUserId())
        val card = CardModel(cardName, FirebaseClass().getCurrentUserId(), cardAssignedUsersList)
        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)
        val task = TaskModel(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )
        mBoardDetails.taskList[position] = task
        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().UpdateTaskList(this@ListOfTasksActivity, mBoardDetails)
    }

    @SuppressLint("CutPasteId")
    fun boardFriendsDetailList(list: ArrayList<UserModel>) {
        mAssingendFriendDetails = list
        hideProgressDialog()
        val addTaskList = TaskModel(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)
        findViewById<RecyclerView>(
            R.id.rv_task_list
        ).layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false
            )
        findViewById<RecyclerView>(R.id.rv_task_list).setHasFixedSize(true)
        val adapter = TaskListItemsAdapter(this, mBoardDetails.taskList)
        findViewById<RecyclerView>(R.id.rv_task_list).adapter = adapter
    }

    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<CardModel>) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)
        mBoardDetails.taskList[taskListPosition].cards = cards
        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().UpdateTaskList(this@ListOfTasksActivity, mBoardDetails)
    }


    private fun Imagepicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }

    fun showSelectedImage(uri: Uri) {
        val linearLayout = findViewById<LinearLayout>(R.id.ll_of_rv_task_list)
        linearLayout.setBackgroundResource(0)

        if (!isFinishing && !isDestroyed) {
            Glide.with(this).clear(linearLayout)

            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_cancle)
                .error(R.drawable.ic_cancle)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        linearLayout.background = resource
                        FirebaseClass().uploadBoardBackgroundImage(this@ListOfTasksActivity, mDocumentBoardId, uri)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }
    }
    companion object {
        const val FRIENDS_REQUEST_CODE: Int = 13
        const val CARD_DETAIL_REQUEST_CODE: Int = 14
    }
}
