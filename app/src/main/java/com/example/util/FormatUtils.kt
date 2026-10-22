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
}
