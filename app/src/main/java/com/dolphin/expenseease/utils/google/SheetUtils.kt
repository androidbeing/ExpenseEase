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
        val expenses = existingData.drop(1).mapNotNull { row ->
            if (row is List<*> && row.size == 4) {
                try {
                    Expense(
                        id = 0, // Assign a default or unique ID if needed
                        date = row[0] as String,
                        type = row[1] as String,
                        amount = (row[2] as String).toDouble(),
                        notes = row[3] as String
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