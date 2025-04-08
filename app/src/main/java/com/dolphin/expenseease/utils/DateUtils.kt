package com.dolphin.expenseease.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Resources
import android.view.View
import com.dolphin.expenseease.listeners.MonthListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun getTodayDate(): String {
        val currentDate = Date()
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(currentDate)
    }

    fun showMonthYearPicker(context: Context, listener: MonthListener, format: String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, _ ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                val formattedDate = dateFormat.format(calendar.time)
                listener.onMonthSelected(formattedDate)
            },
            year,
            month,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.findViewById<View>(
            Resources.getSystem().getIdentifier("day", "id", "android")
        )?.visibility = View.GONE

        datePickerDialog.show()
    }

    fun showDateTimePicker(context: Context, listener: MonthListener, format: String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                        calendar.set(Calendar.MINUTE, selectedMinute)
                        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                        val formattedDate = dateFormat.format(calendar.time)
                        listener.onMonthSelected(formattedDate)
                    },
                    hour,
                    minute,
                    true
                )
                timePickerDialog.show()
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }
}