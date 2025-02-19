package com.example.posystem2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CurrentOrderAdapter(
    private val currentOrderItems: List<ItemModel>,
    private val onItemRemoved: (ItemModel) -> Unit
) : RecyclerView.Adapter<CurrentOrderAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.orderItemName)
        val itemQuantity: TextView = itemView.findViewById(R.id.orderItemQuantity)
        val itemPrice: TextView = itemView.findViewById(R.id.orderItemPrice)
        val removeButton: Button = itemView.findViewById(R.id.removeItemButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.current_item_box, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = currentOrderItems[position]
        holder.itemName.text = item.itemName
        holder.itemQuantity.text = "x${item.quantity}"
        holder.itemPrice.text = "₱${String.format("%.2f", item.itemPrice * item.quantity)}"
        holder.removeButton.setOnClickListener {
            onItemRemoved(item)
        }
    }

    override fun getItemCount(): Int {
        return currentOrderItems.size
    }
}
