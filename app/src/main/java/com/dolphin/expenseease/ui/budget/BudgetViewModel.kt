package com.dolphin.expenseease.ui.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.data.repo.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    val allBudgets: LiveData<List<Budget>> = repository.getAllBudgets()

    fun insertBudget(budget: Budget) = viewModelScope.launch {
        val data = withContext(Dispatchers.IO) {
            repository.insertBudget(budget)
        }
    }

    fun addBudget(budget: Budget) = viewModelScope.launch {
        val data = withContext(Dispatchers.IO) {
            repository.insertBudget(budget)
        }
    }
}