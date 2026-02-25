//
//  NotificationService.swift
//  NotificationServiceExtension
//
//  Created by Caleb Jones on 1/28/26.
//  Copyright © 2026 orgName. All rights reserved.
//

import UserNotifications

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

        // --- V5 notifications ---
        if V5NotificationData.isV5Payload(userInfo),
           let v5Data = V5NotificationData.fromUserInfo(userInfo) {

            // Apply NSE filter — only reliable code path when app is killed.
            guard NSENotificationFilter.shouldShow(payload: v5Data, preferences: preferences) else {
                deliverEmptyNotification(contentHandler: contentHandler)
                return
            }

            // Enrich with server-provided title and body.
            bestAttemptContent.title = v5Data.title
            bestAttemptContent.body = v5Data.body
            bestAttemptContent.sound = .default

            // Download and attach launch image if available.
            if let imageUrlString = v5Data.launchImage,
               let imageUrl = URL(string: imageUrlString) {
                downloadAndAttachImage(url: imageUrl, to: bestAttemptContent, contentHandler: contentHandler)
                return
            }

        } else {
            // Non-V5 payload: apply kill switch only.
            guard preferences.enableNotifications else {
                deliverEmptyNotification(contentHandler: contentHandler)
                return
            }
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

    /// Deliver empty content to suppress notification display.
    /// iOS will not show a notification that has no title, no body, and no sound.
    private func deliverEmptyNotification(contentHandler: (UNNotificationContent) -> Void) {
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

