package com.dolphin.expenseease.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.expenseease.data.db.Expense
import com.dolphin.expenseease.data.repo.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    val allExpenses = repository.getAllExpenses()

    fun insertExpense(expense: Expense) = viewModelScope.launch {
        val data = withContext(Dispatchers.IO) {
            repository.insertExpense(expense)
        }
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text
}