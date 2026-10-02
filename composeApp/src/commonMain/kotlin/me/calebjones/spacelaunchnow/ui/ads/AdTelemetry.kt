package me.calebjones.spacelaunchnow.ui.ads

import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent.AdResult
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.mp.KoinPlatform

/**
 * Reports ad lifecycle callbacks (load, failure, impression, click) as analytics events.
 *
 * Observation only. Failures log at warn so they reach Datadog from release builds;
 * everything else logs at debug.
 */
internal object AdTelemetry {
    private val log by lazy { SpaceLogger.getLogger("AdTelemetry") }
    private val codeRegex = Regex("""(?i)code\D{0,3}(\d+)""")
    private val leadingNumber = Regex("""^\s*(\d+)\b""")

    /** Pulls the numeric AdMob error code out of an exception message, else "unknown". */
    fun parseErrorCode(message: String?): String {
        if (message == null) return "unknown"
        return codeRegex.find(message)?.groupValues?.get(1)
            ?: leadingNumber.find(message)?.groupValues?.get(1)
            ?: "unknown"
    }

    /** "320x50" style label; negative dimensions (fluid, full width) read as "fluid". */
    fun sizeLabel(width: Int, height: Int): String =
        if (width < 0 || height < 0) "fluid" else "${width}x$height"

    fun loaded(format: String, size: String? = null, handler: String? = null) {
        log.d { "ad_loaded format=$format size=$size handler=$handler" }
        send(AnalyticsEvent.AdLifecycle(AdResult.LOADED, format, size, handler))
    }

    fun failed(format: String, error: Throwable?, size: String? = null, handler: String? = null) {
        val message = error?.message
        val code = parseErrorCode(message)
        log.w { "ad_failed format=$format size=$size handler=$handler code=$code message=$message" }
        send(AnalyticsEvent.AdLifecycle(AdResult.FAILED, format, size, handler, code, message ?: "unknown"))
    }

    fun impression(format: String, size: String? = null, handler: String? = null) {
        log.d { "ad_impression format=$format size=$size handler=$handler" }
        send(AnalyticsEvent.AdLifecycle(AdResult.IMPRESSION, format, size, handler))
    }

    fun clicked(format: String, size: String? = null, handler: String? = null) {
        log.d { "ad_clicked format=$format size=$size handler=$handler" }
        send(AnalyticsEvent.AdLifecycle(AdResult.CLICKED, format, size, handler))
    }

    fun trackingStatus(status: String) {
        log.i { "ad_att_status status=$status" }
        send(AnalyticsEvent.AdTrackingStatus(status))
    }

    private fun send(event: AnalyticsEvent) {
        try {
            KoinPlatform.getKoin().get<AnalyticsManager>().track(event)
        } catch (e: Exception) {
            log.d { "Ad telemetry skipped: ${e.message}" }
        }
    }
}
