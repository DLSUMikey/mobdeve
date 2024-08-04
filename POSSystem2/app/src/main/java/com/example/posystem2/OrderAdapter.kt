package com.example.posystem2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderAdapter(
    private val orders: List<OrderModel>,
    private val itemClickListener: (OrderModel) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderId: TextView = view.findViewById(R.id.orderIdtv)
        val orderDate: TextView = view.findViewById(R.id.orderDatetv)
        val totalAmount: TextView = view.findViewById(R.id.orderTotaltv)
        val orderQty: TextView = view.findViewById(R.id.orderQtytv) // New TextView for total quantity
        val orderStatus: TextView = view.findViewById(R.id.orderStatustv) // New TextView for order status
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_box, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.orderId.text = "Order ID: ${order.orderId}"
        holder.orderDate.text = "Date: ${order.orderDate}"
        holder.totalAmount.text = "Total: ₱${String.format("%.2f", order.totalAmount)}" // Format total amount to 2 decimal points
        holder.orderQty.text = "Qty: ${order.totalQuantity}" // Display total quantity
        holder.orderStatus.text = "Status: ${order.status}" // Display order status

        holder.itemView.setOnClickListener { itemClickListener(order) }
    }

    override fun getItemCount() = orders.size
}
