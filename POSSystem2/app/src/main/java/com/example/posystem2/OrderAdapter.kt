package com.example.posystem2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OrderAdapter(
    private val orders: List<OrderEntity>,
    private val itemsInOrders: List<Pair<OrderEntity, List<ItemEntity>>>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderIdTextView: TextView = itemView.findViewById(R.id.orderIdTextView)
        val orderDateTextView: TextView = itemView.findViewById(R.id.orderDateTextView)
        val totalAmountTextView: TextView = itemView.findViewById(R.id.totalAmountTextView)
        val itemsRecyclerView: RecyclerView = itemView.findViewById(R.id.itemsRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val items = itemsInOrders.find { it.first.orderId == order.orderId }?.second ?: emptyList()

        holder.orderIdTextView.text = "Order ID: ${order.orderId}"
        holder.orderDateTextView.text = "Date: ${order.orderDate}"
        holder.totalAmountTextView.text = "Total: $${order.totalAmount}"

        // Set up the RecyclerView for the list of items
        holder.itemsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.itemsRecyclerView.adapter = ItemAdapter(items)
    }

    override fun getItemCount(): Int {
        return orders.size
    }
}
