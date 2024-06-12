package com.dolphin.expenseease.data.db.budget

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget")
    fun getAll(): LiveData<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(budget: Budget)

    @Update
    fun update(budget: Budget)

    @Query("UPDATE budget SET amount = :amount WHERE id = :id")
    fun updateAmountById(id: Int, amount: Double)

    @Delete
    fun delete(budget: Budget)
}