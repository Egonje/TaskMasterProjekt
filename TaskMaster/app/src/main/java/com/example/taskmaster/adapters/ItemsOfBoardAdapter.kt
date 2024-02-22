package com.example.taskmaster.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.taskmaster.R
import com.example.taskmaster.models.TaskBoardModel

open class ItemsOfBoardAdapter(private val context: Context,
                               private var list: ArrayList<TaskBoardModel>):
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_board,
                    parent,
                    false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder){
            val radius = 30
            val requestOptions = RequestOptions().transform(RoundedCorners(radius))
            Glide
                .with(context)
                .load(model.image)
                .apply(requestOptions)
                .centerCrop()
                .placeholder(R.drawable.picture)
                .into(holder.itemView.findViewById(R.id.iv_board_image))
            holder.itemView.findViewById<TextView>(R.id.tv_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.tv_created_by).text = "Created: ${model.whoCreated}"

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position,model)
                }
            }
        }
    }

    interface OnClickListener{
        fun onClick(position: Int, model: TaskBoardModel)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    private class MyViewHolder(view: View): RecyclerView.ViewHolder(view)
}