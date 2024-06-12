package com.dolphin.expenseease.data.db.reminder

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders")
    fun getAll(): LiveData<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(reminder: Reminder)

    @Update
    fun update(reminder: Reminder)

    @Delete
    fun delete(reminder: Reminder)
}