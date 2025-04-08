package com.dolphin.expenseease.listeners

import com.dolphin.expenseease.data.db.expense.Expense

interface AddExpenseListener {
    fun onExpenseAdd(expense: Expense)
}


interface AddBalanceListener {
    fun onBalanceAdd(addedAmount: Double, notes: String)
}

interface AddBudgetListener {
    fun onBudgetAdd(budgetType: String, allocatedAmount: Double, monthYear: String)
}

interface AddReminderListener {
    fun onReminderAdd(notes: String, monthYear: String)
}

interface MonthListener {
    fun onMonthSelected(monthYear: String)
}