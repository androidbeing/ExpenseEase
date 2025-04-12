package com.dolphin.expenseease.ui.reminders

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.reminder.Reminder
import com.dolphin.expenseease.data.db.wallet.MyWallet
import com.dolphin.expenseease.databinding.SheetAddReminderBinding
import com.dolphin.expenseease.listeners.AddReminderListener
import com.dolphin.expenseease.listeners.MonthListener
import com.dolphin.expenseease.utils.Constants.DATE_TIME_FORMAT
import com.dolphin.expenseease.utils.DateUtils.showDateTimePicker
import com.dolphin.expenseease.utils.ExtensiveFunctions.showToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddReminderSheet(private val reminder: Reminder? = null, private val listener: AddReminderListener) : BottomSheetDialogFragment() {

    private var _binding: SheetAddReminderBinding? = null
    private val binding get() = _binding!!
    private val coroutineScope = CoroutineScope(Dispatchers.IO)


    companion object {
        val TAG = AddReminderSheet::class.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetAddReminderBinding.inflate(inflater, container, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog?.window?.setDecorFitsSystemWindows(false)
        } else {
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        initViews()
        return binding.root
    }

    private fun initViews() {
        if(reminder != null) {
            binding.txtNotes.setText(reminder.notes)
            binding.txtMonthYear.setText(reminder.dateTime)
        }
        
        binding.txtMonthYear.setOnClickListener {
            showDateTimePicker(requireContext(), object : MonthListener {
                override fun onMonthSelected(monthYear: String) {
                    binding.txtMonthYear.setText(monthYear)
                }
            }, DATE_TIME_FORMAT)
        }

        binding.btnAdd.setOnClickListener {
            val txtNotes = binding.txtNotes.text.toString().trim()
            val txtMonthYear = binding.txtMonthYear.text.toString().trim()

            if (txtNotes.isEmpty() || txtMonthYear.isEmpty()) {
                binding.root.context.showToast(getString(R.string.fill_all_fields))
                return@setOnClickListener
            }

            coroutineScope.launch {
                listener.onReminderAdd(txtNotes, txtMonthYear)
                dismiss()
            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }
    }
}