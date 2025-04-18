package com.dolphin.expenseease.utils

import java.util.Currency
import java.util.Locale

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

object CurrencyManager {

    private const val CURRENCY_NAME_KEY = "currency_name"
    private const val CURRENCY_SYMBOL_KEY = "currency_symbol"
    private const val CURRENCY_CODE_KEY = "currency_code"
    private const val DEFAULT_CURRENCY = "INR"

    fun detectAndSaveCurrency(context: Context) {
        var currency: Currency? = null
        var countryCode: String? = null
        // Try getting the country code from the SIM card
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            countryCode = telephonyManager.simCountryIso.uppercase(Locale.getDefault()) // Or getNetworkCountryIso()
        }

        if (!countryCode.isNullOrEmpty()) {
            try {
                currency = Currency.getInstance(countryCode)
            } catch (e: IllegalArgumentException) {
                println("No currency found for country code: $countryCode")
            }
        }

        // Explicitly check for India if the country code or locale is incorrect
        if (currency == null || countryCode == "IN") {
            try {
                currency = Currency.getInstance("INR")
            } catch (e: IllegalArgumentException) {
                println("No currency found for INR")
            }
        }

        // If still no currency, set a default value.
        if (currency == null){
            currency = Currency.getInstance(DEFAULT_CURRENCY)
        }
        val currencyName = currency.displayName
        val currencySymbol = currency.symbol
        val currencyCode = currency.currencyCode

        PreferenceHelper.putString(CURRENCY_NAME_KEY, currencyName)
        PreferenceHelper.putString(CURRENCY_SYMBOL_KEY, currencySymbol)
        PreferenceHelper.putString(CURRENCY_CODE_KEY, currencyCode)

        // You can log the results for debugging:
        println("Currency Name: $currencyName")
        println("Currency Symbol: $currencySymbol")
        println("Currency code: $currencyCode")
    }

    fun getCurrencyName(context: Context): String? {
        return PreferenceHelper.getString(CURRENCY_NAME_KEY, "")
    }

    fun getCurrencySymbol(context: Context): String? {
        return PreferenceHelper.getString(CURRENCY_SYMBOL_KEY, "")
    }

    fun getCurrencyCode(context: Context): String? {
        return PreferenceHelper.getString(CURRENCY_CODE_KEY, "")
    }
}