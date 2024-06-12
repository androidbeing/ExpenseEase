package com.dolphin.expenseease.ui.budget

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.databinding.ItemBudgetBinding

class BudgetAdapter(private val context: Context, private val list: MutableList<Budget>) :
    RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BudgetViewHolder {
        val binding: ItemBudgetBinding =
            ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BudgetViewHolder(context, binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bindItems(list[position])
    }

    class BudgetViewHolder(private val context: Context, private val view: ItemBudgetBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bindItems(budget: Budget) {
            view.textType.text = budget.type
            view.textDateTime.text = "${budget.createdAt}"
            view.textAmount.text = "INR ${budget.amount}"
        }
    }
}