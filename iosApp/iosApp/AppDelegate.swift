import UIKit
import FirebaseCore
import FirebaseMessaging
import UserNotifications
import ComposeApp
import GoogleMaps

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil
    ) -> Bool {
        
        print("\n🚀 ========================================")
        print("🚀 APP LAUNCH - Initializing Notifications")
        print("🚀 ========================================\n")
        
        // Initialize Google Maps with API key
        print("0️⃣ Configuring Google Maps...")
        let apiKey = AppSecrets.shared.mapsApiKey
        if !apiKey.isEmpty {
            GMSServices.provideAPIKey(apiKey)
            print("✅ Google Maps configured with API key\n")
        } else {
            print("⚠️ Warning: Google Maps API key not found in AppSecrets\n")
        }
        
        // Initialize Firebase
        print("1️⃣ Configuring Firebase...")
        FirebaseApp.configure()
        print("✅ Firebase configured\n")
        
        // Initialize FCMBridge early so it registers its NSNotification observer
        // before any Kotlin code tries to post notifications
        print("2️⃣ Initializing FCM Bridge...")
        _ = FCMBridge.shared
        print("✅ FCM Bridge initialized\n")
        
        // Initialize ShareHelper to listen for share requests from Kotlin
        print("2️⃣.5 Initializing Share Helper...")
        _ = ShareHelper.shared
        print("✅ Share Helper initialized\n")
        
        // Set delegates
        print("3️⃣ Setting notification delegates...")
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        print("✅ Delegates set\n")
        
        // Request notification permissions
        print("4️⃣ Requesting notification permissions...")
        UNUserNotificationCenter.current().requestAuthorization(
            options: [.alert, .badge, .sound]
        ) { granted, error in
            print("\n📋 NOTIFICATION PERMISSION RESULT:")
            if let error = error {
                print("❌ Error: \(error.localizedDescription)")
            } else if granted {
                print("✅ GRANTED - User allowed notifications")
            } else {
                print("❌ DENIED - User rejected notifications")
                print("⚠️  Go to Settings → Space Launch Now → Notifications to enable")
            }
            print("")
        }
        
        print("5️⃣ Registering for remote notifications...")
        application.registerForRemoteNotifications()
        print("✅ Registration request sent (waiting for APNs response...)\n")
        
        // Clear badge when app launches
        if #available(iOS 16.0, *) {
            UNUserNotificationCenter.current().setBadgeCount(0)
        } else {
            UIApplication.shared.applicationIconBadgeNumber = 0
        }
        
        // Handle notification if app was launched from notification tap
        if let notificationUserInfo = launchOptions?[.remoteNotification] as? [AnyHashable: Any] {
            print("📱 App launched from notification tap")
            handleNotificationTap(userInfo: notificationUserInfo)
        }
        
        print("🚀 App launch complete - monitoring for notifications...\n")
        
        return true
    }
    
    // MARK: - App Lifecycle
    
    func applicationDidBecomeActive(_ application: UIApplication) {
        // Clear badge when app becomes active
        print("\n📱 App became active - clearing badge")
        if #available(iOS 16.0, *) {
            UNUserNotificationCenter.current().setBadgeCount(0)
        } else {
            UIApplication.shared.applicationIconBadgeNumber = 0
        }
    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
        print("\n🌙 App entered background - background notifications should still work")
    }
    
    // MARK: - APNs Token
    
    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        let tokenString = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        print("\n" + String(repeating: "=", count: 60))
        print("✅ APNS DEVICE TOKEN REGISTERED")
        print(String(repeating: "=", count: 60))
        print("Token: \(tokenString.prefix(20))...\(tokenString.suffix(20))")
        print("Full Token: \(tokenString)")
        print(String(repeating: "=", count: 60) + "\n")
        
        Messaging.messaging().apnsToken = deviceToken
        print("✅ APNs token set on Firebase Messaging")
        print("⏳ Waiting for FCM token...\n")
        
        // Process any pending Kotlin FCM requests now that we have APNs token
        FCMBridge.shared.processPendingKotlinRequests()
    }
    
    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        print("\n" + String(repeating: "=", count: 60))
        print("❌ CRITICAL: FAILED TO REGISTER FOR REMOTE NOTIFICATIONS")
        print(String(repeating: "=", count: 60))
        print("Error: \(error.localizedDescription)")
        print("\nPossible causes:")
        print("1. Running on iOS Simulator (APNs not supported)")
        print("2. Missing Push Notification capability in Xcode")
        print("3. Invalid provisioning profile")
        print("4. Network issues")
        print("\n⚠️  This device will NOT receive push notifications!")
        print(String(repeating: "=", count: 60) + "\n")
    }
    
    // MARK: - FCM Token
    
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("\n========================================")
        print("🔑 FCM REGISTRATION TOKEN RECEIVED")
        print("========================================")
        if let token = fcmToken {
            print("✅ FCM Token: \(token)")
            print("📋 Copy this token to test notifications in Firebase Console")
            
            // Store token for Kotlin access
            FCMBridge.shared.setCurrentToken(token)
            print("✅ Token stored in FCMBridge")
            
            // Send token notification
            let dataDict: [String: String] = ["token": token]
            NotificationCenter.default.post(
                name: Notification.Name("FCMToken"),
                object: nil,
                userInfo: dataDict
            )
            print("✅ Token notification posted")
            
            // Process any pending Kotlin requests (e.g., getToken() calls)
            FCMBridge.shared.processPendingKotlinRequests()
            print("✅ Processed pending Kotlin FCM requests")
            
            // Auto-subscribe to debug topic for testing
            print("\n🔧 DEBUG: Auto-subscribing to k_debug_v4 topic...")
            Messaging.messaging().subscribe(toTopic: "k_debug_v4") { error in
                if let error = error {
                    print("❌ Failed to subscribe to k_debug_v4: \(error.localizedDescription)")
                } else {
                    print("✅ Successfully subscribed to k_debug_v4 topic")
                    print("📱 Device will now receive notifications sent to this topic")
                }
            }
        } else {
            print("❌ FCM token is nil - notifications will NOT work")
        }
        print("========================================\n")
    }
    
    // MARK: - Background Notification Handling
    
    /// Called when notification is received while app is in ANY state (active, background, killed)
    /// This is the PRIMARY handler for FCM data-only messages with content-available: 1
    /// For data-only messages to work in background:
    /// 1. Message must include "content-available": 1 (becomes "content-available": true in userInfo)
    /// 2. Message data is at ROOT level of userInfo, not nested
    /// 3. This method receives the message and must display local notification if needed
    func application(
        _ application: UIApplication,
        didReceiveRemoteNotification userInfo: [AnyHashable: Any],
        fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void
    ) {
        print("\n========================================")
        print("📩 FCM NOTIFICATION RECEIVED (Completion Handler)")
        print("========================================")
        print("App State: \(UIApplication.shared.applicationState.rawValue) (0=active, 1=inactive, 2=background)")
        
        // Log background refresh status
        let backgroundRefreshStatus = UIApplication.shared.backgroundRefreshStatus
        print("Background Refresh Status: \(backgroundRefreshStatusString(backgroundRefreshStatus))")
        if backgroundRefreshStatus == .denied || backgroundRefreshStatus == .restricted {
            print("⚠️  WARNING: Background App Refresh is disabled!")
            print("⚠️  Notifications may not work when app is killed")
            print("⚠️  Enable in Settings → General → Background App Refresh")
        }
        
        // Check if this is a data-only message
        let hasContentAvailable = (userInfo["aps"] as? [String: Any])?["content-available"] as? Int == 1
        print("Content-Available: \(hasContentAvailable ? "YES (data-only)" : "NO (notification)")")
        
        if !hasContentAvailable {
            print("⚠️  WARNING: Message does NOT have content-available: 1")
            print("⚠️  This means it won't wake the app when killed")
            print("⚠️  Backend must send: apns.payload.aps.content-available = 1")
        }
        
        print("UserInfo keys: \(userInfo.keys)")
        print("Full UserInfo: \(userInfo)")
        
        // Parse notification data
        guard let notificationData = parseNotificationData(from: userInfo) else {
            print("⚠️ Failed to parse notification data")
            completionHandler(.failed)
            return
        }
        
        // Apply client-side filtering
        let shouldShow = shouldShowNotification(userInfo: userInfo)
        
        if shouldShow {
            print("✅ Notification passed filters, displaying")
            
            // ALWAYS create and display local notification regardless of app state
            // This ensures notifications show when app is:
            // - Killed (background delivery)
            // - Background (already not in foreground)
            // - Foreground (will show as banner)
            displayNotification(data: notificationData, userInfo: userInfo)
            
            // Save to history (displayed and shown)
            saveNotificationToHistory(
                data: notificationData,
                userInfo: userInfo,
                wasFiltered: false,
                filterReason: nil,
                wasShown: true
            )
            
            completionHandler(.newData)
        } else {
            print("🔇 Notification filtered out by user preferences")
            
            // Save to history (filtered and not shown)
            saveNotificationToHistory(
                data: notificationData,
                userInfo: userInfo,
                wasFiltered: true,
                filterReason: "Filtered by user notification preferences",
                wasShown: false
            )
            
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
        
        print("\n========================================")
        print("📩 FCM NOTIFICATION RECEIVED (Foreground)")
        print("========================================")
        print("Title: \(notification.request.content.title)")
        print("Body: \(notification.request.content.body)")
        print("UserInfo: \(userInfo)")
        
        // Parse notification data for history
        if let notificationData = parseNotificationData(from: userInfo) {
            // Apply client-side filtering
            let shouldShow = shouldShowNotification(userInfo: userInfo)
            
            if shouldShow {
                print("✅ Notification passed filters, showing to user")
                
                // Note: History is saved in didReceiveRemoteNotification handler
                // to avoid duplicates when app is in foreground
                
                // Show notification even when app is in foreground
                if #available(iOS 14.0, *) {
                    completionHandler([.banner, .badge, .sound])
                } else {
                    completionHandler([.alert, .badge, .sound])
                }
            } else {
                print("🔇 Notification filtered out by user preferences")
                
                // Note: History is saved in didReceiveRemoteNotification handler
                // to avoid duplicates when app is in foreground
                
                completionHandler([]) // Don't show notification
            }
        } else {
            print("⚠️ Failed to parse notification data for history")
            completionHandler([])
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
        print("\n🔍 [Parse] Parsing notification data...")
        print("🔍 [Parse] Raw userInfo keys: \(userInfo.keys)")
        
        // Extract data from FCM payload
        // For DATA-ONLY messages (content-available: 1):
        //   - Data is at ROOT level of userInfo
        //   - Format: ["notification_type": "value", "launch_id": "value", ...]
        // For NOTIFICATION messages:
        //   - May have nested "data" key
        //   - Or data mixed with "aps" and "gcm.message_id"
        
        var data: [String: String] = [:]
        
        // FIRST: Try to extract data from root level (data-only messages)
        for (key, value) in userInfo {
            if let keyString = key as? String {
                // Skip FCM/APNS metadata keys
                if keyString == "aps" || keyString.hasPrefix("gcm.") || keyString.hasPrefix("google.c.") {
                    continue
                }
                // Convert value to string
                if let valueString = value as? String {
                    data[keyString] = valueString
                } else {
                    // Handle non-string values (numbers, etc.)
                    data[keyString] = String(describing: value)
                }
            }
        }
        
        // SECOND: If no data found at root, try nested "data" key (legacy support)
        if data.isEmpty, let fcmData = userInfo["data"] as? [String: String] {
            print("🔍 [Parse] Found nested 'data' key")
            data = fcmData
        }
        
        print("🔍 [Parse] Extracted data keys: \(data.keys.sorted())")
        
        // Parse required fields
        guard let notificationType = data["notification_type"],
              let launchId = data["launch_id"],
              let launchName = data["launch_name"],
              let launchNet = data["launch_net"],
              let launchLocation = data["launch_location"],
              let agencyId = data["agency_id"] ?? data["lsp_id"], // V5 payloads use lsp_id instead of agency_id
              let locationId = data["location_id"] else {
            print("❌ [Parse] Missing required notification fields")
            print("❌ [Parse] Available fields: \(data.keys.sorted())")
            print("❌ [Parse] notification_type: \(data["notification_type"] ?? "missing")")
            print("❌ [Parse] launch_id: \(data["launch_id"] ?? "missing")")
            print("❌ [Parse] launch_name: \(data["launch_name"] ?? "missing")")
            return nil
        }
        
        let notificationData = NotificationData(
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
        
        print("✅ [Parse] Successfully parsed notification data")
        print("✅ [Parse] Type: \(notificationType), Launch: \(launchName)")
        
        return notificationData
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
    
    // MARK: - Client-Side Filtering (uses shared Kotlin NotificationFilter)
    
    private func shouldShowNotification(userInfo: [AnyHashable: Any]) -> Bool {
        print("\n========================================")
        print("🔍 [iOS SWIFT] EVALUATING NOTIFICATION FILTER")
        print("========================================")
        
        // Convert userInfo to String dictionary for Kotlin interop
        // Extract data from root level (data-only messages) or nested "data" key
        var dataMap: [String: String] = [:]
        
        // Extract from root level first (skipping metadata)
        for (key, value) in userInfo {
            if let keyString = key as? String {
                // Skip FCM/APNS metadata keys
                if keyString == "aps" || keyString.hasPrefix("gcm.") || keyString.hasPrefix("google.c.") {
                    continue
                }
                // Convert value to string
                if let valueString = value as? String {
                    dataMap[keyString] = valueString
                } else {
                    dataMap[keyString] = String(describing: value)
                }
            }
        }
        
        // Fall back to nested "data" key if no data at root
        if dataMap.isEmpty, let fcmData = userInfo["data"] as? [String: String] {
            dataMap = fcmData
        }
        
        // Print notification data being evaluated
        print("📩 [iOS SWIFT] Notification Data:")
        print("   - Type: \(dataMap["notification_type"] ?? "unknown")")
        print("   - Launch: \(dataMap["launch_name"] ?? "unknown")")
        print("   - Agency ID: \(dataMap["agency_id"] ?? dataMap["lsp_id"] ?? "unknown")")
        print("   - Location ID: \(dataMap["location_id"] ?? "unknown")")
        print("   - Webcast: \(dataMap["webcast"] ?? "false")")
        print("   - Webcast Live: \(dataMap["webcast_live"] ?? "false")")
        
        print("\n📲 [iOS SWIFT] Calling Kotlin bridge for filter evaluation...")
        
        // Use shared Kotlin filter logic (same as Android)
        // This ensures consistent filtering behavior across platforms
        let result = IosNotificationBridge.shared.shouldShowNotification(data: dataMap)
        
        print("\n🎯 [iOS SWIFT] Filter Decision: \(result ? "SHOW ✅" : "SUPPRESS 🔇")")
        print("========================================\n")
        
        return result
    }
    
    // MARK: - Notification History
    
    /// Save notification to history for debugging
    private func saveNotificationToHistory(
        data: NotificationData,
        userInfo: [AnyHashable: Any],
        wasFiltered: Bool,
        filterReason: String?,
        wasShown: Bool
    ) {
        // Log full notification JSON
        if let jsonData = try? JSONSerialization.data(withJSONObject: userInfo, options: .prettyPrinted),
           let jsonString = String(data: jsonData, encoding: .utf8) {
            print("📦 Full Notification JSON:\n\(jsonString)")
        }
        
        print("💾 Saving to history: wasFiltered=\(wasFiltered), wasShown=\(wasShown)")
        
        // Convert userInfo to parallel arrays for Kotlin interop
        var keys: [String] = []
        var values: [String] = []
        
        for (key, value) in userInfo {
            if let keyString = key as? String {
                keys.append(keyString)
                values.append(String(describing: value))
            }
        }
        
        // Format display title and body (same logic as displayNotification)
        let displayTitle = data.isWebcastLive() ? "🔴 \(data.launchName)" : data.launchName
        let displayBody = getNotificationBody(data: data)
        
        // Call Kotlin bridge to save notification
        IosPushMessagingBridge.shared.saveNotificationToHistory(
            notificationType: data.notificationType,
            launchId: data.launchId,
            launchUuid: data.launchUuid,
            launchName: data.launchName,
            launchImage: data.launchImage,
            launchNet: data.launchNet,
            launchLocation: data.launchLocation,
            webcast: data.webcast,
            webcastLive: data.webcastLive,
            agencyId: data.agencyId,
            locationId: data.locationId,
            displayedTitle: displayTitle,
            displayedBody: displayBody,
            rawDataKeys: keys,
            rawDataValues: values,
            wasFiltered: wasFiltered,
            filterReason: filterReason,
            wasShown: wasShown
        )
        
        print("💾 Saved notification to history: \(data.launchName) (filtered: \(wasFiltered))")
    }
    
    // MARK: - Deep Linking
    
    private func handleNotificationTap(userInfo: [AnyHashable: Any]) {
        // Navigate to launch detail screen when notification is tapped
        guard let launchId = userInfo["launch_id"] as? String else {
            print("⚠️ No launch_id found in notification userInfo")
            return
        }
        
        print("🚀 Navigating to launch detail for ID: \(launchId)")
        // Call Kotlin function to trigger navigation
        MainViewControllerKt.setNotificationLaunchId(launchId: launchId)
    }
    
    // MARK: - Helper Methods
    
    private func backgroundRefreshStatusString(_ status: UIBackgroundRefreshStatus) -> String {
        switch status {
        case .available:
            return "✅ Available"
        case .denied:
            return "❌ DENIED by user"
        case .restricted:
            return "⚠️  RESTRICTED by system"
        @unknown default:
            return "❓ Unknown"
        }
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
