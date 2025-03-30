package com.dolphin.expenseease.data.repo

import androidx.lifecycle.LiveData
import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.data.db.budget.BudgetDao
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.data.db.expense.ExpenseDao
import com.dolphin.expenseease.data.db.reminder.Reminder
import com.dolphin.expenseease.data.db.reminder.ReminderDao
import com.dolphin.expenseease.data.db.wallet.MyWallet
import com.dolphin.expenseease.data.db.wallet.MyWalletDao
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val reminderDao: ReminderDao,
    private val walletDao: MyWalletDao,
) {
    fun getAllExpenses(): LiveData<List<Expense>> = expenseDao.getAll()

    suspend fun insertExpense(expense: Expense) = expenseDao.insert(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)


    fun getAllBudgets(): LiveData<List<Budget>> = budgetDao.getAll()

    suspend fun insertBudget(budget: Budget) = budgetDao.insert(budget)

    suspend fun deleteBudget(budget: Budget) = budgetDao.delete(budget)


    fun getAllReminders(): LiveData<List<Reminder>> = reminderDao.getAll()

    suspend fun insertReminder(reminder: Reminder) = reminderDao.insert(reminder)

    suspend fun deleteReminder(reminder: Reminder) = reminderDao.delete(reminder)


    fun getAllWallets(): LiveData<List<MyWallet>> = walletDao.getAll()

    suspend fun insertWallet(wallet: MyWallet) = walletDao.insert(wallet)

    suspend fun deleteWallet(wallet: MyWallet) = walletDao.delete(wallet)
}