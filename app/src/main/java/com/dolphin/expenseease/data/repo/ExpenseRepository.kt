package com.dolphin.expenseease.data.repo

import androidx.lifecycle.LiveData
import com.dolphin.expenseease.data.db.Expense
import com.dolphin.expenseease.data.db.ExpenseDao
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    fun getAllExpenses(): LiveData<List<Expense>> = expenseDao.getAll()

    suspend fun insertExpense(expense: Expense) = expenseDao.insert(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)
}