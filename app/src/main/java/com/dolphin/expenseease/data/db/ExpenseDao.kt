package com.dolphin.expenseease.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expense")
    fun getAll(): LiveData<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(expense: Expense)

    @Delete
    fun delete(expense: Expense)
}