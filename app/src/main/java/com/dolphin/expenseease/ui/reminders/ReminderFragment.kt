package com.dolphin.expenseease.ui.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolphin.expenseease.data.db.reminder.Reminder
import com.dolphin.expenseease.databinding.FragmentReminderBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class ReminderFragment: Fragment() {
    private val viewModel: ReminderViewModel by viewModels()
    private var _binding: FragmentReminderBinding? = null
    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var reminderList: MutableList<Reminder>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reminderList = mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReminderBinding.inflate(inflater, container, false)
        val root: View = binding.root
        for (i in 1..10) {
            addReminder()
        }
        initViews()
        return root
    }

    private fun initViews() {
        reminderAdapter = ReminderAdapter(requireContext(), reminderList)
        binding.recyclerReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerReminders.adapter = reminderAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.allReminders.observe(viewLifecycleOwner) {
            reminderList.clear()
            reminderList.addAll(it)
            reminderAdapter.notifyDataSetChanged()
        }
    }

    private fun addReminder() {
        val reminder = Reminder( notes = "Lunch", dateTime = "07/06/2024 05:00", createdAt = Date().time, updatedAt = Date().time)
        viewModel.insertReminder(reminder)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}