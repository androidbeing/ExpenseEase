package com.dolphin.expenseease.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    fun getTodayDate(): String {
        val currentDate = Date()
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(currentDate)
    }
}