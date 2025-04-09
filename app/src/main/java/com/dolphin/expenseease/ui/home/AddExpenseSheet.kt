package com.dolphin.expenseease.ui.home

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.databinding.SheetAddExpenseBinding
import com.dolphin.expenseease.listeners.AddExpenseListener
import com.dolphin.expenseease.listeners.MonthListener
import com.dolphin.expenseease.utils.Constants.DATE_FORMAT
import com.dolphin.expenseease.utils.DateUtils.getTodayDate
import com.dolphin.expenseease.utils.DateUtils.showMonthYearPicker
import com.dolphin.expenseease.utils.ExtensiveFunctions.showToast
import com.dolphin.expenseease.utils.capitalizeFirstLetter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddExpenseSheet(private val expense: Expense? = null, private val listener: AddExpenseListener) : BottomSheetDialogFragment() {
    private var _binding: SheetAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        val TAG = AddExpenseSheet::class.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetAddExpenseBinding.inflate(inflater, container, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog?.window?.setDecorFitsSystemWindows(false)
        } else {
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        initViews()
        return binding.root
    }

    private fun initViews() {
        if(expense != null) {
            binding.txtDate.setText(expense.date)
            binding.txtExpenseNotes.setText(expense.notes)
            binding.txtAmount.setText("${expense.amount}")
            binding.txtExpenseType.setSelection(resources.getStringArray(R.array.expense_types).indexOf(expense.type))
        }

        val txtAmount = binding.txtAmount
        txtAmount.requestFocus()
        binding.txtDate.setText(getTodayDate())

        binding.txtDate.setOnClickListener {
            showMonthYearPicker(requireContext(), object : MonthListener {
                override fun onMonthSelected(monthYear: String) {
                    binding.txtDate.setText(monthYear)
                }
            }, DATE_FORMAT)
        }

        binding.btnAdd.setOnClickListener {
            val amount = txtAmount.text.toString()
            val type = binding.txtExpenseType.selectedItem.toString()
            val expenseNotes = binding.txtExpenseNotes.text.toString()
            val spentDate = binding.txtDate.text.toString()

            if (amount.trim().isEmpty() || type.trim().isEmpty() || expenseNotes.trim().isEmpty() || spentDate.trim().isEmpty()) {
                binding.root.context.showToast("Please fill all fields")
                return@setOnClickListener
            }

            val expense = Expense(
                amount = amount.toDouble(),
                type = type,
                notes = expenseNotes.capitalizeFirstLetter(),
                date = spentDate
            )
            coroutineScope.launch {
                listener.onExpenseAdd(expense)
                dismiss()
            }
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }
}