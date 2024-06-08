package com.dolphin.expenseease.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)