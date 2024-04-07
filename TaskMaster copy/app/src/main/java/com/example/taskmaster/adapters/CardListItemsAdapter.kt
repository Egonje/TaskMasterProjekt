package com.example.taskmaster.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.R
import com.example.taskmaster.activities.ListOfTasksActivity
import com.example.taskmaster.models.CardModel
import com.example.taskmaster.models.SelectedFriendsModel
import com.google.android.material.card.MaterialCardView

open class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<CardModel>,

    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            if(model.labelColor.isNotEmpty()){
                holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.GONE
                holder.itemView.findViewById<View>(R.id.view_label_color).setBackgroundColor(Color.parseColor(model.labelColor))
                (holder.itemView as? MaterialCardView)?.strokeColor = Color.parseColor(model.labelColor)
                val strokeWidth = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.stroke_width)
                (holder.itemView as? MaterialCardView)?.strokeWidth = strokeWidth
            } else {
                holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.GONE
                (holder.itemView as? MaterialCardView)?.strokeWidth = 0
            }
            holder.itemView.findViewById<TextView>(R.id.tv_card_name).text = model.name

            if ((context as ListOfTasksActivity)
                    .mAssingendFriendDetails.size > 0){
                val selectedFriendsList: ArrayList<SelectedFriendsModel> = ArrayList()
                for (i in context.mAssingendFriendDetails.indices){
                    for (j in model.assignedTo){
                        if (context.mAssingendFriendDetails[i].id == j){
                            val selectedFriends = SelectedFriendsModel(
                                context.mAssingendFriendDetails[i].id,
                                context.mAssingendFriendDetails[i].image,
                            )
                            selectedFriendsList.add(selectedFriends)
                        }
                    }
                }
                if (selectedFriendsList.size > 0){
                    if (selectedFriendsList.size == 1 && selectedFriendsList[0].id == model.createdBy) {
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_friends_list).visibility = View.GONE
                    } else {
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_friends_list).visibility = View.VISIBLE

                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_friends_list).layoutManager =
                            GridLayoutManager(context, 4)

                        val adapter = CardFriendsListItemsAdapter(
                            context, selectedFriendsList, false)

                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_friends_list).adapter = adapter

                        adapter.setOnClickListener(
                            object: CardFriendsListItemsAdapter.OnClickListener{
                                override fun onClick() {
                                    if(onClickListener != null){
                                        onClickListener!!.onClick(position)
                                    }
                                }
                        })
                    }
                }else {
                    holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_friends_list).visibility =
                        View.GONE
                }
            }
            holder.itemView.setOnClickListener {
                if (onClickListener != null){
                    onClickListener!!.onClick(position)
                }
            }
        }
    }
    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}