package com.dolphin.expenseease.ui.reminders

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.reminder.Reminder
import com.dolphin.expenseease.databinding.ItemReminderBinding
import com.dolphin.expenseease.listeners.ReminderEditListener
import com.dolphin.expenseease.utils.ExtensiveFunctions.getRelativeTimeString

class ReminderAdapter(private val context: Context, private val list: MutableList<Reminder>, private val listener: ReminderEditListener) :
    RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReminderViewHolder {
        val binding: ItemReminderBinding =
            ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReminderViewHolder(context, binding, listener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bindItems(list[position], position)
    }

    class ReminderViewHolder(private val context: Context, private val view: ItemReminderBinding, private val listener: ReminderEditListener) :
        RecyclerView.ViewHolder(view.root) {
        fun bindItems(reminder: Reminder, position: Int) {
            view.textNotes.text = reminder.notes
            view.textDateTime.text = "${reminder.dateTime}"
            view.textCreatedAt.text = context.getRelativeTimeString(reminder.updatedAt)

            if (System.currentTimeMillis() > reminder.getMillis()) {
                view.txtStatus.text = context.getString(R.string.lbl_elapsed)
                view.txtStatus.setTextColor(context.getColor(R.color.color_elapsed))
            } else {
                view.txtStatus.text = context.getString(R.string.lbl_upcoming)
                view.txtStatus.setTextColor(context.getColor(R.color.color_upcoming))
            }
            view.imgEdit.setOnClickListener { listener.onReminderEdit(reminder, position) }
            view.imgDelete.setOnClickListener { listener.onReminderRemove(reminder, position) }
        }
    }
}