package com.example.util

import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {

    /**
     * Safely parses user decimal inputs supporting both comma (Brazilian standard)
     * and dot (US standard), e.g., "1,5", "1.5", "1.250,50", "2500"
     */
    fun parseBrazilianDecimal(input: String): Double {
        if (input.isBlank()) return 0.0
        val trimmed = input.trim()

        return try {
            // If it contains both dot and comma (e.g. 1.250,50)
            if (trimmed.contains(".") && trimmed.contains(",")) {
                trimmed.replace(".", "").replace(",", ".").toDouble()
            } else if (trimmed.contains(",")) {
                // If it has just comma (e.g. 1500,5 or 0,15)
                trimmed.replace(",", ".").toDouble()
            } else {
                // If it has dot or just integer (e.g. 1500.5 or 1500)
                trimmed.toDouble()
            }
        } catch (e: Exception) {
            0.0
        }
    }

    /**
     * Formats a double to clean localized string
     */
    fun formatDecimal(value: Double, decimals: Int = 2): String {
        return if (value % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", value)
        } else {
            String.format(Locale.US, "%.${decimals}f", value)
        }
    }

    /**
     * Formats timestamp in milliseconds to Brazilian standard date format DD/MM/AAAA
     */
    fun formatDateToBrazilian(timestampMillis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
        return sdf.format(Date(timestampMillis))
    }

    /**
     * Validates partial or complete 4-digit 24h time input (00:00 to 23:59).
     */
    fun isValidTimeDigits(digits: String): Boolean {
        if (digits.length > 4 || digits.any { !it.isDigit() }) return false
        if (digits.isNotEmpty() && digits[0] !in '0'..'2') return false
        if (digits.length >= 2) {
            val allowedSecondHour = if (digits[0] == '2') '0'..'3' else '0'..'9'
            if (digits[1] !in allowedSecondHour) return false
        }
        if (digits.length >= 3 && digits[2] !in '0'..'5') return false
        return true
    }

    /**
     * Formats up to 4 digits into HH:MM representation (e.g. "0730" -> "07:30", "073" -> "07:3").
     */
    fun formatTimeDigitsToHhMm(digits: String): String {
        val clean = digits.filter { it.isDigit() }.take(4)
        return if (clean.length <= 2) {
            clean
        } else {
            "${clean.substring(0, 2)}:${clean.substring(2)}"
        }
    }
}
