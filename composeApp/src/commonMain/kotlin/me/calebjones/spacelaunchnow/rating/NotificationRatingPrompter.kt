package me.calebjones.spacelaunchnow.rating

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent

/** Called by the launch detail view model each time a launch loads. */
interface NotificationRatingTrigger {
    suspend fun onLaunchLoaded(launchId: String, statusId: Int?)
}

/**
 * Asks for the system review prompt after the user opens a launch from a notification.
 * Holds the pending notification launch id; the first evaluation clears it, so one
 * notification gives at most one prompt. The UI collects [promptRequest] and calls
 * AppRatingManager, then [consume].
 */
class NotificationRatingPrompter(
    private val launchCount: suspend () -> Long,
    private val lastPromptAtMs: suspend () -> Long?,
    private val hasRated: suspend () -> Boolean,
    private val saveLastPromptAtMs: suspend (Long) -> Unit,
    private val flagEnabled: suspend () -> Boolean,
    private val nowMs: () -> Long,
    private val analytics: AnalyticsManager,
) : NotificationRatingTrigger {

    private var pendingLaunchId: String? = null
    private var pendingSetAtMs: Long = 0L

    private val _promptRequest = MutableStateFlow(false)
    val promptRequest: StateFlow<Boolean> = _promptRequest.asStateFlow()

    fun onOpenedFromNotification(launchId: String) {
        pendingLaunchId = launchId
        pendingSetAtMs = nowMs()
    }

    fun consume() {
        _promptRequest.value = false
    }

    override suspend fun onLaunchLoaded(launchId: String, statusId: Int?) {
        // Another launch (browsing) must not use up the pending notification.
        if (pendingLaunchId != launchId) return
        pendingLaunchId = null

        val now = nowMs()
        // The launch never loaded in time (for example a failed fetch): ignore the stale id.
        if (now - pendingSetAtMs > PENDING_TTL_MS) return
        val decision = NotificationRatingRule.evaluate(
            NotificationRatingRule.Input(
                launchCount = launchCount(),
                lastPromptAtMs = lastPromptAtMs(),
                nowMs = now,
                hasRated = hasRated(),
                statusId = statusId,
                flagEnabled = flagEnabled(),
            )
        )
        if (decision != NotificationRatingRule.Decision.SHOW) return

        saveLastPromptAtMs(now)
        analytics.track(AnalyticsEvent.RatingPromptRequested(SOURCE_NOTIFICATION, launchId))
        _promptRequest.value = true
    }

    companion object {
        const val SOURCE_NOTIFICATION = "notification"
        const val PENDING_TTL_MS = 2 * 60 * 1000L
    }
}
