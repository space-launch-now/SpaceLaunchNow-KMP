package me.calebjones.spacelaunchnow.wear.util

import android.content.res.Resources
import me.calebjones.spacelaunchnow.wear.R
import kotlin.time.Clock
import kotlin.time.Instant

/** T-minus text for the launch list and detail screens, e.g. "T-1d 13h". */
internal fun formatWearCountdown(resources: Resources, net: Instant): String {
    val duration = net - Clock.System.now()
    if (duration.isNegative()) return resources.getString(R.string.wear_countdown_launched)

    val totalMinutes = duration.inWholeMinutes
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours >= 24 -> resources.getString(R.string.wear_countdown_days_hours, hours / 24, hours % 24)
        hours > 0 -> resources.getString(R.string.wear_countdown_hours_minutes, hours, minutes)
        minutes > 0 -> resources.getString(R.string.wear_countdown_minutes, minutes)
        else -> resources.getString(R.string.wear_countdown_zero)
    }
}
