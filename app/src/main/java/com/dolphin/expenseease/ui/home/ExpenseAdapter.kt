package com.dolphin.expenseease.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.databinding.ItemExpenseBinding

class ExpenseAdapter(private val context: Context, private val list: MutableList<Expense>) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExpenseAdapter.ExpenseViewHolder {
        val binding: ItemExpenseBinding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(context, binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bindItems(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ExpenseViewHolder(private val context: Context, private val view: ItemExpenseBinding) : RecyclerView.ViewHolder(view.root) {
        fun bindItems(expense: Expense) {
            view.txtExpense.text = expense.notes
            view.textType.text = expense.type
            view.textDateTime.text = "${expense.createdAt}"
            view.textAmount.text = "INR ${expense.amount}"
        }
    }
}