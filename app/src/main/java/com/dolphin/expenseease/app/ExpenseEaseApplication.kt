package com.dolphin.expenseease.app

import android.app.Application
import com.dolphin.expenseease.utils.CurrencyManager
import com.dolphin.expenseease.utils.PreferenceHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExpenseEaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        PreferenceHelper.init(this)
        CurrencyManager.detectAndSaveCurrency(this)

    }
}