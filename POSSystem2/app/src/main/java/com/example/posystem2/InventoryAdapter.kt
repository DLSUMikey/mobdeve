package com.example.posystem2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class InventoryAdapter(
    private val inventoryList: List<ItemModel>,
    private val updateStockListener: (ItemModel, Int) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.inventoryItemName)
        val initialStockTextView: TextView = itemView.findViewById(R.id.inventoryInitialStock)
        val amountSoldTextView: TextView = itemView.findViewById(R.id.inventoryAmountSold)
        val currentStockTextView: TextView = itemView.findViewById(R.id.inventoryCurrentStock)
        val editInitialStock: EditText = itemView.findViewById(R.id.editInitialStock)
        val btnUpdateStock: Button = itemView.findViewById(R.id.btnUpdateStock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_box, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = inventoryList[position]
        holder.itemNameTextView.text = item.itemName
        holder.initialStockTextView.text = "Initial Stock: ${item.initialStock}"
        holder.amountSoldTextView.text = "Amount Sold: ${item.amountSold}"
        holder.currentStockTextView.text = "Current Stock: ${item.initialStock - item.amountSold}"

        holder.btnUpdateStock.setOnClickListener {
            val newStock = holder.editInitialStock.text.toString().toIntOrNull()
            if (newStock != null) {
                updateStockListener(item, newStock)
            } else {
                Toast.makeText(holder.itemView.context, "Enter a valid stock number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return inventoryList.size
    }
}

