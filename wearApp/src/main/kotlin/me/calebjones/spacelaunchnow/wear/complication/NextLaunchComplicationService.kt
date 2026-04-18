package me.calebjones.spacelaunchnow.wear.complication

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
import me.calebjones.spacelaunchnow.wear.data.EntitlementSyncManager
import me.calebjones.spacelaunchnow.wear.data.WatchLaunchRepository
import me.calebjones.spacelaunchnow.wear.data.model.CachedLaunch
import org.koin.android.ext.android.inject
import kotlin.time.Clock
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
                    return@launch
                }

                val nextLaunch = watchLaunchRepository.getNextLaunch()
                if (nextLaunch == null) {
                    listener.onComplicationData(buildNoDataComplication(request.complicationType))
                    return@launch
                }

                val complicationData = buildLaunchComplication(nextLaunch, request.complicationType)
                listener.onComplicationData(complicationData)
            } catch (e: Exception) {
                log.e(e) { "Failed to build complication data" }
                listener.onComplicationData(buildNoDataComplication(request.complicationType))
            }
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        val countdownText = "T-2h 15m"
        val vehicleText = "Falcon 9"
        val contentDescription = PlainComplicationText.Builder("Next launch: $vehicleText in $countdownText").build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(countdownText).build(),
                contentDescription = contentDescription,
            ).build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder("$vehicleText — $countdownText").build(),
                contentDescription = contentDescription,
            ).build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = 0.5f,
                min = 0f,
                max = 1f,
                contentDescription = contentDescription,
            )
                .setText(PlainComplicationText.Builder(countdownText).build())
                .build()

            else -> null
        }
    }

    private fun buildLaunchComplication(
        launch: CachedLaunch,
        type: ComplicationType,
    ): ComplicationData? {
        val countdownText = formatCountdown(launch)
        val vehicleName = formatVehicleName(launch)
        val contentDescription = PlainComplicationText.Builder(
            "Next launch: $vehicleName in $countdownText"
        ).build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = PlainComplicationText.Builder(countdownText).build(),
                contentDescription = contentDescription,
            ).build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = PlainComplicationText.Builder("$vehicleName — $countdownText").build(),
                contentDescription = contentDescription,
            ).build()

            ComplicationType.RANGED_VALUE -> {
                val progress = calculateProgress(launch)
                RangedValueComplicationData.Builder(
                    value = progress,
                    min = 0f,
                    max = 1f,
                    contentDescription = contentDescription,
                )
                    .setText(PlainComplicationText.Builder(countdownText).build())
                    .build()
            }

            else -> null
        }
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

    private fun formatCountdown(launch: CachedLaunch): String {
        val now = Clock.System.now()
        val duration = launch.net - now
        if (duration.isNegative()) return "Launched"

        val totalMinutes = duration.inWholeMinutes
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours >= 24 -> "T-${hours / 24}d ${hours % 24}h"
            hours > 0 -> "T-${hours}h ${minutes}m"
            minutes > 0 -> "T-${minutes}m"
            else -> "T-0"
        }
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
