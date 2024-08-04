package com.example.posystem2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StatisticsAdapter(
    private val statisticsList: List<StatisticsModel>
) : RecyclerView.Adapter<StatisticsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.statisticsItemName)
        val itemCountTextView: TextView = itemView.findViewById(R.id.statisticsItemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.statistics_box, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val statistics = statisticsList[position]
        holder.itemNameTextView.text = statistics.itemName
        holder.itemCountTextView.text = "Count: ${statistics.itemCount}"
    }

    override fun getItemCount(): Int {
        return statisticsList.size
    }
}


