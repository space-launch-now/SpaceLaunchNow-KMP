//
//  NotificationService.swift
//  NotificationServiceExtension
//
//  Created by Caleb Jones on 1/28/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import UserNotifications
import os

/// os.Logger for the Notification Service Extension. The NSE runs in a separate, sandboxed
/// process and cannot reach the app's Kermit/Datadog sink; print() is invisible in the field.
/// os_log IS capturable via Console.app / `log stream` / sysdiagnose, so this is the only way
/// to observe killed-app delivery and suppression decisions on a real device.
private enum NSELog {
    private static let subsystem = "me.spacelaunchnow.nse"
    static let receipt = Logger(subsystem: subsystem, category: "receipt")
    static let filter = Logger(subsystem: subsystem, category: "filter")
}

/// Cross-process breadcrumb buffer the NSE writes to and the main app drains into Datadog.
///
/// os_log (NSELog) is capturable on a tethered device but never reaches the backend. To get
/// killed-app delivery/suppression into Datadog, the NSE appends a compact breadcrumb to the
/// shared App Group UserDefaults (same suite as the filter bridge); the main app drains and
/// emits them on next foreground (see IosNotificationBridge.drainNseEventLog on the Kotlin side).
///
/// Entries are stored as pipe-delimited strings "ts|type|decision|reason" in a BOUNDED ring
/// buffer (oldest dropped past the cap) so it cannot grow without bound if the app is rarely
/// opened. Cross-process writes are last-write-wins; acceptable for diagnostics.
private enum NSEBreadcrumb {
    static let appGroup = "group.me.spacelaunchnow.spacelaunchnow"
    static let key = "nse_event_log"
    static let maxEntries = 50

    /// Append a breadcrumb for a delivery decision. `decision` is "shown" or "suppressed".
    static func append(notificationType: String, decision: String, reason: String) {
        guard let defaults = UserDefaults(suiteName: appGroup) else { return }
        let ts = Int(Date().timeIntervalSince1970)
        // Sanitize separators so the pipe-delimited format can't be corrupted by field content.
        func clean(_ s: String) -> String {
            s.replacingOccurrences(of: "|", with: "/").replacingOccurrences(of: "\n", with: " ")
        }
        let entry = "\(ts)|\(clean(notificationType))|\(clean(decision))|\(clean(reason))"

        var log = defaults.stringArray(forKey: key) ?? []
        log.append(entry)
        if log.count > maxEntries {
            log = Array(log.suffix(maxEntries))
        }
        defaults.set(log, forKey: key)
    }
}

/// Notification Service Extension entry point.
///
/// For every mutable-content notification received (including when the app is killed/terminated)
/// this extension:
///  1. Parses the payload as V5NotificationData.
///  2. Loads filter preferences from shared App Group UserDefaults (written by NSEPreferenceBridge).
///  3. Applies NSENotificationFilter to decide allow/block.
///  4. If blocked: delivers empty content so iOS suppresses visual display.
///  5. If allowed: enriches content with server-provided title/body and downloads the launch image.
///
/// Non-V5 payloads only go through the enableNotifications kill switch.
class NotificationService: UNNotificationServiceExtension {

    var contentHandler: ((UNNotificationContent) -> Void)?
    var bestAttemptContent: UNMutableNotificationContent?

