package com.dolphin.expenseease.data.repo

import androidx.lifecycle.LiveData
import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.data.db.budget.BudgetDao
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.data.db.expense.ExpenseDao
import com.dolphin.expenseease.data.db.reminder.Reminder
import com.dolphin.expenseease.data.db.reminder.ReminderDao
import com.dolphin.expenseease.data.db.sheet.MySheet
import com.dolphin.expenseease.data.db.sheet.MySheetDao
import com.dolphin.expenseease.data.db.wallet.MyWallet
import com.dolphin.expenseease.data.db.wallet.MyWalletDao
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val reminderDao: ReminderDao,
    private val walletDao: MyWalletDao,
    private val sheetDao: MySheetDao
) {
    fun getAllExpenses(): LiveData<List<Expense>> = expenseDao.getAll()

    suspend fun insertExpense(expense: Expense) = expenseDao.insert(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)
    fun getExpenseAmountAddedBetween(startTime: Long, endTime: Long): Double {
        return expenseDao.getExpenseAmountAddedBetween(startTime, endTime)
    }

    fun getAllBudgets(): LiveData<List<Budget>> = budgetDao.getAll()

    suspend fun insertBudget(budget: Budget) = budgetDao.insert(budget)
    suspend fun updateBudget(budget: Budget) = budgetDao.update(budget)
    suspend fun deleteBudget(budget: Budget) = budgetDao.delete(budget)


    fun getAllReminders(): LiveData<List<Reminder>> = reminderDao.getAll()

    suspend fun insertReminder(reminder: Reminder) = reminderDao.insert(reminder)
    suspend fun updateReminder(reminder: Reminder) = reminderDao.update(reminder)
    suspend fun deleteReminder(reminder: Reminder) = reminderDao.delete(reminder)

    suspend fun insertSheet(sheet: MySheet) = sheetDao.insert(sheet)


    fun getAllWallets(): LiveData<List<MyWallet>> = walletDao.getAll()

    fun getLatestWallet(): LiveData<MyWallet> = walletDao.getLatestBalance()

    fun getWalletAmountAddedBetween(startTime: Long, endTime: Long): Double {
        return walletDao.getWalletAmountAddedBetween(startTime, endTime)
    }

    suspend fun insertWallet(wallet: MyWallet) = walletDao.insert(wallet)

    suspend fun updateWallet(wallet: MyWallet) = walletDao.update(wallet)

    suspend fun deleteWallet(wallet: MyWallet) = walletDao.delete(wallet)
}