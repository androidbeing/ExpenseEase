package com.dolphin.expenseease.utils

import android.content.Context
import android.text.format.DateUtils
import android.widget.Toast
import java.util.Date

object ExtensiveFunctions {

    fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    fun Context.getRelativeTimeString(pastMillis: Long): String {
        val now = System.currentTimeMillis()
        val timeDiff = now - pastMillis

        return when {
            timeDiff < DateUtils.MINUTE_IN_MILLIS -> "Just now"
            timeDiff < DateUtils.HOUR_IN_MILLIS -> DateUtils.getRelativeTimeSpanString(
                pastMillis,
                now,
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
            timeDiff < DateUtils.DAY_IN_MILLIS -> DateUtils.getRelativeTimeSpanString(
                pastMillis,
                now,
                DateUtils.HOUR_IN_MILLIS
            ).toString()
            timeDiff < DateUtils.WEEK_IN_MILLIS -> "Yesterday"
            else -> {
                val date = Date(pastMillis)
                val formatter = android.text.format.DateFormat.getDateFormat(this)
                formatter.format(date) // Or use a custom format if needed
            }
        }
    }
}