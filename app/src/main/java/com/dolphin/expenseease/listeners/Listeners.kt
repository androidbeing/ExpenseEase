package com.dolphin.expenseease.listeners

import com.dolphin.expenseease.data.db.expense.Expense

interface AddExpenseListener {
    fun onExpenseAdd(expense: Expense)
}

interface ExpenseEditListener {
    fun onExpenseEdit(expense: Expense, index: Int)
    fun onExpenseRemove(expense: Expense, index: Int)
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

interface OnClickAlertListener {
    fun onAcknowledge(isOkay: Boolean)
}