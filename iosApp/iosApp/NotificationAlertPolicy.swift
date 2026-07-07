import UserNotifications

/// Single source of truth for whether a notification phase re-alerts (sound + prominence) or
/// updates silently. Used by both the app target and the Notification Service Extension, so it
/// MUST be a member of both targets (see V5NotificationData.swift for the same dual-target setup).
///
/// Collapse itself is driven server-side by `apns-collapse-id`; this only decides whether the
/// collapsing replacement plays a sound.
enum NotificationAlertPolicy {

    /// Launch phases that update silently when they replace an existing notification.
    /// Everything not in this set re-alerts (high-value launch phases AND all broadcasts/unknown).
    private static let silentTypes: Set<String> = [
        "twentyfourhour",
        "onehour",
        "tenminutes",
        "netstampchanged",
        "failure",
        "partialfailure",
    ]

    /// True if this notification type should play sound and use default prominence.
    static func shouldReAlert(notificationType: String?) -> Bool {
        guard let type = notificationType?.lowercased() else { return true }
        return !silentTypes.contains(type)
    }

    /// Apply the policy to a mutable notification content (sets sound + interruption level).
    static func applySound(to content: UNMutableNotificationContent, notificationType: String?) {
        if shouldReAlert(notificationType: notificationType) {
            content.sound = .default
            if #available(iOS 15.0, *) { content.interruptionLevel = .active }
        } else {
            content.sound = nil
            if #available(iOS 15.0, *) { content.interruptionLevel = .passive }
        }
    }

    /// Presentation options for a foreground launch notification, honoring the policy.
    static func foregroundOptions(notificationType: String?) -> UNNotificationPresentationOptions {
        var options: UNNotificationPresentationOptions = [.banner, .badge]
        if shouldReAlert(notificationType: notificationType) { options.insert(.sound) }
        return options
    }
}
