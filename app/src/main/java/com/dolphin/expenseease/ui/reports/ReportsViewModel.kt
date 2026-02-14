package com.dolphin.expenseease.ui.reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.expenseease.data.db.expense.DailyExpense
import com.dolphin.expenseease.data.repo.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Reports Fragment"
    }
    val text: LiveData<String> = _text

    private val _currentBalance = MutableLiveData<Double>()
    val currentBalance: LiveData<Double> = _currentBalance

    private val _monthlyExpenses = MutableLiveData<Double>()
    val monthlyExpenses: LiveData<Double> = _monthlyExpenses

    // Get latest wallet balance
    fun getLatestWallet() = repository.getLatestWallet()

    // Get daily expenses for the current month
    fun getDailyExpensesForCurrentMonth(): LiveData<List<DailyExpense>> {
        val monthYear = getCurrentMonthYear()
        return repository.getDailyExpensesForMonth(monthYear)
    }

    // Fetch balance and monthly expenses
    fun fetchMonthlyData() {
        viewModelScope.launch {
            // Get current month/year
            val monthYear = getCurrentMonthYear()

            // Get total expenses for current month
            val expenses = repository.getTotalExpensesForMonth(monthYear) ?: 0.0
            _monthlyExpenses.value = expenses
        }
    }

    fun setCurrentBalance(balance: Double) {
        _currentBalance.value = balance
    }

    private fun getCurrentMonthYear(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}