package com.dolphin.expenseease.data.db.sheet

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface MySheetDao {
    @Query("SELECT * FROM my_sheet")
    fun getAll(): LiveData<List<MySheet>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(mySheet: MySheet)

    @Update
    fun update(mySheet: MySheet)

    @Delete
    fun delete(mySheet: MySheet)
}