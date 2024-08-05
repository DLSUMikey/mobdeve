package com.example.posystem2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AccountsAdapter(
    private val accounts: List<ProfileModel>,
    private val onAccountClick: (ProfileModel) -> Unit
) : RecyclerView.Adapter<AccountsAdapter.AccountViewHolder>() {

    class AccountViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailTextView: TextView = itemView.findViewById(R.id.accountEmail)
        val userTypeTextView: TextView = itemView.findViewById(R.id.accountUserType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.account_box, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accounts[position]
        holder.emailTextView.text = account.email
        holder.userTypeTextView.text = account.userType
        holder.itemView.setOnClickListener { onAccountClick(account) }
    }

    override fun getItemCount(): Int {
        return accounts.size
    }
}

