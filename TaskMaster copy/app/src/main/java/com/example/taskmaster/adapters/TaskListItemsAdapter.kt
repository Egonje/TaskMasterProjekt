package com.example.taskmaster.adapters

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.R
import com.example.taskmaster.activities.ListOfTasksActivity
import com.example.taskmaster.firebase.FirebaseClass
import com.example.taskmaster.models.TaskModel
import com.example.taskmaster.models.UserModel
import com.example.taskmaster.utils.Constants
import java.util.Collections

@Suppress("DEPRECATION", "UnusedEquals")
open class TaskListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<TaskModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)
        view.layoutParams = layoutParams

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]


        if (holder is MyViewHolder) {

            if (position == list.size - 1) {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.VISIBLE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility = View.GONE
            } else {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility = View.VISIBLE
            }

            holder.itemView.findViewById<TextView>(R.id.tv_task_list_title).text = model.title

            holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).setOnClickListener {

                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility = View.VISIBLE
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_close_list_name).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility = View.GONE
            }


            holder.itemView.findViewById<ImageButton>(R.id.ib_done_list_name).setOnClickListener {
                val listNameEditText = holder.itemView.findViewById<EditText>(R.id.et_task_list_name)
                val listName = listNameEditText.text.toString()

                if (listName.isNotEmpty()) {
                    if (context is ListOfTasksActivity) {
                        context.createListOfTask(listName)
                    }
                } else {
                    Toast.makeText(context, "Please Enter List Name", Toast.LENGTH_SHORT).show()
                }
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_close_editable_view).setOnClickListener {
                holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility = View.GONE
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_more).setOnClickListener {
                val popupMenu = PopupMenu(context, it)
                popupMenu.inflate(R.menu.menu_three_dots)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.ib_edit_list_name -> {
                            holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name).setText(model.title)
                            holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility = View.GONE
                            holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility = View.VISIBLE
                            true
                        }
                        R.id.ib_delete_list -> {
                            alertDialogForDeleteList(position, model.title)
                            true
                        }
                        R.id.ib_remove_all_card ->{
                            if (context is ListOfTasksActivity) {
                                context.deleteAllCardsInTask()
                            }
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
            }


            holder.itemView.findViewById<ImageButton>(R.id.ib_done_edit_list_name2).setOnClickListener {
                val listName = holder.itemView.
                findViewById<EditText>(R.id.et_edit_task_list_name).text.toString()

                if (listName.isNotEmpty()) {
                    if (context is ListOfTasksActivity) {
                        context.updateListOfTasks(position, listName, model)
                    }
                } else {
                    Toast.makeText(context, "Please Enter List Name", Toast.LENGTH_SHORT).show()
                }
            }

            holder.itemView.findViewById<TextView>(R.id.tv_add_card).setOnClickListener {

                holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.VISIBLE

                holder.itemView.findViewById<ImageButton>(R.id.ib_close_card_name).setOnClickListener {
                    holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility = View.VISIBLE
                    holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.GONE
                }

                holder.itemView.findViewById<ImageButton>(R.id.ib_done_card_name).setOnClickListener {

                    val cardName = holder.itemView.findViewById<EditText>(R.id.et_card_name).text.toString()

                    if (cardName.isNotEmpty()) {
                        if (context is ListOfTasksActivity) {
                            // Dodaj karticu u popis zadataka
                            context.addCardToTaskList(position, cardName)

                            // Dohvati trenutnog korisnika
                            val firebaseClass = FirebaseClass()
                            val currentUserId = firebaseClass.getCurrentUserId()

                            // Dohvati podatke o korisniku
                            firebaseClass.mFireStore.collection(Constants.USERS)
                                .document(currentUserId)
                                .get()
                                .addOnSuccessListener { documentSnapshot ->
                                    val userModel = documentSnapshot.toObject(UserModel::class.java)
                                    userModel?.let {
                                        val updatedCreatedTasks = it.createdTasks + 1

                                        // Kreiraj mapu za ažuriranje podataka korisnika
                                        val userHashMap = hashMapOf<String, Any>(
                                            "createdTasks" to updatedCreatedTasks
                                        )

                                        // Ažuriraj podatke korisnika na Firebase
                                        firebaseClass.updateProfileDataOfUser(context, userHashMap)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error getting user document", e)
                                }
                        }
                    } else {
                        Toast.makeText(context, "Please Enter Card Detail", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            }

            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).layoutManager = LinearLayoutManager(context)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).setHasFixedSize(true)

            val adapter =
                CardListItemsAdapter(context, model.cards)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).adapter = adapter

            adapter.setOnClickListener(object :
                CardListItemsAdapter.OnClickListener {
                override fun onClick(cardPosition: Int) {
                    if (context is ListOfTasksActivity) {
                        context.cardDetails(position, cardPosition)
                    }
                }
            })


            val dividerItemDecoration =
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).addItemDecoration(dividerItemDecoration)


            val helper = ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

                override fun onMove(
                    recyclerView: RecyclerView,
                    dragged: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val draggedPosition = dragged.adapterPosition
                    val targetPosition = target.adapterPosition

                    if (mPositionDraggedFrom == -1) {
                        mPositionDraggedFrom = draggedPosition
                    }
                    mPositionDraggedTo = targetPosition

                    Collections.swap(list[position].cards, draggedPosition, targetPosition)


                    adapter.notifyItemMoved(draggedPosition, targetPosition)

                    return false
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {
                }

                override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                    super.clearView(recyclerView, viewHolder)

                    if (mPositionDraggedFrom != -1 && mPositionDraggedTo != -1 && mPositionDraggedFrom != mPositionDraggedTo) {

                        (context as ListOfTasksActivity).updateCardsInTaskList(
                            position,
                            list[position].cards
                        )
                    }

                    mPositionDraggedFrom = -1
                    mPositionDraggedTo = -1
                }
            })

            helper.attachToRecyclerView(holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list))
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }


    private fun Int.toDp(): Int =
        (this / Resources.getSystem().displayMetrics.density).toInt()


    private fun Int.toPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun alertDialogForDeleteList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title.")
        builder.setIcon(R.drawable.warning)
        builder.setPositiveButton("Yes") { dialogInterface, which ->
            dialogInterface.dismiss()

            if (context is ListOfTasksActivity) {
                context.deleteTaskList(position)
            }
        }

        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}