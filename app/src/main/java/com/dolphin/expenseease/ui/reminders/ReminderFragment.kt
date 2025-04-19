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
import com.dolphin.expenseease.utils.PermissionHandler.hasExactAlarmPermission
import com.dolphin.expenseease.utils.PermissionHandler.openAppSettings
import com.dolphin.expenseease.utils.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                                    if (hasExactAlarmPermission(requireContext())) {
                                        ReminderScheduler.cancelReminder(requireContext(), reminder)
                                    } else {
                                        openAppSettings(requireContext())
                                    }
                                }
                            }
                        }
                    })
                }
            })
        binding.recyclerReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerReminders.adapter = reminderAdapter

        binding.fab.setOnClickListener {
            if (hasExactAlarmPermission(requireContext())) {
                addReminderDialog()
            } else {
                val alert = Alert(getString(R.string.permission_req), getString(R.string.reminder_permission_msg))
                showAlertDialog(requireActivity(), alert, object: OnClickAlertListener {
                    override fun onAcknowledge(isOkay: Boolean) {
                        if (isOkay) {
                            openAppSettings(requireContext())
                        }
                    }
                })
            }
        }
    }

    private fun addReminderDialog(reminderToUpdate: Reminder? = null) {
        val addReminderBottomSheet =
            AddReminderSheet(reminderToUpdate, object : AddReminderListener {
                override fun onReminderAdd(remidner: Reminder) {
                    coroutineScope.launch {
                        if (reminderToUpdate == null) {
                            if (hasExactAlarmPermission(requireContext())) {
                                viewModel.addReminder(remidner)
                                ReminderScheduler.scheduleReminder(requireContext(), remidner)
                            } else {
                                openAppSettings(requireContext())
                            }
                        } else {
                            if (hasExactAlarmPermission(requireContext())) {
                                viewModel.updateReminder(remidner)
                                ReminderScheduler.updateReminder(requireContext(), remidner)
                            } else {
                                openAppSettings(requireContext())
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
            reminderList.addAll(it.sortedByDescending { reminder -> reminder.getMillis() })
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