package me.calebjones.spacelaunchnow.sync

import co.touchlab.kermit.Logger
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ApiException

/**
 * Process-wide record of whether the Wearable API exists on this device.
 *
 * Phones without Wear OS support answer every Data Layer call with `ApiException`
 * code 17 (`API_NOT_CONNECTED`, "API is not available on this device"). That is an expected state, not an error: the first
 * failure marks the API unavailable for the rest of the process and later calls are
 * skipped without touching the client. Any other failure is rethrown to the caller.
 */
class WearAvailability {
    private val log = Logger.withTag("WearAvailability")

    @Volatile
    var isUnavailable: Boolean = false
        private set

    /**
     * Runs [block] unless the Wearable API is known to be missing.
     * Returns true if [block] completed, false if it was skipped or the API turned out to be missing.
     */
    suspend fun runIfAvailable(block: suspend () -> Unit): Boolean {
        if (isUnavailable) {
            log.d { "Wearable API unavailable on this device, skipping watch sync" }
            return false
        }
        return try {
            block()
            true
        } catch (e: Exception) {
            if (e.isApiUnavailable()) {
                isUnavailable = true
                log.d { "Wearable API unavailable on this device, skipping watch sync from now on" }
                false
            } else {
                throw e
            }
        }
    }

    internal companion object {
        fun Throwable.isApiUnavailable(): Boolean {
            var current: Throwable? = this
            var depth = 0
            while (current != null && depth < 5) {
                if (current is ApiException && current.statusCode == CommonStatusCodes.API_NOT_CONNECTED) return true
                current = current.cause
                depth++
            }
            return false
        }
    }
}
