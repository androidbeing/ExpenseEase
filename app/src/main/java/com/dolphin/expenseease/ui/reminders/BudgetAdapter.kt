package com.dolphin.expenseease.ui.reminders

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolphin.expenseease.data.db.reminder.Reminder
import com.dolphin.expenseease.databinding.ItemReminderBinding

class ReminderAdapter(private val context: Context, private val list: MutableList<Reminder>) :
    RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReminderViewHolder {
        val binding: ItemReminderBinding =
            ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReminderViewHolder(context, binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bindItems(list[position])
    }

    class ReminderViewHolder(private val context: Context, private val view: ItemReminderBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bindItems(reminder: Reminder) {
            view.textNotes.text = reminder.notes
            view.textDateTime.text = "${reminder.dateTime}"
            view.textCreatedAt.text = "INR ${reminder.createdAt}"
        }
    }
}