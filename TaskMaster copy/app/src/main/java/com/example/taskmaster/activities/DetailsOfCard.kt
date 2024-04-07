package com.example.taskmaster.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.R
import com.example.taskmaster.adapters.CardFriendsListItemsAdapter
import com.example.taskmaster.dialogs.LabelColorListDialog
import com.example.taskmaster.dialogs.FriendsListDialog
import com.example.taskmaster.firebase.FirebaseClass
import com.example.taskmaster.models.TaskBoardModel
import com.example.taskmaster.models.CardModel
import com.example.taskmaster.models.SelectedFriendsModel
import com.example.taskmaster.models.TaskModel
import com.example.taskmaster.models.UserModel
import com.example.taskmaster.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION", "NAME_SHADOWING")
class DetailsOfCard : MainExtendActivity() {
    private lateinit var mBoardDetails: TaskBoardModel
    private var mTaskListPosition = -1
    private var mCardPosition = -1
    private var mSelectedColor: String = ""
    private lateinit var mFriendsDetailList: ArrayList<UserModel>
    private var mSelectedDueDateMillisSecond: Long = 0
    private var mSelectedDueTimeMillisSecond: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        getIntentData()

        setupActionBar()

        findViewById<EditText>(R.id.et_name_card_details).setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        findViewById<EditText>(R.id.et_name_card_details).setSelection(findViewById<EditText>(R.id.et_name_card_details).text.toString().length)

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if (mSelectedColor.isNotEmpty()) {
            setColor()
        }

        findViewById<TextView>(R.id.tv_select_color).setOnClickListener {
            ColorsListDialog()
        }

        setupSelectedFriendList()

        findViewById<TextView>(R.id.tv_select_friends).setOnClickListener {
            friendsListDialog()
        }

