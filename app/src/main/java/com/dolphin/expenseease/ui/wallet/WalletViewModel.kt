package com.dolphin.expenseease.ui.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dolphin.expenseease.data.db.wallet.MyWallet
import com.dolphin.expenseease.data.repo.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {
    val allBalances = repository.getAllWallets()

    fun addBalance(wallet: MyWallet) = viewModelScope.launch {
        val data = withContext(Dispatchers.IO) {
            repository.insertWallet(wallet)
        }
    }

    fun getLatestBalance(): LiveData<MyWallet> {
        return repository.getLatestWallet()
    }

    fun updateWallet(wallet: MyWallet) = viewModelScope.launch {
        val data = withContext(Dispatchers.IO) {
            repository.updateWallet(wallet)
        }
    }

    fun deleteWallet(wallet: MyWallet) = viewModelScope.launch {
        val data = withContext(Dispatchers.IO) {
            repository.deleteWallet(wallet)
        }
    }
}