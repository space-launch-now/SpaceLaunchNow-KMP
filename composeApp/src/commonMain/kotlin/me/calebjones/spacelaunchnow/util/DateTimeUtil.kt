package me.calebjones.spacelaunchnow.util

import androidx.compose.runtime.Composable
import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.util.logging.logger
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.*

/**
 * Text that still waits for the device language. Compose string resources load only inside a
 * composable ([resolve]) or a coroutine ([load]), so the pure formatting code builds this tree
 * and the caller picks how to read it.
 */
sealed interface LocalizedText {
    /** Text that needs no translation, such as a date the platform already formatted. */
    data class Plain(val value: String) : LocalizedText

    /** A string resource. An argument may itself be a [LocalizedText]. */
    data class Str(val res: StringResource, val args: List<Any> = emptyList()) : LocalizedText

    /** Parts joined by [separator]. */
    data class Joined(val parts: List<LocalizedText>, val separator: String = " ") : LocalizedText
}

@Composable
fun LocalizedText.resolve(): String = when (this) {
    is LocalizedText.Plain -> value
    is LocalizedText.Str -> {
        val resolved = ArrayList<Any>(args.size)
        for (arg in args) resolved.add(if (arg is LocalizedText) arg.resolve() else arg)
        stringResource(res, *resolved.toTypedArray())
    }
    is LocalizedText.Joined -> {
        val resolved = ArrayList<String>(parts.size)
        for (part in parts) resolved.add(part.resolve())
        resolved.joinToString(separator)
    }
}

suspend fun LocalizedText.load(): String = when (this) {
    is LocalizedText.Plain -> value
    is LocalizedText.Str -> getString(res, *args.map { if (it is LocalizedText) it.load() else it }.toTypedArray())
    is LocalizedText.Joined -> parts.map { it.load() }.joinToString(separator)
}

/**
 * Utility object for formatting dates and times in a locale-appropriate way using platform-specific formatters
 */
object DateTimeUtil {
    private val log by lazy { logger() }

