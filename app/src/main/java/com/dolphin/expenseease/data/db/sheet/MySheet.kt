package com.dolphin.expenseease.data.db.sheet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "my_sheet",
    indices = [Index(value = ["sheet_name", "sheet_link", "email"], unique = true)])
data class MySheet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "sheet_name") val sheetName: String,
    @ColumnInfo(name = "sheet_link") val sheetLink: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)