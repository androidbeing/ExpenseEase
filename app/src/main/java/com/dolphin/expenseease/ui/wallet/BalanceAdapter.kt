package com.dolphin.expenseease.ui.wallet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolphin.expenseease.data.db.wallet.MyWallet
import com.dolphin.expenseease.databinding.ItemWalletBinding
import com.dolphin.expenseease.listeners.WalletEditListener
import com.dolphin.expenseease.utils.Constants.LAST_SYNC_ON
import com.dolphin.expenseease.utils.CurrencyManager
import com.dolphin.expenseease.utils.ExtensiveFunctions.getRelativeTimeString
import com.dolphin.expenseease.utils.PreferenceHelper

class BalanceAdapter(
    private val context: Context,
    private val list: MutableList<MyWallet>,
    private val listener: WalletEditListener
) :
    RecyclerView.Adapter<BalanceAdapter.BalViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BalViewHolder {
        val binding: ItemWalletBinding =
            ItemWalletBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BalViewHolder(context, binding, listener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: BalViewHolder, position: Int) {
        holder.bindItems(list[position], position)
    }

    class BalViewHolder(private val context: Context, private val view: ItemWalletBinding,
                        private val listener: WalletEditListener) :
        RecyclerView.ViewHolder(view.root) {
        fun bindItems(wallet: MyWallet, position: Int) {
            val lastSyncTime = PreferenceHelper.getLong(LAST_SYNC_ON)
            if(wallet.createdAt > lastSyncTime) {
                view.imgEdit.visibility = View.VISIBLE
                view.imgDelete.visibility = View.VISIBLE
            } else {
                view.imgEdit.visibility = View.GONE
                view.imgDelete.visibility = View.GONE
            }

            val currencySymbol = CurrencyManager.getCurrencySymbol(context)
            view.textAmount.text = "$currencySymbol ${wallet.addedAmount}"
            view.textNotes.text = wallet.notes
            view.textCreatedAt.text = context.getRelativeTimeString(wallet.updatedAt)
            view.imgEdit.setOnClickListener { listener.onWalletEdit(wallet, position) }
            view.imgDelete.setOnClickListener { listener.onWalletRemove(wallet, position) }
        }
    }
}