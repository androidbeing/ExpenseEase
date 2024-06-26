package com.dolphin.expenseease.data.di

import com.dolphin.expenseease.data.db.expense.Expense
import retrofit2.http.GET

interface ExpenseService {
    @GET("expenses")
    suspend fun getUsers(): List<Expense>
}