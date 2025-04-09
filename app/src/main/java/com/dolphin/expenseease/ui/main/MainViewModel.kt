package com.dolphin.expenseease.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.data.repo.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    val allExpenses = repository.getAllExpenses()

    fun addExpense(expense: Expense) = viewModelScope.launch {
        val data = withContext(Dispatchers.IO) {
            repository.insertExpense(expense)
        }
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch {
        val data = withContext(Dispatchers.IO) {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        val data = withContext(Dispatchers.IO) {
            repository.deleteExpense(expense)
        }
    }
}