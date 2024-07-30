package com.example.posystem2

import android.net.Uri
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

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
        val imageUri = Uri.parse(item.imageUri)
        Picasso.get().load(imageUri).into(holder.imageView)
        holder.textViewName.text = item.itemName
        holder.textViewPrice.text = item.itemPrice.toString()

        holder.itemView.setOnLongClickListener {
            showPopupMenu(it, item)
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

    private fun showPopupMenu(view: View, item: ItemModel) {
        val popup = PopupMenu(view.context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.item_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    itemMenuClickListener(MenuAction.EDIT, item)
                    true
                }
                R.id.action_add -> {
                    itemMenuClickListener(MenuAction.ADD, item)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
