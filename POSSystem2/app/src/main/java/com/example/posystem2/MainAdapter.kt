package com.example.posystem2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainAdapter(
    private var mList: MutableList<ItemModel>,
    private val itemMenuClickListener: (MenuAction, ItemModel) -> Unit
) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    enum class MenuAction {
        EDIT, ADD
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.itemImageiv)
        val textViewName: TextView = itemView.findViewById(R.id.itemNametv)
        val textViewPrice: TextView = itemView.findViewById(R.id.priceTexttv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_box, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.imageView.setImageResource(item.imageId)
        holder.textViewName.text = item.itemName
        holder.textViewPrice.text = item.itemPrice.toString()
        holder.itemView.setOnClickListener {
            itemMenuClickListener(MenuAction.EDIT, item)
        }
        holder.itemView.setOnLongClickListener {
            itemMenuClickListener(MenuAction.ADD, item)
            true
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun updateItems(newItems: List<ItemModel>) {
        mList.clear()
        mList.addAll(newItems)
        notifyDataSetChanged()
    }
}
