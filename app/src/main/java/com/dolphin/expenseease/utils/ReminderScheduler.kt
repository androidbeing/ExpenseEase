package com.dolphin.expenseease.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.reminder.Reminder
import com.dolphin.expenseease.receivers.ReminderBroadcastReceiver

object ReminderScheduler {

    // Schedule a new reminder
    fun scheduleReminder(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("title", context.getString(R.string.app_name))
            putExtra("message", reminder.notes)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.getMillis(),
            pendingIntent
        )
    }

    // Cancel an existing reminder
    fun cancelReminder(context: Context, reminder: Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    // Update an existing reminder
    fun updateReminder(context: Context, reminder: Reminder) {
        cancelReminder(context, reminder)
        scheduleReminder(context, reminder)
    }
}