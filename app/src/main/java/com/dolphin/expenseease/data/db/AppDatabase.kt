package com.dolphin.expenseease.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.data.db.budget.BudgetDao
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.data.db.expense.ExpenseDao
import com.dolphin.expenseease.data.db.reminder.Reminder
import com.dolphin.expenseease.data.db.reminder.ReminderDao
import com.dolphin.expenseease.data.db.sheet.MySheet
import com.dolphin.expenseease.data.db.sheet.MySheetDao
import com.dolphin.expenseease.data.db.wallet.MyWallet
import com.dolphin.expenseease.data.db.wallet.MyWalletDao


@Database(entities = [Expense::class, Budget::class, MyWallet::class, Reminder::class, MySheet::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun myWalletDao(): MyWalletDao
    abstract fun reminderDao(): ReminderDao
    abstract fun sheetDao(): MySheetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}