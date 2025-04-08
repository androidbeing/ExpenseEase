package com.dolphin.expenseease.ui.budget

import android.app.DatePickerDialog
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.dolphin.expenseease.R
import com.dolphin.expenseease.databinding.SheetAddBudgetBinding
import com.dolphin.expenseease.listeners.AddBudgetListener
import com.dolphin.expenseease.listeners.MonthListener
import com.dolphin.expenseease.utils.Constants.MONTH_YEAR_FORMAT
import com.dolphin.expenseease.utils.DateUtils.showMonthYearPicker
import com.dolphin.expenseease.utils.ExtensiveFunctions.showToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AddBudgetSheet(private val listener: AddBudgetListener) : BottomSheetDialogFragment() {
    private var _binding: SheetAddBudgetBinding? = null
    private val binding get() = _binding!!
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        val TAG = AddBudgetSheet::class.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetAddBudgetBinding.inflate(inflater, container, false)
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
        val txtMonthYear = binding.txtMonthYear
        val spinnerBudgetType = binding.spinnerBudgetType

        txtAmount?.requestFocus()

        binding.btnAdd?.setOnClickListener {
            val amount = txtAmount?.text.toString()
            val monthYear = txtMonthYear?.text.toString()
            val budgetType = spinnerBudgetType?.selectedItem.toString()

            if (amount.trim().isEmpty()) {
                binding.root.context.showToast(getString(R.string.enter_amount))
                return@setOnClickListener
            }

            val allocatedAmount = amount.toDouble()
            coroutineScope.launch {
                listener.onBudgetAdd(budgetType, allocatedAmount, monthYear)
                dismiss()
            }
        }
        binding.btnCancel?.setOnClickListener {
            dismiss()
        }

        binding.txtMonthYear.setOnClickListener {
            showMonthYearPicker(requireContext(), object : MonthListener {
                override fun onMonthSelected(monthYear: String) {
                    binding.txtMonthYear.setText(monthYear)
                }
            }, MONTH_YEAR_FORMAT)
        }
    }
}