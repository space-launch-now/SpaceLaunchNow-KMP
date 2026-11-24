package me.calebjones.spacelaunchnow.offer

import kotlinx.datetime.Instant

/**
 * Remote Config values for the holiday offer sheet. The default ([DISABLED]) keeps the
 * campaign off. [startMs] and [endMs] are epoch milliseconds, null when unset or unparseable.
 */
data class HolidayOfferConfig(
    val enabled: Boolean,
    val campaignId: String,
    val startMs: Long?,
    val endMs: Long?,
    val closeDelaySeconds: Int,
) {
    companion object {
        const val DEFAULT_CLOSE_DELAY_SECONDS = 5
        const val MAX_CLOSE_DELAY_SECONDS = 30

        val DISABLED = HolidayOfferConfig(
            enabled = false,
            campaignId = "",
            startMs = null,
            endMs = null,
            closeDelaySeconds = DEFAULT_CLOSE_DELAY_SECONDS,
        )

        /** Builds a config from the raw Remote Config strings. Bad values fall back to the safe default. */
        fun fromRaw(
            enabled: String?,
            campaignId: String?,
            start: String?,
            end: String?,
            closeDelaySeconds: String?,
        ): HolidayOfferConfig = HolidayOfferConfig(
            enabled = enabled?.trim().equals("true", ignoreCase = true),
            campaignId = campaignId?.trim().orEmpty(),
            startMs = parseInstantMs(start),
            endMs = parseInstantMs(end),
            closeDelaySeconds = parseCloseDelay(closeDelaySeconds),
        )

        /** ISO-8601 UTC instant to epoch ms; null when blank or unparseable. */
        fun parseInstantMs(raw: String?): Long? {
            val text = raw?.trim().orEmpty()
            if (text.isEmpty()) return null
            return try {
                Instant.parse(text).toEpochMilliseconds()
            } catch (e: Exception) {
                null
            }
        }

        /** Integer seconds clamped to 0..30; a bad value gives the default of 5. */
        fun parseCloseDelay(raw: String?): Int {
            val value = raw?.trim()?.toIntOrNull() ?: return DEFAULT_CLOSE_DELAY_SECONDS
            return value.coerceIn(0, MAX_CLOSE_DELAY_SECONDS)
        }
    }
}
