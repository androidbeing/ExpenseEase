package com.dolphin.expenseease.ui.budget

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.budget.Budget
import com.dolphin.expenseease.databinding.SheetAddBudgetBinding
import com.dolphin.expenseease.listeners.AddBudgetListener
import com.dolphin.expenseease.utils.DateUtils.getCurrentAndNextMonthYear
import com.dolphin.expenseease.utils.ExtensiveFunctions.showToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddBudgetSheet(private val budget: Budget? = null, private val listener: AddBudgetListener) : BottomSheetDialogFragment() {
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

        val monthYearList = mutableListOf<String>()
        monthYearList.addAll(getCurrentAndNextMonthYear())
        val adapter = ArrayAdapter(
            requireContext(), // Context
            android.R.layout.simple_spinner_item, // Layout for each item
            monthYearList // Data source
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.txtMonthYear.adapter = adapter
        binding.txtMonthYear.setSelection(0)

        if (budget != null) {
            binding.txtAmount.setText("${budget.amount}")
            binding.txtMonthYear.setSelection(monthYearList.indexOf(budget.monthYear))
            binding.spinnerBudgetType.setSelection(resources.getStringArray(R.array.expense_types).indexOf(budget.type))
        }

        txtAmount?.requestFocus()

        binding.btnAdd?.setOnClickListener {
            val amount = txtAmount?.text.toString()
            val monthYear = txtMonthYear?.selectedItem.toString()
            val budgetType = spinnerBudgetType?.selectedItem.toString()

            if (amount.trim().isEmpty()) {
                binding.root.context.showToast(getString(R.string.enter_amount))
                return@setOnClickListener
            }

            val allocatedAmount = amount.toDouble()
            coroutineScope.launch {
                val budget = Budget(id=budget?.id ?: 0, type = budgetType, allocatedAmount, monthYear)
                listener.onBudgetAdd(budget)
                dismiss()
            }
        }
        binding.btnCancel?.setOnClickListener {
            dismiss()
        }
    }
}