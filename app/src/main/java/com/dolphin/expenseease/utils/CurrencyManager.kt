package com.dolphin.expenseease.utils

import java.util.Currency
import java.util.Locale

import android.content.Context
import android.telephony.TelephonyManager

object CurrencyManager {

    private const val CURRENCY_NAME_KEY = "currency_name"
    private const val CURRENCY_SYMBOL_KEY = "currency_symbol"
    private const val CURRENCY_CODE_KEY = "currency_code"
    private const val CURRENCY_DETECTED_KEY = "currency_detected"
    private const val DEFAULT_CURRENCY = "USD" // Universal fallback

    /**
     * Detects and saves currency only if not already detected.
     * Call this from Application.onCreate()
     */
    fun detectAndSaveCurrency(context: Context) {
        // Only detect on first run - don't override user's settings
        val alreadyDetected = PreferenceHelper.getBoolean(CURRENCY_DETECTED_KEY, false)
        if (alreadyDetected) {
            println("Currency already detected, skipping auto-detection")
            return
        }

        forceDetectAndSaveCurrency(context)
    }

    /**
     * Force currency detection and save (useful for manual refresh)
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

        // You can log the results for debugging:
        println("Currency detected and saved:")
        println("Currency Name: $currencyName")
        println("Currency Symbol: $currencySymbol")
        println("Currency code: $currencyCode")
    }

    /**
     * Detects the user's currency using multiple fallback methods:
     * Priority order:
     * 1. SIM operator country (doesn't require permission!)
     * 2. Network operator country (doesn't require permission!)
     * 3. Device locale
     * 4. Default to USD as universal fallback
     */
    private fun detectCurrency(context: Context): Currency {
        var countryCode: String? = null
        var detectionMethod = ""

        println("=== Currency Detection Started ===")

        // Method 1: Try getting country from TelephonyManager
        // Note: getSimOperator() and getNetworkOperator() don't require READ_PHONE_STATE!
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            telephonyManager?.let { tm ->
                // Try SIM operator first (doesn't require permission)
                val simOperator = tm.simOperator
                if (!simOperator.isNullOrEmpty() && simOperator.length >= 3) {
                    // First 3 digits are MCC (Mobile Country Code)
                    val mcc = simOperator.substring(0, 3)
                    val simCountryFromMcc = getCountryCodeFromMcc(mcc)
                    if (simCountryFromMcc != null) {
                        countryCode = simCountryFromMcc
                        detectionMethod = "SIM operator (MCC: $mcc)"
                        println("✓ SIM operator country: $countryCode (MCC: $mcc)")
                    } else {
                        println("✗ Could not determine country from SIM MCC: $mcc")
                    }
                } else {
                    println("✗ SIM operator: empty or invalid")
                }

                // Fallback: Try network operator (doesn't require permission)
                if (countryCode.isNullOrEmpty()) {
                    val networkOperator = tm.networkOperator
                    if (!networkOperator.isNullOrEmpty() && networkOperator.length >= 3) {
                        val mcc = networkOperator.substring(0, 3)
                        val networkCountryFromMcc = getCountryCodeFromMcc(mcc)
                        if (networkCountryFromMcc != null) {
                            countryCode = networkCountryFromMcc
                            detectionMethod = "Network operator (MCC: $mcc)"
                            println("✓ Network operator country: $countryCode (MCC: $mcc)")
                        } else {
                            println("✗ Could not determine country from Network MCC: $mcc")
                        }
                    } else {
                        println("✗ Network operator: empty or invalid")
                    }
                }

                // Alternative: Try simCountryIso if available (may require permission on some devices)
                if (countryCode.isNullOrEmpty()) {
                    try {
                        val simCountry = tm.simCountryIso
                        if (!simCountry.isNullOrEmpty() && simCountry.length == 2) {
                            countryCode = simCountry.uppercase(Locale.getDefault())
                            detectionMethod = "SIM country ISO"
                            println("✓ SIM country ISO: $countryCode")
                        }
                    } catch (e: Exception) {
                        println("✗ SIM country ISO not accessible: ${e.message}")
                    }
                }

                // Alternative: Try networkCountryIso if available
                if (countryCode.isNullOrEmpty()) {
                    try {
                        val networkCountry = tm.networkCountryIso
                        if (!networkCountry.isNullOrEmpty() && networkCountry.length == 2) {
                            countryCode = networkCountry.uppercase(Locale.getDefault())
                            detectionMethod = "Network country ISO"
                            println("✓ Network country ISO: $countryCode")
                        }
                    } catch (e: Exception) {
                        println("✗ Network country ISO not accessible: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("⚠ Error accessing TelephonyManager: ${e.message}")
        }

        // Method 2: If still no country code, use device locale as last resort
        if (countryCode.isNullOrEmpty()) {
            val localeCountry = Locale.getDefault().country
            if (!localeCountry.isNullOrEmpty()) {
                countryCode = localeCountry
                detectionMethod = "Device locale"
                println("⚠ Using device locale country: $countryCode")
                println("  Full device locale: ${Locale.getDefault()}")
                println("  WARNING: Device locale may not reflect actual user location!")
            }
        } else {
            println("✓ Using country from $detectionMethod: $countryCode")
        }

        // Try to get currency from detected country code
        if (!countryCode.isNullOrEmpty()) {
            try {
                val locale = Locale("", countryCode)
                val currency = Currency.getInstance(locale)
                println("✅ Currency detected for $countryCode (via $detectionMethod): ${currency.currencyCode} (${currency.symbol}) - ${currency.displayName}")
                return currency
            } catch (e: IllegalArgumentException) {
                println("✗ No currency found for country code: $countryCode - ${e.message}")
            } catch (e: Exception) {
                println("✗ Error getting currency for country code: $countryCode - ${e.message}")
            }
        }

        // Method 3: Try to get currency directly from default locale
        try {
            val localeCurrency = Currency.getInstance(Locale.getDefault())
            if (localeCurrency != null) {
                println("⚠ Using currency from default locale: ${localeCurrency.currencyCode}")
                return localeCurrency
            }
        } catch (e: Exception) {
            println("✗ Error getting currency from default locale: ${e.message}")
        }

        // Final fallback: USD (most widely recognized)
        println("⚠️ Using default currency: $DEFAULT_CURRENCY")
        return Currency.getInstance(DEFAULT_CURRENCY)
    }

    /**
     * Convert Mobile Country Code (MCC) to ISO country code
     * Reference: https://en.wikipedia.org/wiki/Mobile_country_code
     */
    private fun getCountryCodeFromMcc(mcc: String): String? {
        return when (mcc) {
            "404", "405", "406" -> "IN"  // India
            "234", "235" -> "GB"  // United Kingdom
            "310", "311", "312", "313", "314", "315", "316" -> "US"  // United States
            "262" -> "DE"  // Germany
            "208" -> "FR"  // France
            "222" -> "IT"  // Italy
            "214" -> "ES"  // Spain
            "440" -> "JP"  // Japan
            "450" -> "KR"  // South Korea
            "460", "461" -> "CN"  // China
            "505" -> "AU"  // Australia
            "302" -> "CA"  // Canada
            "724" -> "BR"  // Brazil
            "334" -> "MX"  // Mexico
            "510" -> "ID"  // Indonesia
            "525" -> "SG"  // Singapore
            "520" -> "TH"  // Thailand
            "502" -> "MY"  // Malaysia
            "454" -> "HK"  // Hong Kong
            "466" -> "TW"  // Taiwan
            "432" -> "IR"  // Iran
            "410" -> "PK"  // Pakistan
            "413" -> "LK"  // Sri Lanka
            "470" -> "BD"  // Bangladesh
            "502" -> "MY"  // Malaysia
            "515" -> "PH"  // Philippines
            "530" -> "NZ"  // New Zealand
            "602" -> "EG"  // Egypt
            "655" -> "ZA"  // South Africa
            "621" -> "NG"  // Nigeria
            "634" -> "SD"  // Sudan
            "420" -> "SA"  // Saudi Arabia
            "424" -> "AE"  // UAE
            "427" -> "QA"  // Qatar
            "419" -> "KW"  // Kuwait
            "401" -> "KZ"  // Kazakhstan
            "250" -> "RU"  // Russia
            "255" -> "UA"  // Ukraine
            "257" -> "BY"  // Belarus
            "425" -> "IL"  // Israel
            "286" -> "TR"  // Turkey
            "426" -> "BH"  // Bahrain
            "422" -> "OM"  // Oman
            "421" -> "YE"  // Yemen
            "416" -> "JO"  // Jordan
            "415" -> "LB"  // Lebanon
            "218" -> "BA"  // Bosnia and Herzegovina
            "219" -> "HR"  // Croatia
            "220" -> "RS"  // Serbia
            "260" -> "PL"  // Poland
            "284" -> "BG"  // Bulgaria
            "226" -> "RO"  // Romania
            "231" -> "SK"  // Slovakia
            "232" -> "AT"  // Austria
            "238" -> "DK"  // Denmark
            "240" -> "SE"  // Sweden
            "242" -> "NO"  // Norway
            "244" -> "FI"  // Finland
            "246" -> "LT"  // Lithuania
            "247" -> "LV"  // Latvia
            "248" -> "EE"  // Estonia
            "228" -> "CH"  // Switzerland
            "268" -> "PT"  // Portugal
            "270" -> "LU"  // Luxembourg
            "272" -> "IE"  // Ireland
            "274" -> "IS"  // Iceland
            "276" -> "AL"  // Albania
            "278" -> "MT"  // Malta
            "280" -> "CY"  // Cyprus
            "282" -> "GE"  // Georgia
            "283" -> "AM"  // Armenia
            "297" -> "ME"  // Montenegro
            "293" -> "SI"  // Slovenia
            "294" -> "MK"  // North Macedonia
            "340" -> "GP"  // Guadeloupe
            "362" -> "CW"  // Curaçao
            "346" -> "KY"  // Cayman Islands
            "338" -> "JM"  // Jamaica
            "342" -> "BB"  // Barbados
            "344" -> "AG"  // Antigua and Barbuda
            "348" -> "VG"  // British Virgin Islands
            "352" -> "GD"  // Grenada
            "354" -> "MS"  // Montserrat
            "356" -> "KN"  // Saint Kitts and Nevis
            "358" -> "LC"  // Saint Lucia
            "360" -> "VC"  // Saint Vincent and the Grenadines
            "363" -> "AW"  // Aruba
            "364" -> "BS"  // Bahamas
            "365" -> "AI"  // Anguilla
            "366" -> "DM"  // Dominica
            "368" -> "CU"  // Cuba
            "370" -> "DO"  // Dominican Republic
            "372" -> "HT"  // Haiti
            "374" -> "TT"  // Trinidad and Tobago
            "376" -> "TC"  // Turks and Caicos Islands
            "400" -> "AZ"  // Azerbaijan
            "402" -> "BT"  // Bhutan
            "412" -> "AF"  // Afghanistan
            "414" -> "MM"  // Myanmar
            "417" -> "SY"  // Syria
            "418" -> "IQ"  // Iraq
            "428" -> "MN"  // Mongolia
            "429" -> "NP"  // Nepal
            "430" -> "AE"  // UAE (additional)
            "431" -> "AE"  // UAE (additional)
            "434" -> "UZ"  // Uzbekistan
            "436" -> "TJ"  // Tajikistan
            "437" -> "KG"  // Kyrgyzstan
            "438" -> "TM"  // Turkmenistan
            "441" -> "JP"  // Japan (additional)
            "452" -> "VN"  // Vietnam
            "455" -> "MO"  // Macau
            "456" -> "KH"  // Cambodia
            "457" -> "LA"  // Laos
            "467" -> "KP"  // North Korea
            "472" -> "MV"  // Maldives
            "502" -> "MY"  // Malaysia (additional)
            "514" -> "TL"  // East Timor
            "537" -> "PG"  // Papua New Guinea
            "539" -> "TO"  // Tonga
            "540" -> "SB"  // Solomon Islands
            "541" -> "VU"  // Vanuatu
            "542" -> "FJ"  // Fiji
            "543" -> "WF"  // Wallis and Futuna
            "544" -> "AS"  // American Samoa
            "545" -> "KI"  // Kiribati
            "546" -> "NC"  // New Caledonia
            "547" -> "PF"  // French Polynesia
            "548" -> "CK"  // Cook Islands
            "549" -> "WS"  // Samoa
            "550" -> "FM"  // Micronesia
            "551" -> "MH"  // Marshall Islands
            "552" -> "PW"  // Palau
            "604" -> "MA"  // Morocco
            "605" -> "TN"  // Tunisia
            "606" -> "LY"  // Libya
            "607" -> "GM"  // Gambia
            "608" -> "SN"  // Senegal
            "609" -> "MR"  // Mauritania
            "610" -> "ML"  // Mali
            "611" -> "GN"  // Guinea
            "612" -> "CI"  // Ivory Coast
            "613" -> "BF"  // Burkina Faso
            "614" -> "NE"  // Niger
            "615" -> "TG"  // Togo
            "616" -> "BJ"  // Benin
            "617" -> "MU"  // Mauritius
            "618" -> "LR"  // Liberia
            "619" -> "SL"  // Sierra Leone
            "620" -> "GH"  // Ghana
            "622" -> "TD"  // Chad
            "623" -> "CF"  // Central African Republic
            "624" -> "CM"  // Cameroon
            "625" -> "CV"  // Cape Verde
            "626" -> "ST"  // São Tomé and Príncipe
            "627" -> "GQ"  // Equatorial Guinea
            "628" -> "GA"  // Gabon
            "629" -> "CG"  // Republic of the Congo
            "630" -> "CD"  // Democratic Republic of the Congo
            "631" -> "AO"  // Angola
            "632" -> "GW"  // Guinea-Bissau
            "633" -> "SC"  // Seychelles
            "635" -> "RW"  // Rwanda
            "636" -> "ET"  // Ethiopia
            "637" -> "SO"  // Somalia
            "638" -> "DJ"  // Djibouti
            "639" -> "KE"  // Kenya
            "640" -> "TZ"  // Tanzania
            "641" -> "UG"  // Uganda
            "642" -> "BI"  // Burundi
            "643" -> "MZ"  // Mozambique
            "645" -> "ZM"  // Zambia
            "646" -> "MG"  // Madagascar
            "647" -> "RE"  // Réunion
            "648" -> "ZW"  // Zimbabwe
            "649" -> "NA"  // Namibia
            "650" -> "MW"  // Malawi
            "651" -> "LS"  // Lesotho
            "652" -> "BW"  // Botswana
            "653" -> "SZ"  // Eswatini
            "654" -> "KM"  // Comoros
            "659" -> "SS"  // South Sudan
            "702" -> "BZ"  // Belize
            "704" -> "GT"  // Guatemala
            "706" -> "SV"  // El Salvador
            "708" -> "HN"  // Honduras
            "710" -> "NI"  // Nicaragua
            "712" -> "CR"  // Costa Rica
            "714" -> "PA"  // Panama
            "716" -> "PE"  // Peru
            "722" -> "AR"  // Argentina
            "730" -> "CL"  // Chile
            "732" -> "CO"  // Colombia
            "734" -> "VE"  // Venezuela
            "736" -> "BO"  // Bolivia
            "738" -> "GY"  // Guyana
            "740" -> "EC"  // Ecuador
            "742" -> "GF"  // French Guiana
            "744" -> "PY"  // Paraguay
            "746" -> "SR"  // Suriname
            "748" -> "UY"  // Uruguay
            "750" -> "FK"  // Falkland Islands
            else -> null
        }
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

    /**
     * Manually set currency (for user preference)
     */
    fun setCurrency(currencyCode: String) {
        try {
            val currency = Currency.getInstance(currencyCode)
            PreferenceHelper.putString(CURRENCY_NAME_KEY, currency.displayName)
            PreferenceHelper.putString(CURRENCY_SYMBOL_KEY, currency.symbol)
            PreferenceHelper.putString(CURRENCY_CODE_KEY, currency.currencyCode)
            PreferenceHelper.putBoolean(CURRENCY_DETECTED_KEY, true)
            println("Currency manually set to: ${currency.currencyCode}")
        } catch (e: Exception) {
            println("Error setting currency: ${e.message}")
        }
    }

    /**
     * Clear saved currency to allow re-detection
     */
    fun clearCurrency() {
        PreferenceHelper.putString(CURRENCY_NAME_KEY, "")
        PreferenceHelper.putString(CURRENCY_SYMBOL_KEY, "")
        PreferenceHelper.putString(CURRENCY_CODE_KEY, "")
        PreferenceHelper.putBoolean(CURRENCY_DETECTED_KEY, false)
        println("Currency settings cleared")
    }
}