package com.dolphin.expenseease.utils

import java.util.Locale

/**
 * Converts the first letter of a string to uppercase and the rest to lowercase.
 *
 * This function handles various cases, including:
 * - Empty strings.
 * - Strings with only one character.
 * - Strings with leading or trailing whitespace.
 * - Strings that are already capitalized or lowercase.
 * - Strings containing multiple words.
 * - Strings with non-letter characters.
 * - Null input.
 *
 * @param input The string to capitalize.
 * @param locale The locale to use for capitalization rules (defaults to the system's default locale).
 * @return The string with the first letter capitalized and the rest lowercased,
 *         or an empty string if the input is null or empty.
 */
fun String?.capitalizeFirstLetter(locale: Locale = Locale.getDefault()): String {
    if (this.isNullOrEmpty()) {
        return ""
    }

    val trimmedString = this.trim()
    if (trimmedString.isEmpty()) {
        return ""
    }

    return if (trimmedString.length == 1) {
        trimmedString.uppercase(locale)
    } else {
        val firstChar = trimmedString[0]
        val restOfString = trimmedString.substring(1)
        if (firstChar.isLetter()) {
            firstChar.uppercaseChar() + restOfString.lowercase(locale)
        } else {
            firstChar + restOfString.lowercase(locale)
        }
    }
}