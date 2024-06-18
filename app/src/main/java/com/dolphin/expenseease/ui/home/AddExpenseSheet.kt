package com.dolphin.expenseease.ui.home

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.dolphin.expenseease.data.db.expense.Expense
import com.dolphin.expenseease.databinding.SheetAddExpenseBinding
import com.dolphin.expenseease.listeners.AddExpenseListener
import com.dolphin.expenseease.utils.DateUtils.getTodayDate
import com.dolphin.expenseease.utils.ExtensiveFunctions.showToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddExpenseSheet(private val listener: AddExpenseListener) : BottomSheetDialogFragment() {
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
        val txtAmount = binding.txtAmount
        txtAmount.requestFocus()

        binding.btnAdd.setOnClickListener {
            val amount = txtAmount.text.toString()
            val type = binding.txtExpenseType.selectedItem.toString()
            val expenseNotes = binding.txtExpenseNotes.text.toString()

            if (amount.trim().isEmpty() || type.trim().isEmpty() || expenseNotes.trim().isEmpty()) {
                binding.root.context.showToast("Please fill all fields")
                return@setOnClickListener
            }

            val expense = Expense(
                amount = amount.toDouble(),
                type = type,
                notes = expenseNotes,
                date = getTodayDate()
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