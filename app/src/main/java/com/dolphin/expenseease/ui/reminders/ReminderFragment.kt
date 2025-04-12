package com.dolphin.expenseease.ui.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.reminder.Reminder
import com.dolphin.expenseease.data.model.Alert
import com.dolphin.expenseease.databinding.FragmentReminderBinding
import com.dolphin.expenseease.listeners.AddReminderListener
import com.dolphin.expenseease.listeners.OnClickAlertListener
import com.dolphin.expenseease.listeners.ReminderEditListener
import com.dolphin.expenseease.utils.DialogUtils.showAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class ReminderFragment : Fragment() {
    private val viewModel: ReminderViewModel by viewModels()
    private var _binding: FragmentReminderBinding? = null
    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var reminderList: MutableList<Reminder>
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var updateIndex: Int = -1

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
        initViews()
        return root
    }

    private fun initViews() {
        reminderAdapter =
            ReminderAdapter(requireContext(), reminderList, object : ReminderEditListener {
                override fun onReminderEdit(
                    reminder: Reminder,
                    index: Int
                ) {
                    updateIndex = index
                    addReminderDialog(reminder)
                }

                override fun onReminderRemove(
                    reminder: Reminder,
                    index: Int
                ) {
                    val alert = Alert(getString(R.string.delete), getString(R.string.del_msg))
                    showAlertDialog(requireContext(), alert, object : OnClickAlertListener {
                        override fun onAcknowledge(isOkay: Boolean) {
                            if (isOkay) {
                                requireActivity().runOnUiThread {
                                    reminderList.remove(reminder)
                                    reminderAdapter.notifyItemRemoved(index)
                                    viewModel.deleteReminder(reminder)
                                }
                            }
                        }
                    })
                }
            })
        binding.recyclerReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerReminders.adapter = reminderAdapter

        binding.fab.setOnClickListener {
            addReminderDialog()
        }
    }

    private fun addReminderDialog(reminderToUpdate: Reminder? = null) {
        val addReminderBottomSheet =
            AddReminderSheet(reminderToUpdate, object : AddReminderListener {
                override fun onReminderAdd(notes: String, monthYear: String) {
                    coroutineScope.launch {
                        val reminder = Reminder(
                            notes = notes,
                            dateTime = monthYear,
                            createdAt = Date().time,
                            updatedAt = Date().time
                        )
                        if (reminderToUpdate == null) {
                            viewModel.addReminder(reminder)
                        } else {
                            requireActivity().runOnUiThread {
                                reminderList[updateIndex] = reminder
                                reminderAdapter.notifyItemChanged(updateIndex)
                                viewModel.updateReminder(reminder)
                            }
                        }
                    }
                }
            })
        addReminderBottomSheet.show(childFragmentManager, AddReminderSheet.TAG)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.allReminders.observe(viewLifecycleOwner) {
            reminderList.clear()
            reminderList.addAll(it)
            reminderAdapter.notifyDataSetChanged()
            setView(reminderList.isNotEmpty())
        }
    }

    private fun setView(hasItems: Boolean) {
        binding.textNoItems.visibility = if (hasItems) View.GONE else View.VISIBLE
        binding.recyclerReminders.visibility = if (hasItems) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}