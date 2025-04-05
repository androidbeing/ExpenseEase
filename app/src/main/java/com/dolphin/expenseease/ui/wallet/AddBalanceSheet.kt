package com.dolphin.expenseease.ui.wallet

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

import com.dolphin.expenseease.databinding.SheetAddBalanceBinding
import com.dolphin.expenseease.listeners.AddBalanceListener
import com.dolphin.expenseease.utils.ExtensiveFunctions.showToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddBalanceSheet(private val listener: AddBalanceListener) : BottomSheetDialogFragment() {
    private var _binding: SheetAddBalanceBinding? = null
    private val binding get() = _binding!!
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        val TAG = AddBalanceSheet::class.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetAddBalanceBinding.inflate(inflater, container, false)
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
        txtAmount?.requestFocus()

        binding.btnAdd?.setOnClickListener {
            val amount = txtAmount?.text.toString()
            val notes = binding.txtExpenseNotes?.text.toString()

            if (amount.trim().isEmpty()) {
                binding.root.context.showToast("Please enter an amount")
                return@setOnClickListener
            }

            val addedAmount = amount.toDouble()
            coroutineScope.launch {
                listener.onBalanceAdd(addedAmount, notes)
                dismiss()
            }
        }
        binding.btnCancel?.setOnClickListener {
            dismiss()
        }
    }
}