import UIKit
import FirebaseCore
import FirebaseMessaging
import UserNotifications

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
        
        // Initialize Firebase
        FirebaseApp.configure()
        
        // Set delegates
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        
        // Request notification permissions
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .badge, .sound]
        ) { granted, error in
            print("Notification permission granted: \(granted)")
            if let error = error {
                print("Error requesting notifications: \(error.localizedDescription)")
            }
        }
        
        application.registerForRemoteNotifications()
        
        // Clear badge when app launches
        UIApplication.shared.applicationIconBadgeNumber = 0
        
        // Handle notification if app was launched from notification tap
        if let notificationUserInfo = launchOptions?[.remoteNotification] as? [AnyHashable: Any] {
            print("App launched from notification tap")
            handleNotificationTap(userInfo: notificationUserInfo)
        }
        
        return true
    }
    
    // MARK: - App Lifecycle
    
    func applicationDidBecomeActive(_ application: UIApplication) {
        // Clear badge when app becomes active
        print("App became active - clearing badge")
        UIApplication.shared.applicationIconBadgeNumber = 0
    }
    
    // MARK: - APNs Token
    
    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        print("APNs device token: \(deviceToken)")
        Messaging.messaging().apnsToken = deviceToken
    }
    
    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        print("Failed to register for remote notifications: \(error.localizedDescription)")
    }
    
    // MARK: - FCM Token
    
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("FCM token: \(fcmToken ?? "nil")")
        
        // Store token for Kotlin access
        if let token = fcmToken {
            FCMBridge.shared.setCurrentToken(token)
        }
        
        // Send token notification
        let dataDict: [String: String] = ["token": fcmToken ?? ""]
        NotificationCenter.default.post(
            name: Notification.Name("FCMToken"),
            object: nil,
            userInfo: dataDict
        )
    }
    
    // MARK: - Background Notification Handling
    
    /// Called when notification is received while app is in BACKGROUND or KILLED state
    /// This is where FCM notifications arrive when app is not in foreground
    func application(
        _ application: UIApplication,
        didReceiveRemoteNotification userInfo: [AnyHashable: Any],
        fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void
    ) {
        print("=== FCM Message Received (Background/Killed) ===")
        print("UserInfo: \(userInfo)")
        
        // Parse notification data
        guard let notificationData = parseNotificationData(from: userInfo) else {
            print("⚠️ Failed to parse notification data")
            completionHandler(.failed)
            return
        }
        
        // Apply client-side filtering
        if shouldShowNotification(userInfo: userInfo) {
            print("✅ Notification passed filters, displaying")
            
            // Create and display local notification with parsed content
            displayNotification(data: notificationData, userInfo: userInfo)
            completionHandler(.newData)
        } else {
            print("🔇 Notification filtered out by user preferences")
            completionHandler(.noData)
        }
    }
    
    // MARK: - Foreground Notifications
    
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        let userInfo = notification.request.content.userInfo
        
        print("=== FCM Message Received (Foreground) ===")
        print("UserInfo: \(userInfo)")
        
        // Apply client-side filtering
        if shouldShowNotification(userInfo: userInfo) {
            print("✅ Notification passed filters, showing to user")
            // Show notification even when app is in foreground
            if #available(iOS 14.0, *) {
                completionHandler([.banner, .badge, .sound])
            } else {
                completionHandler([.alert, .badge, .sound])
            }
        } else {
            print("🔇 Notification filtered out by user preferences")
            completionHandler([]) // Don't show notification
        }
    }
    
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo
        print("User tapped notification: \(userInfo)")
        
        handleNotificationTap(userInfo: userInfo)
        
        completionHandler()
    }
    
    // MARK: - Notification Parsing and Display
    
    /// Parse notification data from FCM payload
    private func parseNotificationData(from userInfo: [AnyHashable: Any]) -> NotificationData? {
        // Extract data from FCM payload
        // FCM sends data in the "data" key for data-only messages
        // Or directly in userInfo for notification messages
        
        var data: [String: String] = [:]
        
        // Try to get data from FCM data payload first
        if let fcmData = userInfo["data"] as? [String: String] {
            data = fcmData
        } else {
            // Fall back to direct userInfo parsing
            for (key, value) in userInfo {
                if let keyString = key as? String, let valueString = value as? String {
                    data[keyString] = valueString
                }
            }
        }
        
        // Parse required fields
        guard let notificationType = data["notification_type"],
              let launchId = data["launch_id"],
              let launchName = data["launch_name"],
              let launchNet = data["launch_net"],
              let launchLocation = data["launch_location"],
              let agencyId = data["agency_id"],
              let locationId = data["location_id"] else {
            print("❌ Missing required notification fields")
            return nil
        }
        
        return NotificationData(
            notificationType: notificationType,
            launchId: launchId,
            launchUuid: data["launch_uuid"] ?? "",
            launchName: launchName,
            launchImage: data["launch_image"],
            launchNet: launchNet,
            launchLocation: launchLocation,
            webcast: data["webcast"] ?? "false",
            webcastLive: data["webcast_live"],
            agencyId: agencyId,
            locationId: locationId
        )
    }
    
    /// Display notification with proper formatting (matches Android implementation)
    private func displayNotification(data: NotificationData, userInfo: [AnyHashable: Any]) {
        let content = UNMutableNotificationContent()
        
        // Format title - add 🔴 emoji if webcast is live (matches Android)
        let baseTitle = data.launchName
        let displayTitle = data.isWebcastLive() ? "🔴 \(baseTitle)" : baseTitle
        
        // Format body using same logic as Android
        let displayBody = getNotificationBody(data: data)
        
        // Try to use FCM notification payload if available, otherwise use formatted values
        if let fcmNotification = userInfo["aps"] as? [String: Any],
           let alert = fcmNotification["alert"] as? [String: String] {
            content.title = alert["title"] ?? displayTitle
            content.body = alert["body"] ?? displayBody
        } else {
            content.title = displayTitle
            content.body = displayBody
        }
        
        // Add badge
        content.badge = 1
        
        // Add sound
        content.sound = .default
        
        // Add custom data for handling taps (includes launch UUID for navigation)
        var extendedUserInfo = userInfo
        extendedUserInfo["launch_id"] = data.launchUuid
        extendedUserInfo["launch_uuid"] = data.launchUuid
        extendedUserInfo["launch_name"] = data.launchName
        content.userInfo = extendedUserInfo
        
        // Add category for action buttons (optional - can add later)
        // content.categoryIdentifier = "LAUNCH_NOTIFICATION"
        
        print("📱 [Notification] Building notification...")
        print("📱 [Notification] Title: \(content.title ?? "nil")")
        print("📱 [Notification] Body: \(content.body ?? "nil")")
        print("📱 [Notification] Image URL: \(data.launchImage ?? "nil")")
        
        // Try to add image if available
        if let imageUrlString = data.launchImage,
           let imageUrl = URL(string: imageUrlString) {
            downloadImage(from: imageUrl) { attachment in
                if let attachment = attachment {
                    content.attachments = [attachment]
                    print("📱 [Notification] ✅ Image attached")
                } else {
                    print("📱 [Notification] ⚠️ Failed to attach image")
                }
                self.scheduleNotification(content: content, identifier: data.launchUuid)
            }
        } else {
            print("📱 [Notification] No image URL, showing text-only notification")
            scheduleNotification(content: content, identifier: data.launchUuid)
        }
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
                print("❌ Failed to display notification: \(error)")
            } else {
                print("✅ Notification displayed successfully")
            }
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
    
    // MARK: - Client-Side Filtering (matches Android implementation)
    
    private func shouldShowNotification(userInfo: [AnyHashable: Any]) -> Bool {
        // Convert userInfo to String dictionary
        var dataMap: [String: String] = [:]
        for (key, value) in userInfo {
            if let keyString = key as? String, let valueString = value as? String {
                dataMap[keyString] = valueString
            }
        }
        
        // Use the FCMBridge filter
        return FCMBridge.shared.shouldShowNotification(data: dataMap)
    }
    
    // MARK: - Deep Linking
    
    private func handleNotificationTap(userInfo: [AnyHashable: Any]) {
        // TODO: Implement deep linking
        // Example: Navigate to launch detail screen
        
        guard let launchId = userInfo["launch_id"] as? String else {
            return
        }
        
        print("💡 TODO: Navigate to launch detail for ID: \(launchId)")
        // Post notification for app to handle navigation
        NotificationCenter.default.post(
            name: Notification.Name("OpenLaunchDetail"),
            object: nil,
            userInfo: ["launchId": launchId]
        )
    }
}

// MARK: - Supporting Data Models

/// Parsed notification data structure (matches Android NotificationData.kt)
struct NotificationData {
    let notificationType: String
    let launchId: String
    let launchUuid: String
    let launchName: String
    let launchImage: String?
    let launchNet: String
    let launchLocation: String
    let webcast: String
    let webcastLive: String?
    let agencyId: String
    let locationId: String
    
    /// Check if webcast is live (matches Android implementation)
    func isWebcastLive() -> Bool {
        return webcastLive?.lowercased() == "true"
    }
    
    /// Check if webcast is available (matches Android implementation)
    func hasWebcast() -> Bool {
        return webcast.lowercased() == "true"
    }
}
