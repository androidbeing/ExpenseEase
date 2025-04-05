package com.dolphin.expenseease.listeners

import com.dolphin.expenseease.data.db.expense.Expense

interface AddExpenseListener {
    fun onExpenseAdd(expense: Expense)
}


interface AddBalanceListener {
    fun onBalanceAdd(addedAmount: Double, notes: String)
}