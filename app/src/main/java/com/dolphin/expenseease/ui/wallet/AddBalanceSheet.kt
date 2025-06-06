package com.dolphin.expenseease.ui.wallet

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.dolphin.expenseease.R
import com.dolphin.expenseease.data.db.wallet.MyWallet

import com.dolphin.expenseease.databinding.SheetAddBalanceBinding
import com.dolphin.expenseease.listeners.AddBalanceListener
import com.dolphin.expenseease.utils.ExtensiveFunctions.showToast
import com.dolphin.expenseease.utils.capitalizeFirstLetter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddBalanceSheet(private val wallet: MyWallet? = null, private val listener: AddBalanceListener) : BottomSheetDialogFragment() {
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
        if(wallet != null) {
            binding.txtAmount.setText("${wallet.addedAmount}")
            binding.txtExpenseNotes.setText(wallet.notes)
        }

        val txtAmount = binding.txtAmount
        txtAmount?.requestFocus()

        binding.btnAdd?.setOnClickListener {
            val amount = txtAmount?.text.toString()
            val notes = binding.txtExpenseNotes?.text.toString().capitalizeFirstLetter()

            if (amount.trim().isEmpty()) {
                binding.root.context.showToast(getString(R.string.enter_amount))
                return@setOnClickListener
            }

            val addedAmount = amount.toDouble()
            coroutineScope.launch {
                val wallet = MyWallet(id=wallet?.id ?: 0, addedAmount = addedAmount, notes = notes, balance = 0.0)
                listener.onBalanceAdd(wallet)
                dismiss()
            }
        }
        binding.btnCancel?.setOnClickListener {
            dismiss()
        }
    }
}