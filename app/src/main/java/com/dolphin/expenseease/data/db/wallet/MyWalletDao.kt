package com.dolphin.expenseease.data.db.wallet

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MyWalletDao {

    @Query("SELECT * FROM my_wallet")
    fun getAll(): LiveData<List<MyWallet>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(wallet: MyWallet)

    @Update
    fun update(wallet: MyWallet)

    @Query("UPDATE my_wallet SET balance = :balance WHERE id = :id")
    fun updateBalanceById(id: Int, balance: Double)

    @Delete
    fun delete(wallet: MyWallet)
}