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
import java.util.*

object DateUtils {

    fun getNDaysBeforeMillis(todayMillis: Long, n: Int): Long {
        val millisInADay = 24 * 60 * 60 * 1000 // Milliseconds in a day
        return todayMillis - (n * millisInADay)
    }

    fun getStartOfCurrentMonthInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getTodayStartInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun parseDateToMillis(dateString: String, format: String = "dd/MM/yyyy"): Long {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.parse(dateString)?.time ?: 0L
    }

    fun formatMillisToDate(millis: Long, format: String = "dd/MM/yyyy"): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun getTodayDate(): String {
        val currentDate = Date()
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(currentDate)
    }

    fun getStartOfMonthInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getCurrentTimeInMillis(): Long {
        return System.currentTimeMillis()
    }


    fun getAllCurrencies(): Map<String, String> {
        val currencies = mutableMapOf<String, String>()
        val availableLocales = Locale.getAvailableLocales()

        for (locale in availableLocales) {
            try {
                val currency = Currency.getInstance(locale)
                if (currency != null) {
                    currencies[currency.currencyCode] = currency.symbol
                }
            } catch (e: Exception) {
                // Ignore locales that don't have a currency
            }
        }

        return currencies
    }

    fun getCurrentAndNextMonthYear(isExpense: Boolean = false): List<String> {
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val monthsList = mutableListOf<String>()

        if (isExpense) {
            for (i in 0 downTo -3) {
                val calendarCopy = calendar.clone() as Calendar
                calendarCopy.add(Calendar.MONTH, i)
                monthsList.add(dateFormat.format(calendarCopy.time))
            }
        } else {
            // Get current month and year
            val currentMonthYear = dateFormat.format(calendar.time)
            monthsList.add(currentMonthYear)

            // Move to the next month
            calendar.add(Calendar.MONTH, 1)
            val nextMonthYear = dateFormat.format(calendar.time)
            monthsList.add(nextMonthYear)
        }
        return monthsList
    }

    fun showMonthYearPicker(context: Context, listener: MonthListener, format: String, minDate: Long=0, maxDate: Long=0) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDate ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDate)
                val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                val formattedDate = dateFormat.format(calendar.time)
                listener.onMonthSelected(formattedDate)
            },
            year,
            month,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        if (minDate > 0 && maxDate > 0) {
            // Set min and max dates
            datePickerDialog.datePicker.minDate = minDate
            datePickerDialog.datePicker.maxDate = maxDate
        }

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