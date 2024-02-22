package com.example.taskmaster.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskmaster.R
import com.example.taskmaster.models.UserModel
import com.example.taskmaster.utils.Constants

open class FriendListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<UserModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_friend,
                parent,
                false
            )
        )
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.user)
                .into(holder.itemView.findViewById(R.id.iv_friend_image))

            holder.itemView.findViewById<TextView>(R.id.tv_friend_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.tv_friend_email).text = model.email

            if (model.selected) {
              holder.itemView.findViewById<ImageView>(R.id.iv_selected_friend).visibility = View.VISIBLE
            } else {
                holder.itemView.findViewById<ImageView>(R.id.iv_selected_friend).visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if(onClickListener != null){
                    if (model.selected){
                        onClickListener!!.onClick(position, model, Constants.UN_SELECT)
                    } else {
                        onClickListener!!.onClick(position, model, Constants.SELECT)
                    }
                    }
            }
        }
    }
    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    interface OnClickListener {
        fun onClick(position: Int, user: UserModel, action: String)
    }
}