        mSelectedDueTimeMillisSecond =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].time
        mSelectedDueDateMillisSecond =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate
        if (mSelectedDueTimeMillisSecond > 0) {
            val simpleTimeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val selectedTime = simpleTimeFormat.format(Date(mSelectedDueTimeMillisSecond))
            findViewById<TextView>(R.id.tv_select_time).text = selectedTime
        }
        if (mSelectedDueDateMillisSecond > 0) {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            if (mBoardDetails.taskList.isNotEmpty() &&
                mBoardDetails.taskList[mTaskListPosition].cards.isNotEmpty()
            ) {
                val selectedDate =
                    simpleDateFormat.format(Date(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate))
                findViewById<TextView>(R.id.tv_select_date).text = selectedDate
            }

        }

        findViewById<TextView>(R.id.tv_select_date).setOnClickListener {

            showDataPicker()
        }

        findViewById<Button>(R.id.btn_update_card_details).setOnClickListener {
            if (findViewById<EditText>(R.id.et_name_card_details).text.toString().isNotEmpty()) {
                updateCardDetails()
            } else {
                Toast.makeText(this@DetailsOfCard, "Enter card name.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
                return true
            }
            R.id.action_complete_card -> {
                alertDialogForCompleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar() {
        setSupportActionBar(findViewById(R.id.toolbar_card_details_activity))
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_white_ios_24)

            if (mTaskListPosition >= 0 && mCardPosition >= 0 &&
                mTaskListPosition < mBoardDetails.taskList.size &&
                mCardPosition < mBoardDetails.taskList[mTaskListPosition].cards.size
            ) {
                actionBar.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name
            } else {
            }
        }
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_card_details_activity).setNavigationOnClickListener { onBackPressed() }
    }

    private fun getIntentData() {

        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            val boardExtra = intent.getParcelableExtra(Constants.BOARD_DETAIL) as? TaskBoardModel
            if (boardExtra != null) {
                mBoardDetails = boardExtra
            } else {
            }
        }

        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mFriendsDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    fun addUpdateTaskListSuccess() {

        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateCardDetails() {
        val card = CardModel(
            findViewById<EditText>(R.id.et_name_card_details).text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMillisSecond,
            mSelectedDueTimeMillisSecond // Ovdje koristimo mSelectedDueTimeMillisSecond za vrijeme
        )

        val taskList: ArrayList<TaskModel> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().UpdateTaskList(this@DetailsOfCard, mBoardDetails)
    }



    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(R.drawable.warning)

        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun alertDialogForCompleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_complete_card,
                cardName
            )
        )
        builder.setIcon(R.drawable.alert_complete_card)

        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->
            dialogInterface.dismiss()
            completeCard()
        }
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun deleteCard() {

        val cardsList: ArrayList<CardModel> = mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardPosition)

        val taskList: ArrayList<TaskModel> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        taskList[mTaskListPosition].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))
        FirebaseClass().UpdateTaskList(this@DetailsOfCard, mBoardDetails)
    }
    private fun completeCard() {
        val firebaseClass = FirebaseClass()

        // Dohvati trenutnog korisnika
        val currentUserId = firebaseClass.getCurrentUserId()

        // Povećaj broj završenih zadataka
        firebaseClass.mFireStore.collection(Constants.USERS)
            .document(currentUserId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val userModel = documentSnapshot.toObject(UserModel::class.java)
                userModel?.let {
                    deleteCard()
                    val updatedCompletedTasks = it.completedTasks + 1

                    // Kreiraj mapu za ažuriranje podataka korisnika
                    val userHashMap = hashMapOf<String, Any>(
                        "completedTasks" to updatedCompletedTasks
                    )

                    // Ažuriraj podatke korisnika na Firebase
                    firebaseClass.updateProfileDataOfUser(this, userHashMap)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to do operation", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setColor() {
        findViewById<TextView>(R.id.tv_select_color).text = ""
        findViewById<TextView>(R.id.tv_select_color).setBackgroundColor(
            Color.parseColor(
                mSelectedColor
            )
        )
    }

    private fun colorsList(): ArrayList<String> {

        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#5733FF")// Indigo
        colorsList.add("#0C90F1")// Azure
        colorsList.add("#FFFF00")// Žuta
        colorsList.add("#0000FF")// Plava
        colorsList.add("#FF0000")// Crvena
        colorsList.add("#00FF00")// Zelena
        colorsList.add("#8B008B")

        return colorsList
    }


    private fun ColorsListDialog() {

        val colorsList: ArrayList<String> = colorsList()

        val listDialog = object : LabelColorListDialog(
            this@DetailsOfCard,
            colorsList,
            resources.getString(R.string.str_select_color),
            mSelectedColor
        ) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun friendsListDialog() {

        val cardAssignedFriendsList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        if (cardAssignedFriendsList.size > 0) {
            for (i in mFriendsDetailList.indices) {
                for (j in cardAssignedFriendsList) {
                    if (mFriendsDetailList[i].id == j) {
                        mFriendsDetailList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mFriendsDetailList.indices) {
                mFriendsDetailList[i].selected = false
            }
        }

        val listDialog = object : FriendsListDialog(
            this@DetailsOfCard,
            mFriendsDetailList,
            resources.getString(R.string.str_select_friend)
        ) {
            override fun onItemSelected(user: UserModel, action: String) {

                if (action == Constants.SELECT) {
                    if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(
                            user.id
                        )
                    ) {
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(
                            user.id
                        )
                    }
                } else {
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(
                        user.id
                    )

                    for (i in mFriendsDetailList.indices) {
                        if (mFriendsDetailList[i].id == user.id) {
                            mFriendsDetailList[i].selected = false
                        }
                    }
                }

                setupSelectedFriendList()
            }
        }
        listDialog.show()
    }

    @SuppressLint("CutPasteId")
    private fun setupSelectedFriendList() {

        val cardAssignedFriendsList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        val selectedFriendsList: ArrayList<SelectedFriendsModel> = ArrayList()

        for (i in mFriendsDetailList.indices) {
            for (j in cardAssignedFriendsList) {
                if (mFriendsDetailList[i].id == j) {
                    val selectedFriend = SelectedFriendsModel(
                        mFriendsDetailList[i].id,
                        mFriendsDetailList[i].image
                    )

                    selectedFriendsList.add(selectedFriend)
                }
            }
        }

        if (selectedFriendsList.size > 0) {

            selectedFriendsList.add(SelectedFriendsModel("", ""))

            findViewById<TextView>(R.id.tv_select_friends).visibility = View.GONE
            findViewById<RecyclerView>(R.id.rv_selected_friends_list).visibility = View.VISIBLE

            findViewById<RecyclerView>(R.id.rv_selected_friends_list).layoutManager =
                GridLayoutManager(this@DetailsOfCard, 6)
            val adapter =
                CardFriendsListItemsAdapter(this@DetailsOfCard, selectedFriendsList, true)
            findViewById<RecyclerView>(R.id.rv_selected_friends_list).adapter = adapter
            adapter.setOnClickListener(object :
                CardFriendsListItemsAdapter.OnClickListener {
                override fun onClick() {
                    friendsListDialog()
                }
            })
        } else {
            findViewById<TextView>(R.id.tv_select_friends).visibility = View.VISIBLE
            findViewById<RecyclerView>(R.id.rv_selected_friends_list).visibility = View.GONE
        }
    }

    private fun showDataPicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val dpd = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                        val sMonthOfYear = if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"
                        val sHourOfDay = if (hourOfDay < 10) "0$hourOfDay" else "$hourOfDay"
                        val sMinute = if (minute < 10) "0$minute" else "$minute"

                        val selectedDateTime = "$sDayOfMonth/$sMonthOfYear/$year"
                        val selectedTime = "$sHourOfDay:$sMinute"

                        findViewById<TextView>(R.id.tv_select_date).text = selectedDateTime

                        findViewById<TextView>(R.id.tv_select_time).text = selectedTime

                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                        val sdt = SimpleDateFormat("HH:mm", Locale.ENGLISH)

                        val theTime = sdt.parse(selectedTime)
                        val theDate = sdf.parse(selectedDateTime)

                        val cal = Calendar.getInstance().apply {
                            time = theDate!!
                        }
                        mSelectedDueDateMillisSecond = cal.timeInMillis

                        cal.apply {
                            time = theTime!!
                        }
                        mSelectedDueTimeMillisSecond = cal.timeInMillis

                    },
                    hour,
                    minute,
                    true
                ).show()
            },
            year,
            month,
            day
        )
        dpd.datePicker.minDate = System.currentTimeMillis() - 1000
        dpd.show()
    }
}