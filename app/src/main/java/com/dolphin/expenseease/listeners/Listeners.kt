package com.dolphin.expenseease.listeners

import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.data.db.reminder.Reminder
import com.dolphin.expenseease.data.db.wallet.MyWallet

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

interface WalletEditListener {
    fun onWalletEdit(wallet: MyWallet, index: Int)
    fun onWalletRemove(wallet: MyWallet, index: Int)
}

interface AddBudgetListener {
    fun onBudgetAdd(budgetType: String, allocatedAmount: Double, monthYear: String)
}

interface BudgetEditListener {
    fun onBudgetEdit(budget: Budget, index: Int)
    fun onBudgetRemove(budget: Budget, index: Int)
}

interface AddReminderListener {
    fun onReminderAdd(notes: String, monthYear: String)
}

interface ReminderEditListener {
    fun onReminderEdit(reminder: Reminder, index: Int)
    fun onReminderRemove(reminder: Reminder, index: Int)
}

interface MonthListener {
    fun onMonthSelected(monthYear: String)
}

interface OnClickAlertListener {
    fun onAcknowledge(isOkay: Boolean)
}