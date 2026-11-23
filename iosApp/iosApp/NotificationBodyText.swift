//
//  NotificationBodyText.swift
//  iosApp
//
//  Body text the app builds itself for a launch push (foreground display and test
//  notifications). Text comes from NotificationText.xcstrings, so it follows the device
//  language. A push that reaches the Notification Service Extension shows the server's
//  title and body and never uses this file.
//

import Foundation

enum NotificationBodyText {

    /// Body for a launch push. A known `notification_type` uses a catalog string; an unknown
    /// type shows the "Launch from" text. (`AppDelegate` shows the server's body when the push
    /// carries one, so this text is the fallback.)
    static func body(
        notificationType: String,
        launchNet: String,
        launchLocation: String
    ) -> String {
        switch notificationType.lowercased() {
        case "netstampchanged":
            return text("notification_schedule_changed", "SCHEDULE UPDATE: Next attempt no earlier than %@", formatLaunchDate(launchNet))
        case "success":
            return text("notification_launch_successful", "The launch was successful!")
        case "failure":
            return text("notification_launch_failure", "A launch failure has occurred.")
        case "partialfailure":
            return text("notification_launch_partial_failure", "The launch was a partial failure.")
        case "inflight":
            return text("notification_launch_liftoff", "Liftoff! Launch vehicle is now in flight!")
        case "oneminute":
            return text("notification_one_minute", "Launch attempt in less than one minute at %@", formatLaunchDate(launchNet))
        case "tenminutes":
            return text("notification_ten_minutes", "Launch attempt in less than ten minutes at %@", formatLaunchDate(launchNet))
        case "onehour":
            return text("notification_one_hour", "Launch attempt in less than one hour at %@", formatLaunchDate(launchNet))
        case "twentyfourhour":
            return text("notification_twenty_four_hours", "Launch attempt in less than 24 hours at %@", formatLaunchDate(launchNet))
        default:
            return text("notification_fallback_launch_from", "Launch from %@", launchLocation)
        }
    }

    /// Look up `key` in NotificationText.xcstrings; `english` is the fallback and the master text.
    private static func text(_ key: String, _ english: String, _ args: CVarArg...) -> String {
        let format = NSLocalizedString(key, tableName: "NotificationText", bundle: .main, value: english, comment: "")
        return args.isEmpty ? format : String(format: format, arguments: args)
    }

    /// Format the launch time for the user: "2025-10-15T12:00:00Z" becomes "12:00 PM" in en-US
    /// and the local style (12 or 24 hour) in other languages.
    static func formatLaunchDate(_ launchNet: String) -> String {
        let inputFormatter = ISO8601DateFormatter()
        inputFormatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]

        // Try with fractional seconds first, then without
        guard
            let date = inputFormatter.date(from: launchNet)
                ?? {
                    inputFormatter.formatOptions = [.withInternetDateTime]
                    return inputFormatter.date(from: launchNet)
                }()
        else {
            print("⚠️ Failed to parse launch date: \(launchNet)")
            return launchNet  // Return original if parsing fails
        }

        let outputFormatter = DateFormatter()
        outputFormatter.locale = Locale.current
        outputFormatter.setLocalizedDateFormatFromTemplate("jmm")
        outputFormatter.timeZone = TimeZone.current
        return outputFormatter.string(from: date)
    }
}

extension NotificationData {
    /// Body text for this launch push; see `NotificationBodyText.body`.
    func displayBody() -> String {
        NotificationBodyText.body(
            notificationType: notificationType,
            launchNet: launchNet,
            launchLocation: launchLocation
        )
    }
}
