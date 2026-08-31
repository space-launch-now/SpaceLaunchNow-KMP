package me.calebjones.spacelaunchnow.util

import androidx.compose.ui.platform.UriHandler
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import kotlin.coroutines.cancellation.CancellationException

private val log by lazy { SpaceLogger.getLogger("UriHandlerExt") }

/**
 * Opens [url] through the Compose [UriHandler] without letting a platform failure crash the app.
 *
 * Compose's `AndroidUriHandler` calls `startActivity` on whatever `LocalContext` resolves to. When
 * that is not an Activity - for example while a detail screen is being torn down - the platform
 * throws `AndroidRuntimeException: Calling startActivity() from outside of an Activity context
 * requires the FLAG_ACTIVITY_NEW_TASK flag.`
 *
 * We fall back to [ExternalLinkHandler], which opens from the application context with
 * `FLAG_ACTIVITY_NEW_TASK` on Android, so the tap still does what the user asked. If even that
 * fails the link is dropped and logged rather than crashing.
 *
 * Migrating every `openUri` call site to [ExternalLinkHandler] is the durable fix and is tracked
 * as a follow-up; this extension guards the video link sinks in the meantime.
 */
fun UriHandler.openUriSafely(url: String) {
    try {
        openUri(url)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (e: Exception) {
        log.w(e) { "UriHandler could not open $url, falling back to ExternalLinkHandler" }
        try {
            ExternalLinkHandler.openUrl(url)
        } catch (fallbackFailure: Exception) {
            log.e(fallbackFailure) { "Unable to open $url" }
        }
    }
}
