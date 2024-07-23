package com.example.possystem2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.posystem2.ItemModel

class MainAdapter(private val mList: List<ItemModel>) :
    RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    // ViewHolder class to hold the views for each item
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = ItemView.findViewById(R.id.itemImageiv)
        val textViewName: TextView = ItemView.findViewById(R.id.itemNametv)
        val textViewPrice: TextView = ItemView.findViewById(R.id.priceTexttv)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the item layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_box, parent, false)
        return ViewHolder(view)
    }

    // Bind the data to the views (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Get the item from the list
        val item = mList[position]

        // Set the image, name, and price of the item to the views
        holder.imageView.setImageResource(item.imageId)
        holder.textViewName.text = item.itemName
        holder.textViewPrice.text = item.itemPrice.toString()
    }

    // Return the size of the list (invoked by the layout manager)
    override fun getItemCount(): Int {
        return mList.size
    }
}
