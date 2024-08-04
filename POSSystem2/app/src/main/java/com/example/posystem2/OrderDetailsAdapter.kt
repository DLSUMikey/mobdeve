package com.example.posystem2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderDetailsAdapter(
    private val items: List<ItemModel>
) : RecyclerView.Adapter<OrderDetailsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.orderItemName)
        val itemQuantity: TextView = itemView.findViewById(R.id.orderItemQuantity)
        val itemPrice: TextView = itemView.findViewById(R.id.orderItemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_details_box, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.itemName
        holder.itemQuantity.text = "x${item.quantity}"
        holder.itemPrice.text = "₱${String.format("%.2f", item.itemPrice * item.quantity)}"
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
