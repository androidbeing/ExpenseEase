package com.dolphin.expenseease.ui.reminders

import androidx.lifecycle.ViewModel
import com.dolphin.expenseease.data.repo.ExpenseRepository
import javax.inject.Inject

class ReminderViewModel@Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel()  {
}