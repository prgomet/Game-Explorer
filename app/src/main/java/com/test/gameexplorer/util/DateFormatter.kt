package com.test.gameexplorer.util

import android.util.Log
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility for standardizing date strings from the RAWG API into a more 
 * readable, tactical format.
 */
object DateFormatter {
    private const val TAG = "DateFormatter"
    
    // API format: 2023-10-27
    private val rawApiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    // Desired display: OCT 27, 2023
    private val tacticalDisplayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.US)

    /**
     * Formats a raw date string from the API.
     * Returns a localized placeholder if the date is null or invalid.
     */
    fun format(dateString: String?): String {
        if (dateString.isNullOrBlank()) {
            return "---" // Tactical placeholder for missing intel
        }
        
        return try {
            val parsedDate = LocalDate.parse(dateString, rawApiFormatter)
            parsedDate.format(tacticalDisplayFormatter).uppercase()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse date: $dateString. Falling back to raw value.")
            dateString.uppercase()
        }
    }
}
