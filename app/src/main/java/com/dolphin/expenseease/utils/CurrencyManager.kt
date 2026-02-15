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
    private const val CURRENCY_DETECTED_KEY = "currency_detected"
    private const val DEFAULT_CURRENCY = "USD"

    /**
     * Detects and saves currency only on first run.
     * Does not override user-set currency on subsequent app launches.
     */
    fun detectAndSaveCurrency(context: Context) {
        val alreadyDetected = PreferenceHelper.getBoolean(CURRENCY_DETECTED_KEY, false)
        if (alreadyDetected) {
            println("Currency already detected, skipping auto-detection")
            return
        }
        forceDetectAndSaveCurrency(context)
    }

    /**
     * Forces currency detection even if already detected.
     * Used for manual "Re-detect Currency" functionality.
     */
    fun forceDetectAndSaveCurrency(context: Context) {
        val currency = detectCurrency(context)
        val currencyName = currency.displayName
        val currencySymbol = currency.symbol
        val currencyCode = currency.currencyCode

        PreferenceHelper.putString(CURRENCY_NAME_KEY, currencyName)
        PreferenceHelper.putString(CURRENCY_SYMBOL_KEY, currencySymbol)
        PreferenceHelper.putString(CURRENCY_CODE_KEY, currencyCode)
        PreferenceHelper.putBoolean(CURRENCY_DETECTED_KEY, true)

        println("Currency saved: $currencyName ($currencyCode) $currencySymbol")
    }

    /**
     * Clears the currency detection flag to allow re-detection.
     */
    fun clearCurrency() {
        PreferenceHelper.putBoolean(CURRENCY_DETECTED_KEY, false)
        println("Currency detection flag cleared")
    }

    /**
     * Detects currency based on multiple sources with proper priority:
     * 1. SIM card country (requires READ_PHONE_STATE permission)
     * 2. Network country
     * 3. Device locale
     * 4. USD as universal fallback
     */
    private fun detectCurrency(context: Context): Currency {
        var countryCode: String? = null
        
        println("=== Currency Detection Started ===")
        
        // Method 1: Try SIM card country (most reliable for mobile users)
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
        
        println("READ_PHONE_STATE permission: $hasPermission")
        
        if (hasPermission) {
            try {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                telephonyManager?.let {
                    // Try SIM country first
                    val simCountry = it.simCountryIso?.uppercase(Locale.getDefault())
                    if (!simCountry.isNullOrEmpty()) {
                        println("SIM country code: $simCountry")
                        countryCode = simCountry
                    }
                    
                    // Method 2: Fallback to network country if SIM is not available
                    if (countryCode.isNullOrEmpty()) {
                        val networkCountry = it.networkCountryIso?.uppercase(Locale.getDefault())
                        if (!networkCountry.isNullOrEmpty()) {
                            println("Network country code: $networkCountry")
                            countryCode = networkCountry
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error accessing TelephonyManager: ${e.message}")
            }
        }
        
        // Method 3: Fallback to device locale (less reliable as it's based on language settings)
        if (countryCode.isNullOrEmpty()) {
            countryCode = Locale.getDefault().country
            println("Using device locale country: $countryCode (Note: based on language settings)")
        }
        
        // Try to get currency for the detected country code
        var currency: Currency? = null
        if (!countryCode.isNullOrEmpty()) {
            try {
                val locale = Locale("", countryCode)
                currency = Currency.getInstance(locale)
                println("Currency detected: ${currency.currencyCode} for country: $countryCode")
            } catch (e: IllegalArgumentException) {
                println("No currency found for country code: $countryCode")
            }
        }
        
        // Method 4: Universal fallback to USD
        if (currency == null) {
            println("Falling back to default currency: $DEFAULT_CURRENCY")
            currency = Currency.getInstance(DEFAULT_CURRENCY)
        }
        
        println("=== Currency Detection Complete: ${currency.currencyCode} ===")
        return currency
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