    /**
     * Formats an Instant to a human-readable local date and time string using platform-specific locale formatting
     * Android: Uses DateTimeFormatter with locale
     * Other platforms: Falls back to kotlinx-datetime formatting
     *
     * @param instant The instant to format
     * @param useUtc If true, displays time in UTC instead of local timezone
     */
    fun formatLaunchDateTime(instant: Instant, useUtc: Boolean = false): String {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            val localDateTime = instant.toLocalDateTime(timeZone)
            formatLocalDateTime(localDateTime, useUtc)
        } catch (e: Exception) {
            instant.toString()
        }
    }
    
    /**
     * Formats an Instant to a shorter date string using locale formatting
     *
     * @param instant The instant to format
     * @param useUtc If true, displays date in UTC instead of local timezone
     */
    fun formatLaunchDate(instant: Instant, useUtc: Boolean = false): String {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            val localDateTime = instant.toLocalDateTime(timeZone)
            formatLocalDate(localDateTime, useUtc)
        } catch (e: Exception) {
            instant.toString()
        }
    }
    
    /**
     * Formats an Instant to just the time using locale formatting
     *
     * @param instant The instant to format
     * @param useUtc If true, displays time in UTC instead of local timezone
     */
    fun formatLaunchTime(instant: Instant, useUtc: Boolean = false): String {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            val localDateTime = instant.toLocalDateTime(timeZone)
            formatLocalTime(localDateTime, useUtc)
        } catch (e: Exception) {
            instant.toString()
        }
    }
    
    /**
     * Formats an Instant to a relative format if it's soon, otherwise absolute
     * Examples: "Today at 14:30", "Tomorrow at 09:15", "Dec 25, 2024 at 14:30"
     *
     * @param instant The instant to format
     * @param useUtc If true, uses UTC timezone for calculations and display
     */
    @Composable
    fun formatLaunchDateTimeRelative(instant: Instant, useUtc: Boolean = false): String =
        relativeLaunchDateTime(instant, useUtc).resolve()

    /** Same as [formatLaunchDateTimeRelative], for code that is not a composable. */
    fun relativeLaunchDateTime(instant: Instant, useUtc: Boolean = false): LocalizedText {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            val localDateTime = instant.toLocalDateTime(timeZone)
            val now = System.now().toLocalDateTime(timeZone)

            val daysDiff = localDateTime.date.toEpochDays() - now.date.toEpochDays()
            val time = formatLaunchTime(instant, useUtc)

            when (daysDiff) {
                0L -> LocalizedText.Str(Res.string.time_today_at, listOf(time))
                1L -> LocalizedText.Str(Res.string.time_tomorrow_at, listOf(time))
                (-1L) -> LocalizedText.Str(Res.string.time_yesterday_at, listOf(time))
                in 2..6 -> LocalizedText.Str(
                    Res.string.time_weekday_at,
                    listOf(LocalizedText.Str(weekdayName(localDateTime.dayOfWeek)), time)
                )
                else -> LocalizedText.Plain(formatLaunchDateTime(instant, useUtc))
            }
        } catch (e: Exception) {
            LocalizedText.Plain(instant.toString())
        }
    }

    private fun weekdayName(day: DayOfWeek): StringResource = when (day) {
        DayOfWeek.MONDAY -> Res.string.time_weekday_monday
        DayOfWeek.TUESDAY -> Res.string.time_weekday_tuesday
        DayOfWeek.WEDNESDAY -> Res.string.time_weekday_wednesday
        DayOfWeek.THURSDAY -> Res.string.time_weekday_thursday
        DayOfWeek.FRIDAY -> Res.string.time_weekday_friday
        DayOfWeek.SATURDAY -> Res.string.time_weekday_saturday
        DayOfWeek.SUNDAY -> Res.string.time_weekday_sunday
    }

    /**
     * Formats an ISO-8601 duration string to a relative time format for timeline events
     * Examples: 
     * - "-PT38M" -> "T - 38m"
     * - "PT2H30M" -> "T + 2h 30m"
     * - "-PT45S" -> "T - 45s"
     * - "PT1H" -> "T + 1h"
     * - "-P1DT14H" -> "T - 1d 14h"
     * - "P2D" -> "T + 2d"
     */
    @Composable
    fun formatTimelineRelativeTime(relativeTime: String?): String =
        timelineRelativeTime(relativeTime).resolve()

    /** Same as [formatTimelineRelativeTime], for code that is not a composable. */
    fun timelineRelativeTime(relativeTime: String?): LocalizedText {
        val zero = timelineText(negative = false, listOf(unitText(Res.string.time_unit_seconds, 0)))
        if (relativeTime.isNullOrBlank()) return zero
        
        return try {
            // Parse ISO-8601 duration format (e.g., "-PT38M", "PT2H30M15S", "-P1DT14H")
            val trimmed = relativeTime.trim()
            val isNegative = trimmed.startsWith("-")
            val durationPart = if (isNegative) trimmed.substring(1) else trimmed
            
            // Must start with "P" for a valid ISO-8601 duration
            if (!durationPart.startsWith("P")) {
                return zero
            }
            
            val afterP = durationPart.substring(1) // Remove "P"
            
            // Split into date part and time part at "T"
            val tIndex = afterP.indexOf('T')
            val datePart = if (tIndex >= 0) afterP.substring(0, tIndex) else afterP
            val timePart = if (tIndex >= 0) afterP.substring(tIndex + 1) else ""
            
            // Parse days (D) from the date part
            var days = 0
            val daysMatch = Regex("(\\d+)D").find(datePart)
            if (daysMatch != null) {
                days = daysMatch.groupValues[1].toInt()
            }
            
            // Parse hours, minutes, seconds from the time part
            var hours = 0
            var minutes = 0
            var seconds = 0
            
            var remaining = timePart
            
            // Parse hours (H)
            val hoursMatch = Regex("(\\d+)H").find(remaining)
            if (hoursMatch != null) {
                hours = hoursMatch.groupValues[1].toInt()
                remaining = remaining.replace(hoursMatch.value, "")
            }
            
            // Parse minutes (M)
            val minutesMatch = Regex("(\\d+)M").find(remaining)
            if (minutesMatch != null) {
                minutes = minutesMatch.groupValues[1].toInt()
                remaining = remaining.replace(minutesMatch.value, "")
            }
            
            // Parse seconds (S)
            val secondsMatch = Regex("(\\d+)S").find(remaining)
            if (secondsMatch != null) {
                seconds = secondsMatch.groupValues[1].toInt()
            }
            
            // Format the result
            val parts = mutableListOf<LocalizedText>()

            if (days > 0) parts.add(unitText(Res.string.time_unit_days, days))
            if (hours > 0) parts.add(unitText(Res.string.time_unit_hours, hours))
            if (minutes > 0) parts.add(unitText(Res.string.time_unit_minutes, minutes))
            if (seconds > 0) parts.add(unitText(Res.string.time_unit_seconds, seconds))

            // If no components found, default to 0s
            if (parts.isEmpty()) parts.add(unitText(Res.string.time_unit_seconds, 0))

            timelineText(isNegative, parts)
        } catch (e: Exception) {
            zero
        }
    }

    private fun unitText(unit: StringResource, value: Int) = LocalizedText.Str(unit, listOf(value.toString()))

    private fun timelineText(negative: Boolean, parts: List<LocalizedText>) = LocalizedText.Str(
        if (negative) Res.string.time_timeline_before else Res.string.time_timeline_after,
        listOf(LocalizedText.Joined(parts))
    )

    /**
     * Parses an ISO-8601 duration string into total seconds (signed).
     * Negative durations (before NET) return negative values.
     * Returns null if the string cannot be parsed.
     *
     * Examples:
     * - "-PT38M" -> -2280
     * - "PT2H30M" -> 9000
     * - "-P1DT14H" -> -136800
     * - "P2D" -> 172800
     */
    fun parseDurationToSeconds(relativeTime: String?): Long? {
        if (relativeTime.isNullOrBlank()) return null

        return try {
            val trimmed = relativeTime.trim()
            val isNegative = trimmed.startsWith("-")
            val durationPart = if (isNegative) trimmed.substring(1) else trimmed

            if (!durationPart.startsWith("P")) return null

            val afterP = durationPart.substring(1)
            val tIndex = afterP.indexOf('T')
            val datePart = if (tIndex >= 0) afterP.substring(0, tIndex) else afterP
            val timePart = if (tIndex >= 0) afterP.substring(tIndex + 1) else ""

            var days = 0L
            val daysMatch = Regex("(\\d+)D").find(datePart)
            if (daysMatch != null) {
                days = daysMatch.groupValues[1].toLong()
            }

            var hours = 0L
            var minutes = 0L
            var seconds = 0L

            val hoursMatch = Regex("(\\d+)H").find(timePart)
            if (hoursMatch != null) hours = hoursMatch.groupValues[1].toLong()

            val minutesMatch = Regex("(\\d+)M").find(timePart)
            if (minutesMatch != null) minutes = minutesMatch.groupValues[1].toLong()

            val secondsMatch = Regex("(\\d+)S").find(timePart)
            if (secondsMatch != null) seconds = secondsMatch.groupValues[1].toLong()

            val totalSeconds = days * 86400 + hours * 3600 + minutes * 60 + seconds

            if (totalSeconds == 0L && datePart.isEmpty() && timePart.isEmpty()) return null

            if (isNegative) -totalSeconds else totalSeconds
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Determines whether a timeline event has already occurred based on the launch NET and
     * the event's relative time offset.
     *
     * @param relativeTime ISO-8601 duration string relative to NET (e.g., "-PT38M", "PT2H30M")
     * @param net The launch NET (Network Event Time) as an Instant
     * @return true if the event time is in the past, false otherwise. Returns false if parsing fails.
     */
    fun isTimelineEventPassed(relativeTime: String?, net: Instant): Boolean {
        val offsetSeconds = parseDurationToSeconds(relativeTime) ?: return false
        val eventTime = net + offsetSeconds.seconds
        return eventTime <= System.now()
    }

    /**
     * Formats a domain Launch's NET date with custom precision fallback logic.
     */
    @Composable
    fun formatDateWithPrecisionFallback(launch: me.calebjones.spacelaunchnow.domain.model.Launch, useUtc: Boolean = false): String =
        dateWithPrecisionFallback(launch, useUtc).resolve()

    /** Same as [formatDateWithPrecisionFallback], for code that is not a composable. */
    fun dateWithPrecisionFallback(launch: me.calebjones.spacelaunchnow.domain.model.Launch, useUtc: Boolean = false): LocalizedText {
        val net = launch.net ?: return LocalizedText.Str(Res.string.time_tbd)
        val precisionId = launch.netPrecision?.id

        return dateWithPrecisionFallback(net, precisionId, useUtc)
    }

    private fun dateWithPrecisionFallback(net: kotlinx.datetime.Instant, precisionId: Int?, useUtc: Boolean): LocalizedText {
        val year = formatYear(net, useUtc)
        return when (precisionId) {
            7 -> LocalizedText.Plain(formatMonthYear(net, useUtc))
            8, 9, 10, 11 -> LocalizedText.Str(Res.string.time_quarter_year, listOf(quarterNumber(net, useUtc), year))
            12 -> LocalizedText.Str(Res.string.time_half_1_year, listOf(year))
            13 -> LocalizedText.Str(Res.string.time_half_2_year, listOf(year))
            14 -> LocalizedText.Str(Res.string.time_net_year, listOf(year))
            15 -> LocalizedText.Str(Res.string.time_fiscal_year, listOf(year))
            16 -> LocalizedText.Str(Res.string.time_decade_year, listOf(year))
            else -> LocalizedText.Plain(formatLaunchDate(net, useUtc))
        }
    }

    fun formatYear(instant: Instant, useUtc: Boolean = false): String {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            val ldt = instant.toLocalDateTime(timeZone)
            ldt.year.toString()
        } catch (e: Throwable) {
            ""
        }
    }

    fun formatMonthYear(instant: Instant, useUtc: Boolean = false): String {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            val ldt = instant.toLocalDateTime(timeZone)
            val monthYear = formatLaunchDate(instant, useUtc)
            val year = ldt.year.toString()
            val idx = monthYear.indexOf(year)
            if (idx > 0) {
                val beforeYear = monthYear.substring(0, idx).trim().trimEnd(',')
                "$beforeYear $year"
            } else monthYear
        } catch (_: Throwable) {
            formatLaunchDate(instant, useUtc)
        }
    }

    /** The calendar quarter, 1 to 4. The "Q" prefix comes from the `time_quarter_year` string. */
    private fun quarterNumber(instant: Instant, useUtc: Boolean): Int {
        return try {
            val timeZone = if (useUtc) TimeZone.UTC else TimeZone.currentSystemDefault()
            (instant.toLocalDateTime(timeZone).monthNumber - 1) / 3 + 1
        } catch (_: Throwable) {
            0
        }
    }

}

// Top-level expect declarations for multiplatform date/time formatting
expect fun formatLocalDateTime(localDateTime: LocalDateTime, useUtc: Boolean = false): String
expect fun formatLocalDate(localDateTime: LocalDateTime, useUtc: Boolean = false): String
expect fun formatLocalTime(localDateTime: LocalDateTime, useUtc: Boolean = false): String
