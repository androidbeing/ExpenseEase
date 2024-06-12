package com.dolphin.expenseease.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.data.db.budget.BudgetDao
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.data.db.expense.ExpenseDao
import com.dolphin.expenseease.data.db.reminder.Reminder
import com.dolphin.expenseease.data.db.reminder.ReminderDao

@Database(entities = [Expense::class, Budget::class, Reminder::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun reminderDao(): ReminderDao
}