package me.calebjones.spacelaunchnow.util

import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.domain.model.NetPrecision
import me.calebjones.spacelaunchnow.ui.preview.PreviewData
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import org.jetbrains.compose.resources.getString
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.countdown_label_days
import spacelaunchnow_kmp.composeapp.generated.resources.countdown_label_hours
import spacelaunchnow_kmp.composeapp.generated.resources.countdown_label_minutes
import spacelaunchnow_kmp.composeapp.generated.resources.countdown_label_seconds
import spacelaunchnow_kmp.composeapp.generated.resources.time_compact_after
import spacelaunchnow_kmp.composeapp.generated.resources.time_compact_before
import spacelaunchnow_kmp.composeapp.generated.resources.time_unit_days

/**
 * English output of the time helpers must match what the app showed before the strings moved to
 * keys (spec 017, T7). Plural categories per language are proven in `i18n/PluralCategoryTest`.
 */
class LocalizedTimeTextTest {
    private val defaultLocale = Locale.getDefault()

    @AfterTest
    fun restoreLocale() = Locale.setDefault(defaultLocale)

    private suspend fun timeline(relativeTime: String?) = DateTimeUtil.timelineRelativeTime(relativeTime).load()

    @Test
    fun englishTimelineOffsetsAreUnchanged() = runTest {
        // Test negative durations (before NET)
        assertEquals("T - 38m", timeline("-PT38M"))
        assertEquals("T - 45s", timeline("-PT45S"))
        assertEquals("T - 2h", timeline("-PT2H"))
        assertEquals("T - 1h 30m", timeline("-PT1H30M"))
        assertEquals("T - 2h 15m 30s", timeline("-PT2H15M30S"))
        
        // Test positive durations (after NET)
        assertEquals("T + 38m", timeline("PT38M"))
        assertEquals("T + 45s", timeline("PT45S"))
        assertEquals("T + 2h", timeline("PT2H"))
        assertEquals("T + 1h 30m", timeline("PT1H30M"))
        assertEquals("T + 2h 15m 30s", timeline("PT2H15M30S"))
        
        // Test edge cases
        assertEquals("T + 0s", timeline(""))
        assertEquals("T + 0s", timeline(null))
        assertEquals("T + 0s", timeline("PT"))
        assertEquals("T + 0s", timeline("invalid"))
        
        // Test single components
        assertEquals("T - 1h", timeline("-PT1H"))
        assertEquals("T + 30m", timeline("PT30M"))
        assertEquals("T - 15s", timeline("-PT15S"))

        // Test durations with days (beyond 24 hours)
        assertEquals("T - 1d 14h", timeline("-P1DT14H"))
        assertEquals("T + 2d", timeline("P2D"))
        assertEquals("T - 3d 2h 30m", timeline("-P3DT2H30M"))
        assertEquals("T + 1d 6h 15m 10s", timeline("P1DT6H15M10S"))
        assertEquals("T + 1d", timeline("P1D"))
        assertEquals("T - 7d", timeline("-P7D"))
        assertEquals("T + 0s", timeline("P"))
    }


    @Test
    fun englishCountdownLabelsAndUnitsAreUnchanged() = runTest {
        Locale.setDefault(Locale.ENGLISH)
        assertEquals("DAYS", getString(Res.string.countdown_label_days))
        assertEquals("HOURS", getString(Res.string.countdown_label_hours))
        assertEquals("MINUTES", getString(Res.string.countdown_label_minutes))
        assertEquals("SECONDS", getString(Res.string.countdown_label_seconds))
        assertEquals("T- 1d 06h", getString(Res.string.time_compact_before, "1d 06h"))
        assertEquals("T+ 1d 06h", getString(Res.string.time_compact_after, "1d 06h"))
        assertEquals("12d", getString(Res.string.time_unit_days, "12"))
    }

    @Test
    fun englishCompactCountdownIsUnchanged() = runTest {
        Locale.setDefault(Locale.ENGLISH)
        assertEquals("T- 1d 06h 05m 09s", DateTimeUtil.compactCountdown(1, 6, 5, 9, isPast = false).load())
        assertEquals("T+ 12d 00h 59m 59s", DateTimeUtil.compactCountdown(12, 0, 59, 59, isPast = true).load())
    }

    @Test
    fun englishRelativeDaysAreUnchanged() = runTest {
        Locale.setDefault(Locale.ENGLISH)
        val now = Clock.System.now()
        val time = DateTimeUtil.formatLaunchTime(now, useUtc = true)
        assertEquals("Today at $time", DateTimeUtil.relativeLaunchDateTime(now, useUtc = true).load())
        val tomorrow = now + 1.days
        assertEquals(
            "Tomorrow at ${DateTimeUtil.formatLaunchTime(tomorrow, true)}",
            DateTimeUtil.relativeLaunchDateTime(tomorrow, useUtc = true).load()
        )
        val yesterday = now - 1.days
        assertEquals(
            "Yesterday at ${DateTimeUtil.formatLaunchTime(yesterday, true)}",
            DateTimeUtil.relativeLaunchDateTime(yesterday, useUtc = true).load()
        )
        val weekday = now + 3.days
        val text = DateTimeUtil.relativeLaunchDateTime(weekday, useUtc = true).load()
        val names = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        assertEquals(true, names.any { text == "$it at ${DateTimeUtil.formatLaunchTime(weekday, true)}" }, text)
    }

    @Test
    fun englishPrecisionLabelsAreUnchanged() = runTest {
        Locale.setDefault(Locale.ENGLISH)
        val net = Instant.parse("2026-08-15T12:00:00Z")
        fun launchWith(id: Int?) = PreviewData.domainLaunchSpaceX.copy(
            net = net,
            netPrecision = id?.let { NetPrecision(id = it, name = "p", abbrev = null, description = null) }
        )
        suspend fun text(id: Int?) = DateTimeUtil.dateWithPrecisionFallback(launchWith(id), useUtc = true).load()
        assertEquals("Q3 2026", text(9))
        assertEquals("H1 2026", text(12))
        assertEquals("H2 2026", text(13))
        assertEquals("NET 2026", text(14))
        assertEquals("FY 2026", text(15))
        assertEquals("Decade 2026", text(16))
        assertEquals(
            "TBD",
            DateTimeUtil.dateWithPrecisionFallback(launchWith(null).copy(net = null)).load()
        )
    }
}
