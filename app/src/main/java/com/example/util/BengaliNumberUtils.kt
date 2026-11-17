package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object BengaliNumberUtils {
    private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toEnglishDigits(input: String): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val idx = bengaliDigits.indexOf(ch)
            sb.append(if (idx >= 0) ('0' + idx) else ch)
        }
        return sb.toString()
    }

    fun toBengaliDigits(input: String): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bengaliDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatScript(input: String, useBengaliNumerals: Boolean): String {
        return if (useBengaliNumerals) toBengaliDigits(input) else toEnglishDigits(input)
    }

    fun parseDoubleOrNull(raw: String): Double? {
        val cleaned = toEnglishDigits(raw)
            .replace(",", "")
            .trim()
        if (cleaned.isEmpty()) return null
        return cleaned.toDoubleOrNull()
    }

    fun formatDecimal(
        value: Double,
        decimals: Int = 4,
        useBengaliNumerals: Boolean = true,
    ): String {
        val pattern = if (decimals <= 0) "0" else "0.${"#".repeat(decimals)}"
        val df = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        val formatted = df.format(value)
        return formatScript(formatted, useBengaliNumerals)
    }

    fun formatFixedDecimal(
        value: Double,
        decimals: Int = 2,
        useBengaliNumerals: Boolean = true,
    ): String {
        val pattern = if (decimals <= 0) "0" else "0.${"0".repeat(decimals)}"
        val df = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        val formatted = df.format(value)
        return formatScript(formatted, useBengaliNumerals)
    }
}
