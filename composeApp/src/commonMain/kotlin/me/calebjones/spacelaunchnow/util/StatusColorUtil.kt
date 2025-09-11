package me.calebjones.spacelaunchnow.util

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Utility object for determining status colors for launches and landings
 * Converted from Java version to Kotlin with Compose Material3 colors
 */
object StatusColorUtil {

    /**
     * Get the appropriate button colors for a launch status
     * @param statusId The launch status ID (1-8, or null for unknown)
     * @return ButtonColors for the status button
     */
    @Composable
    fun getLaunchStatusButtonColors(statusId: Int?): ButtonColors {
        val backgroundColor = getLaunchStatusColor(statusId)
        return ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.White
        )
    }

    /**
     * Get the appropriate color for a launch status
     * @param statusId The launch status ID (1-8, or null for unknown)
     * @return Color for the status
     */
    fun getLaunchStatusColor(statusId: Int?): Color {
        return when (statusId) {
            1 -> Color(0xFF43A047) // GO for launch - Green 600
            2 -> Color(0xFFE53935) // TBD for launch - Red 500
            3 -> Color(0xFF2E7D32) // Success for launch - Green 800
            4 -> Color(0xFFD32F2F) // Failure to launch - Red 700
            5 -> Color(0xFFFF9800) // HOLD - Orange 500
            6 -> Color(0xFF1976D2) // In Flight - Blue 500
            7 -> Color(0xFF78909C) // Partial Failure - Blue Grey 500
            8 -> Color(0xFF455A64) // Partial Failure - Blue Grey 800
            else -> Color(0xFF6A1B9A) // Unknown status - Purple 800
        }
    }

    /**
     * Get the appropriate button colors for a landing status
     * @param landingStatusId The landing status ID (1-3, or null for unknown)
     * @return ButtonColors for the landing status button
     */
    @Composable
    fun getLandingStatusButtonColors(landingStatusId: Int?): ButtonColors {
        val backgroundColor = getLandingStatusColor(landingStatusId)
        return ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.White
        )
    }

    /**
     * Get the appropriate color for a landing status
     * @param landingStatusId The landing status ID (1-3, or null for unknown)
     * @return Color for the landing status
     */
    fun getLandingStatusColor(landingStatusId: Int?): Color {
        return when (landingStatusId) {
            1 -> Color(0xFF2E7D32) // Success - Green 800
            2 -> Color(0xFFD32F2F) // Failure - Red 700
            3 -> Color(0xFF78909C) // Partial/Unknown - Blue Grey 500
            else -> Color(0xFF1976D2) // Default/No attempt - Blue 500
        }
    }

    /**
     * Get human-readable status name for launch status ID
     * @param statusId The launch status ID
     * @return Human-readable status name
     */
    fun getLaunchStatusName(statusId: Int?): String {
        return when (statusId) {
            1 -> "GO"
            2 -> "TBD"
            3 -> "Success"
            4 -> "Failure"
            5 -> "Hold"
            6 -> "In Flight"
            7 -> "Partial Failure"
            8 -> "Partial Failure"
            else -> "Unknown"
        }
    }

    /**
     * Get human-readable status name for landing status ID
     * @param landingStatusId The landing status ID
     * @return Human-readable landing status name
     */
    fun getLandingStatusName(landingStatusId: Int?): String {
        return when (landingStatusId) {
            1 -> "Success"
            2 -> "Failure"
            3 -> "Partial"
            else -> "No Attempt"
        }
    }
}
