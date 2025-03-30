package com.dolphin.expenseease.data.di

import android.app.Application
import dagger.Module
import androidx.room.Room
import com.dolphin.expenseease.data.db.AppDatabase
import com.dolphin.expenseease.data.db.budget.BudgetDao
import com.dolphin.expenseease.data.db.expense.ExpenseDao
import com.dolphin.expenseease.data.db.reminder.ReminderDao
import com.dolphin.expenseease.data.db.wallet.MyWalletDao
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "expense_database"
        ).build()
    }

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideBudgetDao(database: AppDatabase): BudgetDao {
        return database.budgetDao()
    }

    @Provides
    fun provideReminderDao(database: AppDatabase): ReminderDao {
        return database.reminderDao()
    }

    @Provides
    fun provideWalletDao(database: AppDatabase): MyWalletDao {
        return database.walletDao()
    }
}
