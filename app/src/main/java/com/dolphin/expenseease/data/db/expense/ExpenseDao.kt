package com.dolphin.expenseease.data.db.expense

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expense")
    fun getAll(): LiveData<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(expense: Expense)

    @Update
    fun update(expense: Expense)

    @Query("UPDATE expense SET amount = :amount WHERE id = :id")
    fun updateAmountById(id: Int, amount: Double)

    @Query("SELECT SUM(amount) FROM expense WHERE created_at BETWEEN :startTime AND :endTime")
    fun getExpenseAmountAddedBetween(startTime: Long, endTime: Long): Double

    @Delete
    fun delete(expense: Expense)
}