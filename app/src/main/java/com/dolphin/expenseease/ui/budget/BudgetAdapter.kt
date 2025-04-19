package com.dolphin.expenseease.ui.budget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.databinding.ItemBudgetBinding
import com.dolphin.expenseease.listeners.BudgetEditListener
import com.dolphin.expenseease.utils.Constants.LAST_SYNC_ON
import com.dolphin.expenseease.utils.CurrencyManager
import com.dolphin.expenseease.utils.ExtensiveFunctions.getRelativeTimeString
import com.dolphin.expenseease.utils.PreferenceHelper

class BudgetAdapter(private val context: Context, private val list: MutableList<Budget>,
    private val listener: BudgetEditListener) :
    RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BudgetViewHolder {
        val binding: ItemBudgetBinding =
            ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetViewHolder(context, binding, listener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bindItems(list[position], position)
    }

    class BudgetViewHolder(private val context: Context, private val view: ItemBudgetBinding, private val listener: BudgetEditListener) :
        RecyclerView.ViewHolder(view.root) {
        fun bindItems(budget: Budget, position: Int) {
            val lastSyncTime = PreferenceHelper.getLong(LAST_SYNC_ON)
            if(budget.createdAt > lastSyncTime) {
                view.imgEdit.visibility = View.VISIBLE
                view.imgDelete.visibility = View.VISIBLE
            } else {
                view.imgEdit.visibility = View.GONE
                view.imgDelete.visibility = View.GONE
            }
            val currencySymbol = CurrencyManager.getCurrencySymbol(context)
            view.textAmount.text = "$currencySymbol ${budget.amount}"
            view.textType.text = budget.type
            view.textDateTime.text = context.getRelativeTimeString(budget.updatedAt)
            view.txtDate.text = "${budget.monthYear}"
            view.imgEdit.setOnClickListener { listener.onBudgetEdit(budget, position) }
            view.imgDelete.setOnClickListener { listener.onBudgetRemove(budget, position) }
        }
    }
}