package me.calebjones.spacelaunchnow.ui.ads

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import platform.AppTrackingTransparency.ATTrackingManager
import platform.AppTrackingTransparency.ATTrackingManagerAuthorizationStatusNotDetermined
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationStateActive
import kotlin.coroutines.resume

private val log by lazy { SpaceLogger.getLogger("AppTracking") }

/** Longest wait for the ATT prompt path before ads load anyway. */
private const val ATT_TIMEOUT_MS = 10_000L

/**
 * App Tracking Transparency request. Without it the IDFA is always zero, which caps iOS eCPM.
 * Call on the main thread, after the UMP form and before ads preload.
 */
object AppTracking {
    private var requested = false

    /**
     * Shows the ATT prompt once per process. Returns when the user answers, when the status
     * is already decided, or after [ATT_TIMEOUT_MS]. Never throws: a failure must not block ads.
     */
    suspend fun requestIfNeeded() {
        if (requested) return
        requested = true
        try {
            if (ATTrackingManager.trackingAuthorizationStatus != ATTrackingManagerAuthorizationStatusNotDetermined) {
                log.d { "ATT already decided: ${ATTrackingManager.trackingAuthorizationStatus}" }
                return
            }
            val answered = withTimeoutOrNull(ATT_TIMEOUT_MS) {
                awaitActive()
                suspendCancellableCoroutine<Unit> { cont ->
                    ATTrackingManager.requestTrackingAuthorizationWithCompletionHandler { status ->
                        // TODO(ads-analytics): track the A1 ad-consent event with this status during integration.
                        log.i { "ATT authorization status: $status" }
                        if (cont.isActive) cont.resume(Unit)
                    }
                }
            }
            if (answered == null) log.w { "ATT prompt not answered within ${ATT_TIMEOUT_MS}ms, loading ads anyway" }
        } catch (e: Exception) {
            log.w(e) { "ATT request failed: ${e.message}" }
        }
    }

    /** ATT only prompts while the app is active; wait for the next activation otherwise. */
    private suspend fun awaitActive() {
        if (UIApplication.sharedApplication.applicationState == UIApplicationStateActive) return
        suspendCancellableCoroutine<Unit> { cont ->
            val center = NSNotificationCenter.defaultCenter
            var observer: Any? = null
            observer = center.addObserverForName(
                name = UIApplicationDidBecomeActiveNotification,
                `object` = null,
                queue = NSOperationQueue.mainQueue,
            ) { _ ->
                observer?.let { center.removeObserver(it) }
                if (cont.isActive) cont.resume(Unit)
            }
            cont.invokeOnCancellation { observer?.let { center.removeObserver(it) } }
        }
    }
}