    override func didReceive(
        _ request: UNNotificationRequest,
        withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void
    ) {
        self.contentHandler = contentHandler
        bestAttemptContent = (request.content.mutableCopy() as? UNMutableNotificationContent)

        guard let bestAttemptContent = bestAttemptContent else {
            contentHandler(request.content)
            return
        }

        let userInfo = request.content.userInfo
        let preferences = NSEFilterPreferences.load()

        // Log receipt with detected markers (privacy: types/markers only, no user content).
        let notificationType = (userInfo["notification_type"] as? String) ?? "unknown"
        let isV5 = V5NotificationData.isV5Payload(userInfo)
        NSELog.receipt.log(
            "NSE received: type=\(notificationType, privacy: .public) isV5=\(isV5, privacy: .public) hasEventId=\(userInfo["event_id"] != nil, privacy: .public) hasArticleId=\(userInfo["article_id"] != nil, privacy: .public)"
        )

        // --- V5 notifications ---
        if isV5,
           let v5Data = V5NotificationData.fromUserInfo(userInfo) {

            // Apply NSE filter — only reliable code path when app is killed.
            guard NSENotificationFilter.shouldShow(payload: v5Data, preferences: preferences) else {
                NSELog.filter.log("NSE suppressed V5 launch: type=\(notificationType, privacy: .public) reason=launch_filter")
                deliverEmptyNotification(notificationType: notificationType, reason: "v5_launch_filter", contentHandler: contentHandler)
                return
            }

            NSELog.filter.log("NSE allowed V5 launch: type=\(notificationType, privacy: .public)")
            NSEBreadcrumb.append(notificationType: notificationType, decision: "shown", reason: "v5_launch_allowed")

            // Enrich with server-provided title and body.
            bestAttemptContent.title = v5Data.title
            bestAttemptContent.body = v5Data.body
            NotificationAlertPolicy.applySound(to: bestAttemptContent, notificationType: notificationType)

            // Download and attach launch image if available.
            if let imageUrlString = v5Data.launchImage,
               let imageUrl = URL(string: imageUrlString) {
                downloadAndAttachImage(url: imageUrl, to: bestAttemptContent, contentHandler: contentHandler)
                return
            }

        } else {
            // Non-V5 payload (event / news / custom): apply kill switch first.
            guard preferences.enableNotifications else {
                NSELog.filter.log("NSE suppressed broadcast: type=\(notificationType, privacy: .public) reason=kill_switch")
                deliverEmptyNotification(notificationType: notificationType, reason: "kill_switch", contentHandler: contentHandler)
                return
            }

            // Apply the relevant broadcast-type per-type toggle. These types are NOT
            // agency/location filtered — each is gated only by its own toggle.
            if !isBroadcastTypeAllowed(userInfo: userInfo, preferences: preferences) {
                NSELog.filter.log("NSE suppressed broadcast: type=\(notificationType, privacy: .public) reason=per_type_toggle_off")
                deliverEmptyNotification(notificationType: notificationType, reason: "per_type_toggle_off", contentHandler: contentHandler)
                return
            }

            NSELog.filter.log("NSE allowed broadcast: type=\(notificationType, privacy: .public)")
            NSEBreadcrumb.append(notificationType: notificationType, decision: "shown", reason: "broadcast_allowed")
        }

        contentHandler(bestAttemptContent)
    }

    override func serviceExtensionTimeWillExpire() {
        if let contentHandler = contentHandler,
           let bestAttemptContent = bestAttemptContent {
            contentHandler(bestAttemptContent)
        }
    }

    // MARK: - Private

    /// Decide whether a non-V5 broadcast-type notification (event / news / custom) is allowed
    /// by its per-type toggle. Mirrors the Kotlin worker's per-type filtering for killed-app
    /// delivery. Detection order matches the worker: custom → event → news.
    ///
    /// - `notification_type == "custom"` → ANNOUNCEMENTS toggle
    /// - `event_id` present              → EVENTS toggle
    /// - `article_id` present            → FEATURED_NEWS toggle
    /// - anything else                   → allowed (no per-type toggle governs it)
    private func isBroadcastTypeAllowed(
        userInfo: [AnyHashable: Any],
        preferences: NSEFilterPreferences
    ) -> Bool {
        let notificationType = userInfo["notification_type"] as? String

        if notificationType == "custom" {
            return preferences.topicAnnouncements
        }
        if userInfo["event_id"] != nil {
            return preferences.topicEvents
        }
        if userInfo["article_id"] != nil {
            return preferences.topicFeaturedNews
        }
        return true
    }

    /// Deliver empty content to suppress notification display.
    /// iOS will not show a notification that has no title, no body, and no sound.
    /// `reason` is logged (os_log) and breadcrumbed (App Group) so killed-app suppression is
    /// observable both on-device and, after drain, in Datadog.
    private func deliverEmptyNotification(
        notificationType: String,
        reason: String,
        contentHandler: (UNNotificationContent) -> Void
    ) {
        NSELog.filter.log("NSE delivering EMPTY (suppressed): type=\(notificationType, privacy: .public) reason=\(reason, privacy: .public)")
        NSEBreadcrumb.append(notificationType: notificationType, decision: "suppressed", reason: reason)
        let empty = UNMutableNotificationContent()
        empty.sound = nil
        contentHandler(empty)
    }

    private func downloadAndAttachImage(
        url: URL,
        to content: UNMutableNotificationContent,
        contentHandler: @escaping (UNNotificationContent) -> Void
    ) {
        let task = URLSession.shared.downloadTask(with: url) { localUrl, _, error in
            defer {
                contentHandler(content)
            }

            guard let localUrl = localUrl, error == nil else {
                return
            }

            do {
                let attachment = try UNNotificationAttachment(
                    identifier: "launch-image",
                    url: localUrl,
                    options: nil
                )
                content.attachments = [attachment]
            } catch {
                // Image attachment failed — notification still delivered without image.
            }
        }

        task.resume()
    }
}

