import Foundation
import UserNotifications

/// Helper class for triggering test notifications from debug menu
/// Mirrors the notification display logic in AppDelegate
@objc public class NotificationTestHelper: NSObject {
    
    @objc public static let shared = NotificationTestHelper()
    
    private override init() {
        super.init()
    }
    
    /// Display a test notification using the same logic as real FCM notifications
    @objc public func displayTestNotification(
        notificationType: String,
        launchId: String,
        launchUuid: String,
        launchName: String,
        launchImage: String?,
        launchNet: String,
        launchLocation: String,
        webcast: String,
        webcastLive: String?,
        agencyId: String,
        locationId: String
    ) {
        print("📱 [TestNotification] Triggering test notification...")
        print("📱 [TestNotification] Type: \(notificationType)")
        print("📱 [TestNotification] Launch: \(launchName)")
        
        let notificationData = NotificationData(
            notificationType: notificationType,
            launchId: launchId,
            launchUuid: launchUuid,
            launchName: launchName,
            launchImage: launchImage,
            launchNet: launchNet,
            launchLocation: launchLocation,
            webcast: webcast,
            webcastLive: webcastLive,
            agencyId: agencyId,
            locationId: locationId
        )
        
        let content = UNMutableNotificationContent()
        
        // Format title - add 🔴 emoji if webcast is live (matches Android)
        let baseTitle = notificationData.launchName
        let displayTitle = notificationData.isWebcastLive() ? "🔴 \(baseTitle)" : baseTitle
        
        // Format body using same logic as AppDelegate
        let displayBody = getNotificationBody(data: notificationData)
        
        content.title = "Test: \(displayTitle)"
        content.body = displayBody
        content.badge = 1
        content.sound = .default
        
        // Add custom data for handling taps
        content.userInfo = [
            "launch_id": launchUuid,
            "launch_uuid": launchUuid,
            "launch_name": launchName,
            "notification_type": notificationType,
            "is_test": "true"
        ]
        
        print("📱 [TestNotification] Title: \(content.title)")
        print("📱 [TestNotification] Body: \(content.body)")
        print("📱 [TestNotification] Image URL: \(launchImage ?? "nil")")
        
        // Try to add image if available
        if let imageUrlString = launchImage,
           let imageUrl = URL(string: imageUrlString) {
            downloadImage(from: imageUrl) { attachment in
                if let attachment = attachment {
                    content.attachments = [attachment]
                    print("📱 [TestNotification] ✅ Image attached")
                } else {
                    print("📱 [TestNotification] ⚠️ Failed to attach image")
                }
                self.scheduleNotification(content: content, identifier: "test-\(launchUuid)")
            }
        } else {
            print("📱 [TestNotification] No image URL, showing text-only notification")
            scheduleNotification(content: content, identifier: "test-\(launchUuid)")
        }
    }
    
    /// Get notification body message based on notification type (matches Android strings)
    private func getNotificationBody(data: NotificationData) -> String {
        let formattedDate = formatLaunchDate(data.launchNet)
        
        switch data.notificationType.lowercased() {
        case "netstampchanged":
            return "SCHEDULE UPDATE: Next attempt no earlier than \(formattedDate)"
        case "success":
            return "The launch was successful!"
        case "failure":
            return "A launch failure has occurred."
        case "partialfailure":
            return "The launch was a partial failure."
        case "inflight":
            return "Liftoff! Launch vehicle is now in flight!"
        case "oneminute":
            return "Launch attempt in less than one minute at \(formattedDate)"
        case "tenminutes":
            return "Launch attempt in less than ten minutes at \(formattedDate)"
        case "onehour":
            return "Launch attempt in less than one hour at \(formattedDate)"
        case "twentyfourhour":
            return "Launch attempt in less than 24 hours at \(formattedDate)"
        default:
            // Fallback to location
            return "Launch from \(data.launchLocation)"
        }
    }
    
    /// Format launch date to readable time (matches Android)
    /// Input: "2025-10-15T12:00:00Z" -> Output: "12:00 PM"
    private func formatLaunchDate(_ launchNet: String) -> String {
        let inputFormatter = ISO8601DateFormatter()
        inputFormatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        
        // Try with fractional seconds first, then without
        guard let date = inputFormatter.date(from: launchNet) ?? {
            inputFormatter.formatOptions = [.withInternetDateTime]
            return inputFormatter.date(from: launchNet)
        }() else {
            print("⚠️ Failed to parse launch date: \(launchNet)")
            return launchNet // Return original if parsing fails
        }
        
        // Format to user's local time
        let outputFormatter = DateFormatter()
        outputFormatter.dateFormat = "h:mm a"
        outputFormatter.timeZone = TimeZone.current
        return outputFormatter.string(from: date)
    }
    
    /// Schedule local notification
    private func scheduleNotification(content: UNNotificationContent, identifier: String) {
        let request = UNNotificationRequest(
            identifier: identifier,
            content: content,
            trigger: nil // Show immediately
        )
        
        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("❌ Failed to display test notification: \(error)")
            } else {
                print("✅ Test notification displayed successfully")
            }
        }
    }
    
    /// Download image for notification attachment
    private func downloadImage(from url: URL, completion: @escaping (UNNotificationAttachment?) -> Void) {
        let task = URLSession.shared.dataTask(with: url) { data, response, error in
            guard let data = data, error == nil else {
                print("Failed to download image: \(error?.localizedDescription ?? "unknown error")")
                completion(nil)
                return
            }
            
            // Save to temp file
            let tmpDirectory = FileManager.default.temporaryDirectory
            let tmpFile = tmpDirectory.appendingPathComponent(url.lastPathComponent)
            
            do {
                try data.write(to: tmpFile)
                let attachment = try UNNotificationAttachment(identifier: "image", url: tmpFile, options: nil)
                completion(attachment)
            } catch {
                print("Failed to create notification attachment: \(error)")
                completion(nil)
            }
        }
        task.resume()
    }
}
