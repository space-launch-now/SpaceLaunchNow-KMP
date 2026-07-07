package me.calebjones.spacelaunchnow.data.notifications

/**
 * One NSE delivery-decision breadcrumb, written by NotificationService.swift as
 * a pipe-delimited "ts|type|decision|reason" string in App Group UserDefaults.
 */
data class NseBreadcrumb(
    val timestampEpochSeconds: Long,
    val type: String,
    val decision: String,
    val reason: String,
) {
    companion object {
        fun parse(entry: String): NseBreadcrumb? {
            val parts = entry.split("|", limit = 4)
            if (parts.size < 4) return null
            val ts = parts[0].toLongOrNull() ?: return null
            return NseBreadcrumb(ts, parts[1], parts[2], parts[3])
        }
    }
}
