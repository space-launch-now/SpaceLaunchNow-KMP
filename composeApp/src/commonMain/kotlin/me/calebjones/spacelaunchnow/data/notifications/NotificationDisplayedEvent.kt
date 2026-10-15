package me.calebjones.spacelaunchnow.data.notifications

import co.touchlab.kermit.Severity
import me.calebjones.spacelaunchnow.util.logging.DiagnosticEventKind
import me.calebjones.spacelaunchnow.util.logging.DiagnosticEvents

/**
 * Emits the single `notification_displayed` event after Android posts a launch notification
 * (client-log-quality spec 4.2, N14). Replaces the old multi-line `[V5 Notification]` trace.
 * Carries the type, launch id and flags only: never the title, body or launch name.
 */
object NotificationDisplayedEvent {

    fun attributes(
        notificationType: String,
        launchId: String,
        reason: String,
        channel: String,
        hasImage: Boolean,
    ): Map<String, Any?> = mapOf(
        "notification_type" to notificationType,
        "launch_id" to launchId,
        "reason" to reason,
        "channel" to channel,
        "has_image" to hasImage,
    )

    fun emit(
        notificationType: String,
        launchId: String,
        reason: String,
        channel: String,
        hasImage: Boolean,
        tag: String = "SLN-NotificationDisplay",
    ) {
        DiagnosticEvents.emit(
            kind = DiagnosticEventKind.NOTIFICATION_DISPLAYED,
            severity = Severity.Info,
            tag = tag,
            attrs = attributes(notificationType, launchId, reason, channel, hasImage),
        )
    }
}
