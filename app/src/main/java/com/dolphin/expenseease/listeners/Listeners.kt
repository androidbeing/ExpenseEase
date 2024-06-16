package com.dolphin.expenseease.listeners

import com.dolphin.expenseease.data.db.expense.Expense

interface AddExpenseListener {
    fun onExpenseAdd(expense: Expense)
}