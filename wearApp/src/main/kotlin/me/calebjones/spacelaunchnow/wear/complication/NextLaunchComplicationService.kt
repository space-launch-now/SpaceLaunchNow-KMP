package me.calebjones.spacelaunchnow.wear.complication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.SystemClock
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.wear.WearActivity
import me.calebjones.spacelaunchnow.wear.data.EntitlementSyncManager
import me.calebjones.spacelaunchnow.wear.data.WatchLaunchRepository
import me.calebjones.spacelaunchnow.wear.data.model.CachedLaunch
import org.koin.android.ext.android.inject
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class NextLaunchComplicationService : ComplicationDataSourceService() {

    private val watchLaunchRepository: WatchLaunchRepository by inject()
    private val entitlementSyncManager: EntitlementSyncManager by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val log = Logger.withTag("NextLaunchComplication")

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        serviceScope.launch {
            try {
                val isPremium = entitlementSyncManager.isWearOsPremium()
                if (!isPremium) {
                    listener.onComplicationData(buildFreeUserData(request.complicationType))
                    scheduleNextUpdate(remaining = null)
                    return@launch
                }

                // Refresh data (tier 1: direct API, tier 2: DataLayer, tier 3: stale cache)
                watchLaunchRepository.refreshLaunches()

                val nextLaunch = watchLaunchRepository.getNextLaunch()
                if (nextLaunch == null) {
                    listener.onComplicationData(buildNoDataComplication(request.complicationType))
                    scheduleNextUpdate(remaining = null)
                    return@launch
                }

                val complicationData = buildLaunchComplication(nextLaunch, request.complicationType)
                listener.onComplicationData(complicationData)

                // Schedule rapid refresh if countdown < 1 hour; otherwise rely on manifest 30-min period
                val remaining = nextLaunch.net - Clock.System.now()
                scheduleNextUpdate(remaining)
            } catch (e: Exception) {
                log.e(e) { "Failed to build complication data" }
                listener.onComplicationData(buildNoDataComplication(request.complicationType))
                scheduleNextUpdate(remaining = null)
            }
        }
    }

    /**
     * Arms (or cancels) the rapid-refresh alarm.
     *
     * - remaining == null or >= 1 hour → cancel any pending alarm; the manifest
     *   UPDATE_PERIOD_SECONDS=1800 (30 min) handles routine refreshes.
     * - remaining < 1 hour → set a 5-minute alarm so the countdown stays current.
     */
    private fun scheduleNextUpdate(remaining: Duration?) {
        val alarmManager = getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = alarmPendingIntent()

        if (remaining == null || remaining >= 1.hours) {
            // Cancel rapid alarm — the manifest 30-min period is sufficient
            alarmManager.cancel(pendingIntent)
            log.d { "Rapid refresh alarm cancelled (remaining=${remaining})" }
        } else {
            // Arm a 5-minute wakeup alarm
            val triggerAt = SystemClock.elapsedRealtime() + 5.minutes.inWholeMilliseconds
            alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent)
            log.d { "Rapid refresh alarm set in 5 min (remaining=${remaining})" }
        }
    }

    private fun alarmPendingIntent(): PendingIntent {
        val intent = Intent(this, ComplicationUpdateReceiver::class.java).apply {
            action = ComplicationUpdateReceiver.ACTION_UPDATE
        }
        return PendingIntent.getBroadcast(
            this,
            REQUEST_CODE_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {

        val countdownShort = "1D 13H"
        val countdownLong = "T-1D 13H"
        val vehicleText = "Falcon 9"
        val agencyAbbrev = "SpaceX"
        val contentDescription = PlainComplicationText.Builder("Next launch: $vehicleText in $countdownLong").build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(countdownShort).build(),
                contentDescription = contentDescription,
            )
                .setTitle(PlainComplicationText.Builder(agencyAbbrev).build())
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder("$vehicleText — $countdownLong").build(),
                contentDescription = contentDescription,
            ).build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 0.5f,
                min = 0f,
                max = 1f,
                contentDescription = contentDescription,
            )
                .setText(PlainComplicationText.Builder(countdownShort).build())
                .setTitle(PlainComplicationText.Builder(agencyAbbrev).build())
                .build()

            else -> null
        }
    }

    private fun buildLaunchComplication(
        launch: CachedLaunch,
        type: ComplicationType,
    ): ComplicationData? {
        val countdownShort = formatCountdownShort(launch)
        val countdownLong = formatCountdown(launch)
        val vehicleName = formatVehicleName(launch)
        val agencyTitle = formatAgencyTitle(launch)
        val contentDescription = PlainComplicationText.Builder(
            "Next launch: $vehicleName in $countdownLong"
        ).build()
        val tapAction = launchDetailPendingIntent(launch.id)

        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(countdownShort).build(),
                contentDescription = contentDescription,
            )
                .setTitle(PlainComplicationText.Builder(agencyTitle).build())
                .setTapAction(tapAction)
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder("$vehicleName — $countdownLong").build(),
                contentDescription = contentDescription,
            )
                .setTapAction(tapAction)
                .build()

            ComplicationType.RANGED_VALUE -> {
                val progress = calculateProgress(launch)
                RangedValueComplicationData.Builder(
                    value = progress,
                    min = 0f,
                    max = 1f,
                    contentDescription = contentDescription,
                )
                    .setText(PlainComplicationText.Builder(countdownShort).build())
                    .setTitle(PlainComplicationText.Builder(agencyTitle).build())
                    .setTapAction(tapAction)
                    .build()
            }

            else -> null
        }
    }

    /** PendingIntent that opens WearActivity and deep-links to the given launch's detail screen. */
    private fun launchDetailPendingIntent(launchId: String): PendingIntent {
        val intent = Intent(this, WearActivity::class.java).apply {
            putExtra(WearActivity.EXTRA_LAUNCH_ID, launchId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            launchId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildFreeUserData(type: ComplicationType): ComplicationData? {
        val subscribeText = PlainComplicationText.Builder("Subscribe").build()
        val contentDescription = PlainComplicationText.Builder("Subscribe to see launch countdown").build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = subscribeText,
                contentDescription = contentDescription,
            ).build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = subscribeText,
                contentDescription = contentDescription,
            ).build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 0f,
                min = 0f,
                max = 1f,
                contentDescription = contentDescription,
            )
                .setText(subscribeText)
                .build()

            else -> null
        }
    }

    private fun buildNoDataComplication(type: ComplicationType): ComplicationData? {
        val noLaunchText = PlainComplicationText.Builder("No launches").build()
        val contentDescription = PlainComplicationText.Builder("No upcoming launches").build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = noLaunchText,
                contentDescription = contentDescription,
            ).build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = noLaunchText,
                contentDescription = contentDescription,
            ).build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 0f,
                min = 0f,
                max = 1f,
                contentDescription = contentDescription,
            )
                .setText(noLaunchText)
                .build()

            else -> null
        }
    }

    /** Full countdown with T- prefix for long text and tile display. e.g. "T-1D 13H", "T-4H 22M" */
    private fun formatCountdown(launch: CachedLaunch): String {
        val now = Clock.System.now()
        val duration = launch.net - now
        if (duration.isNegative()) return "Launched"

        val totalMinutes = duration.inWholeMinutes
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60

        return when {
            hours >= 24 -> "T-${hours / 24}D ${hours % 24}H"
            hours > 0   -> "T-${hours}H ${mins}M"
            mins > 0    -> "T-${mins}M"
            else        -> "T-0"
        }
    }

    /**
     * Compact countdown WITHOUT the "T-" prefix for SHORT_TEXT complication slots.
     * The watch face renders "T-" as the title line and this as the value line.
     * Max ~7 chars: "1D 13H", "4H 22M", "45M"
     */
    private fun formatCountdownShort(launch: CachedLaunch): String {
        val now = Clock.System.now()
        val duration = launch.net - now
        if (duration.isNegative()) return "Done"

        val totalMinutes = duration.inWholeMinutes
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60

        return when {
            hours >= 24 -> "${hours / 24}D ${hours % 24}H"
            hours > 0   -> "${hours}H ${mins}M"
            mins > 0    -> "${mins}M"
            else        -> "Now"
        }
    }

    /**
     * Returns the agency abbreviation for use as the SHORT_TEXT/RANGED_VALUE complication title.
     * The title field is small (~7 chars max), so we prefer the abbreviation.
     * Falls back to a truncated lspName, then "T-" if nothing is available.
     */
    private fun formatAgencyTitle(launch: CachedLaunch): String {
        if (!launch.lspAbbrev.isNullOrBlank()) return launch.lspAbbrev
        if (!launch.lspName.isNullOrBlank()) return launch.lspName.take(6)
        return "T-"
    }

    private fun formatVehicleName(launch: CachedLaunch): String {
        val lspDisplay = launch.lspName?.let { name ->
            if (name.length > 15 && !launch.lspAbbrev.isNullOrEmpty()) {
                launch.lspAbbrev
            } else {
                name
            }
        }
        return when {
            lspDisplay != null && launch.rocketConfigName != null ->
                "$lspDisplay | ${launch.rocketConfigName}"
            launch.rocketConfigName != null -> launch.rocketConfigName
            else -> launch.name
        }
    }

    companion object {
        private const val REQUEST_CODE_ALARM = 1001
        // Baseline update period (30 min) is declared in AndroidManifest.xml via
        // android.support.wearable.complications.UPDATE_PERIOD_SECONDS = 1800.
        // When countdown < 1 hour, AlarmManager provides 5-minute rapid refresh.
    }

    private fun calculateProgress(launch: CachedLaunch): Float {
        val now = Clock.System.now()
        val windowStart = launch.net - 24.hours
        val totalDuration = 24.hours
        val elapsed = now - windowStart

        return when {
            elapsed.isNegative() -> 0f
            elapsed > totalDuration -> 1f
            else -> (elapsed.inWholeMinutes.toFloat() / totalDuration.inWholeMinutes.toFloat()).coerceIn(0f, 1f)
        }
    }
}
