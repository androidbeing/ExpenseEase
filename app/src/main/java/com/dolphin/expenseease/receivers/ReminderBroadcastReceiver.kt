package com.dolphin.expenseease.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dolphin.expenseease.utils.NotificationHelper

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: intent.getStringExtra("message") ?: "Message"
        NotificationHelper.sendNotification(context, title, message)
    }
}