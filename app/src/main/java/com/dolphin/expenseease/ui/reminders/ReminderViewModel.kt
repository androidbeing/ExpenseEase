package com.dolphin.expenseease.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.expenseease.data.db.reminder.Reminder
import com.dolphin.expenseease.data.repo.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel@Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel()  {

    val allReminders = repository.getAllReminders()

    fun addReminder(reminder: Reminder) = viewModelScope.launch {
        val data = withContext(Dispatchers.IO) {
            repository.insertReminder(reminder)
        }
    }
}