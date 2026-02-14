package com.dolphin.expenseease.data.repo

import androidx.lifecycle.LiveData
import com.dolphin.expenseease.data.db.wallet.MyWallet
import com.dolphin.expenseease.data.db.wallet.MyWalletDao
import javax.inject.Inject

class WalletRepository @Inject constructor(
    private val walletDao: MyWalletDao
) {
    fun getAllWallets(): LiveData<List<MyWallet>> = walletDao.getAll()

    fun getLatestWallet(): LiveData<MyWallet> = walletDao.getLatestBalance()

    fun getWalletAmountAddedBetween(startTime: Long, endTime: Long): Double {
        return walletDao.getWalletAmountAddedBetween(startTime, endTime)
    }

    fun updateWalletBalance(id: Int, balance: Double) {
        walletDao.updateBalanceById(id, balance)
    }

    suspend fun insertWallet(wallet: MyWallet) = walletDao.insert(wallet)

    suspend fun updateWallet(wallet: MyWallet) = walletDao.update(wallet)

    suspend fun deleteWallet(wallet: MyWallet) = walletDao.delete(wallet)
}

