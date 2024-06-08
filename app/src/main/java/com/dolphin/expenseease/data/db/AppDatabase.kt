package com.dolphin.expenseease.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Expense::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}