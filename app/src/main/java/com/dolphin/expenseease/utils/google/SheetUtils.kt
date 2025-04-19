package com.dolphin.expenseease.utils.google

import android.util.Log
import com.dolphin.expenseease.data.db.expense.Expense

object SheetUtils {

    fun getCurrentYearSheetName(): String {
        val currentYear = getCurrentYear()
        return "BUDGET_BUDDY_SHEET_$currentYear"
    }

    fun getCurrentYear(): String {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        return currentYear.toString()
    }

    fun convertToList(existingData: List<List<Any>>): List<Expense> {
        if (existingData.isEmpty() && existingData[0].isEmpty()) {
            Log.e("AAA", "No data found in the spreadsheet.")
            return emptyList()
        }
        val expenses = existingData[0].drop(1).mapNotNull { row ->
            if (row is List<*> && row.size == 6) {
                try {
                    Expense(
                        id = 0, // Assign a default or unique ID if needed
                        date = row[0] as String,
                        type = row[1] as String,
                        amount = (row[2] as String).toDouble(),
                        notes = row[3] as String,
                        createdAt = (row[4] as String).toLong(),
                        updatedAt = (row[5] as String).toLong()
                    )
                } catch (e: Exception) {
                    Log.e("AAA", "Error parsing row: $row", e)
                    null
                }
            } else {
                null
            }
        }

        return expenses
    }
}