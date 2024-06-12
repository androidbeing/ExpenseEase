package com.dolphin.expenseease.ui.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dolphin.expenseease.data.repo.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is Wallet Fragment"
    }
    val text: LiveData<String> = _text
}