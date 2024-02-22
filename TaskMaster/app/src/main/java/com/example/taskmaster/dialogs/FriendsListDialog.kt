package com.example.taskmaster.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.R
import com.example.taskmaster.adapters.FriendListItemsAdapter
import com.example.taskmaster.models.UserModel

abstract class FriendsListDialog(
    context: Context,
    private var list: ArrayList<UserModel>,
    private val title: String = ""
) : Dialog(context) {

    private var adapter: FriendListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        view.findViewById<TextView>(R.id.tvTitle).text = title

        if (list.size > 0) {

            view.findViewById<RecyclerView>(R.id.rvList).layoutManager = LinearLayoutManager(context)
            adapter = FriendListItemsAdapter(context, list)
            view.findViewById<RecyclerView>(R.id.rvList).adapter = adapter

            adapter!!.setOnClickListener(object :
                FriendListItemsAdapter.OnClickListener {
                override fun onClick(position: Int, user: UserModel, action:String) {
                    dismiss()
                    onItemSelected(user, action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user: UserModel, action:String)
}