package com.dolphin.expenseease.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.databinding.ItemExpenseBinding
import com.dolphin.expenseease.listeners.ExpenseEditListener
import com.dolphin.expenseease.utils.CurrencyManager
import com.dolphin.expenseease.utils.ExtensiveFunctions.getRelativeTimeString

class ExpenseAdapter(private val context: Context, private val list: MutableList<Expense>, private val listener: ExpenseEditListener) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpenseViewHolder {
        val binding: ItemExpenseBinding =
            ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(context, listener, binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bindItems(list[position], position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ExpenseViewHolder(private val context: Context, private val listener: ExpenseEditListener, private val view: ItemExpenseBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bindItems(expense: Expense, position: Int) {
            val currencySymbol = CurrencyManager.getCurrencySymbol(context)
            view.textAmount.text = "$currencySymbol ${expense.amount}"
            view.txtExpense.text = expense.notes
            view.textType.text = expense.type
            view.textDateTime.text = context.getRelativeTimeString(expense.updatedAt)
            view.txtSpentOn.text = context.getString(R.string.spent_on, expense.date)
            view.imgEdit.setOnClickListener { listener.onExpenseEdit(expense, position) }
            view.imgDelete.setOnClickListener { listener.onExpenseRemove(expense, position) }
        }
    }
}