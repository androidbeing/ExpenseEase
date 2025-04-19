package com.dolphin.expenseease.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.expenseease.data.db.sheet.MySheet
import com.dolphin.expenseease.data.repo.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    val allExpenses = repository.getAllExpenses()
    val allBudgets = repository.getAllBudgets()
    val allWallets = repository.getAllWallets()

    fun addSheet(sheet: MySheet) = viewModelScope.launch {
        val data = withContext(Dispatchers.IO) {
            repository.insertSheet(sheet)
        }
    }
}