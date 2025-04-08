package com.dolphin.expenseease.ui.wallet

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolphin.expenseease.data.db.wallet.MyWallet
import com.dolphin.expenseease.databinding.ItemWalletBinding

class BalanceAdapter(private val context: Context, private val list: MutableList<MyWallet>) :
    RecyclerView.Adapter<BalanceAdapter.BalViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BalViewHolder {
        val binding: ItemWalletBinding =
            ItemWalletBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BalViewHolder(context, binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: BalViewHolder, position: Int) {
        holder.bindItems(list[position])
    }

    class BalViewHolder(private val context: Context, private val view: ItemWalletBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bindItems(wallet: MyWallet) {
            view.textNotes.text = wallet.notes
            view.textAmount.text = "${wallet.addedAmount}"
            view.textCreatedAt.text = "INR ${wallet.createdAt}"
        }
    }
}