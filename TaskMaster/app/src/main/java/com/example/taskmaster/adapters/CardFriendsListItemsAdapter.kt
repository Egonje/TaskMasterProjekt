package com.example.taskmaster.adapters
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskmaster.R
import com.example.taskmaster.models.SelectedFriendsModel


open class CardFriendsListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<SelectedFriendsModel>,
    private val assignFriends: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card_selected_friend,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {

            if (position == list.size - 1 && assignFriends) {
                holder.itemView.findViewById<ImageView>(R.id.iv_add_friend).visibility = View.VISIBLE
                holder.itemView.findViewById<ImageView>(R.id.iv_selected_friend_image).visibility = View.GONE
            } else {
                holder.itemView.findViewById<ImageView>(R.id.iv_add_friend).visibility = View.GONE
                holder.itemView.findViewById<ImageView>(R.id.iv_selected_friend_image).visibility = View.VISIBLE

                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.user)
                    .into(holder.itemView.findViewById(R.id.iv_selected_friend_image))
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick()
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
        fun onClick()
    }


    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}