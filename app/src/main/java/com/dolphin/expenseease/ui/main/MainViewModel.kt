package com.dolphin.expenseease.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.data.db.wallet.MyWallet
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

    private val _totalAmountSpent = MutableLiveData<Double>()
    val totalAmountSpent: LiveData<Double> get() = _totalAmountSpent

    private val _totalAmountThisMonth = MutableLiveData<Double>()
    val totalAmountThisMonth: LiveData<Double> get() = _totalAmountThisMonth

    fun fetchTotalAmountSpent(startDate: String) = viewModelScope.launch {
        val totalAmount = withContext(Dispatchers.IO) {
            repository.getTotalAmountSpentToday(startDate)
        }
        _totalAmountSpent.postValue(totalAmount?.toDouble() ?: 0.0) // Default to 0.0 if null
    }

    fun fetchTotalAmountThisMonth(startDate: Long, endDate: Long) = viewModelScope.launch {
        val totalAmount = withContext(Dispatchers.IO) {
            repository.getTotalAmountSpent(startDate, endDate)
        }
        _totalAmountThisMonth.postValue(totalAmount?.toDouble() ?: 0.0) // Default to 0.0 if null
    }

    fun addExpense(expense: Expense) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            // Insert the expense
            repository.insertExpense(expense)

            // Fetch the latest wallet balance
            val latestWallet = repository.getLatestWallet()
            val walletValue = latestWallet?.value
            if (walletValue != null) {
                // Reduce the balance by the expense amount
                val updatedBalance = walletValue.balance?.minus(expense.amount)
                if (updatedBalance != null) {
                    repository.updateWalletBalance(walletValue.id, updatedBalance)
                }
            }
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

    fun getLatestBalance(): LiveData<MyWallet> {
        return repository.getLatestWallet()
    }

}