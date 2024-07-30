package com.example.posystem2

import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainAdapter(
    private val mList: List<ItemModel>,
    private val itemMenuListener: (MenuAction, ItemModel) -> Unit
) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    enum class MenuAction {
        EDIT, ADD
    }

    // ViewHolder class to hold the views for each item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.itemImageiv)
        val textViewName: TextView = itemView.findViewById(R.id.itemNametv)
        val textViewPrice: TextView = itemView.findViewById(R.id.priceTexttv)
        val menuBtn: ImageView = itemView.findViewById(R.id.menuBtn)
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

        // Handle menu icon click
        holder.menuBtn.setOnClickListener {
            showPopupMenu(it, item)
        }
    }

    // Show the popup menu
    private fun showPopupMenu(view: View, item: ItemModel) {
        val popup = PopupMenu(view.context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.item_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            handleMenuItemClick(menuItem, item)
            true
        }
        popup.show()
    }

    // Handle menu item clicks
    private fun handleMenuItemClick(menuItem: MenuItem, item: ItemModel) {
        when (menuItem.itemId) {
            R.id.action_edit -> itemMenuListener(MenuAction.EDIT, item)
            R.id.action_add -> itemMenuListener(MenuAction.ADD, item)
        }
    }

    // Return the size of the list (invoked by the layout manager)
    override fun getItemCount(): Int {
        return mList.size
    }

}
