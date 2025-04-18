package com.dolphin.expenseease.data.db.reminder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dolphin.expenseease.utils.Constants.DATE_TIME_FORMAT

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "datetime") val dateTime: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
) {
    fun getMillis(): Long {
        return try {
            val formatter = java.text.SimpleDateFormat(DATE_TIME_FORMAT, java.util.Locale.getDefault())
            formatter.parse(dateTime)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}