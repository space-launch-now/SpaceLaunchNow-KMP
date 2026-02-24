# iOS Notification Service Extension Setup Guide

**Feature**: V5 Client-Side Notification System  
**Platform**: iOS only  
**Status**: Ready for Xcode integration

---

## Overview

This guide walks through setting up the Notification Service Extension (NSE) for client-side V5 notification filtering on iOS.

**Architecture**: NSE intercepts notifications with `mutable-content: 1`, applies filter logic, and either displays or suppresses them before the user sees them.

---

## Prerequisites

✅ **CODE COMPLETE** - All Swift files implemented:
- ✅ V5FilterPreferences.swift - Filter preference model
- ✅ V5PreferencesSyncManager.swift - App Group sync manager
- ✅ V5NotificationFilter.swift - Filter logic (all 7 filter types)
- ✅ V5NotificationData.swift - Payload parsing (existing)
- ✅ NotificationService.swift - NSE entry point with V5 filtering
- ✅ iosApp.entitlements - Updated with App Groups
- ✅ iosAppDebug.entitlements - Updated with App Groups

⚠️ **XCODE INTEGRATION REQUIRED**:
1. ✅ Create NSE target (DONE - you created it)
2. ✅ Replace NotificationService.swift template (DONE - code is in place)
3. ⏳ **NEXT**: Add Swift files to NSE target membership
4. ⏳ Build and verify compilation
5. ⏳ Test on physical device

---

## Step 1: Create NSE Target in Xcode ✅ DONE

### 1.1 Add New Target ✅ DONE

You've already completed this:
1. ✅ Opened `iosApp.xcodeproj` in Xcode
2. ✅ File → New → Target → Notification Service Extension
3. ✅ Configured with Product Name: `NotificationServiceExtension`
4. ✅ Activated the scheme

### 1.2 Verify Target Created ✅ DONE

You've verified:
- ✅ `NotificationServiceExtension` target exists in Project Navigator
- ✅ `NotificationService.swift` template file created
- ✅ `Info.plist` for NSE created

### 1.3 Configure Bundle Identifier ✅ DONE

You've configured:
- ✅ Bundle Identifier: `me.calebjones.spacelaunchnow.NotificationServiceExtension`
- ✅ Minimum iOS Version: **15.0**

---

## Step 2: Implement NotificationService.swift ✅ DONE

### 2.1 Replace Template Code ✅ DONE

You've successfully replaced the template with the V5 filtering implementation:
- ✅ V5 payload detection (checks for `lsp_id` field)
- ✅ V5NotificationData parsing
- ✅ V5PreferencesSyncManager integration (App Group)
- ✅ V5NotificationFilter application
- ✅ Allow/block decision logic
- ✅ Image attachment support
- ✅ Comprehensive logging

**Current State**: Code is in place but shows compilation errors (expected - fixed in Step 3)

### 2.2 Configure Info.plist ✅ AUTO-GENERATED

The `Info.plist` for NSE was automatically created by Xcode with correct defaults:
- ✅ `NSExtensionPointIdentifier`: `com.apple.usernotifications.service`
- ✅ `NSExtensionPrincipalClass`: Points to `NotificationService`

### 2.3 Add App Groups Capability ✅ DONE

You've configured App Groups:
- ✅ Selected `NotificationServiceExtension` target
- ✅ Added **App Groups** capability
- ✅ Added `group.me.calebjones.spacelaunchnow`
- ✅ Entitlements file created automatically

---

## Step 3: Add Swift Files to Both Targets ⚠️ DO THIS NOW

**CRITICAL**: These files need to be accessible to BOTH main app and NSE.

**Current Issue**: The compilation errors you're seeing (Cannot find 'V5NotificationData', etc.) are because these files aren't added to the NSE target yet.

**Fix**: Add each file to the NotificationServiceExtension target:

### 3.1 V5NotificationData.swift ⚠️ ADD TO NSE TARGET

**Action Required**:
1. In Xcode, select `iosApp/iosApp/V5NotificationData.swift` in Project Navigator
2. Open **File Inspector** (right panel, ⌘⌥1)
3. Find **Target Membership** section
4. Check ✅ **NotificationServiceExtension** (in addition to iosApp)

### 3.2 V5FilterPreferences.swift ⚠️ ADD TO NSE TARGET

**Action Required**:
1. In Xcode, select `iosApp/iosApp/V5FilterPreferences.swift`
2. Open **File Inspector** (⌘⌥1)
3. **Target Membership** → Check ✅ **NotificationServiceExtension**

### 3.3 V5PreferencesSyncManager.swift ⚠️ ADD TO NSE TARGET

**Action Required**:
1. In Xcode, select `iosApp/iosApp/V5PreferencesSyncManager.swift`
2. Open **File Inspector** (⌘⌥1)
3. **Target Membership** → Check ✅ **NotificationServiceExtension**

### 3.4 V5NotificationFilter.swift ⚠️ ADD TO NSE TARGET

**Action Required**:
1. In Xcode, select `iosApp/iosApp/V5NotificationFilter.swift`
2. Open **File Inspector** (⌘⌥1)
3. **Target Membership** → Check ✅ **NotificationServiceExtension**

---

**✅ Once Complete**: All 4 Swift files will be compiled into both targets. All compilation errors will disappear.

**Quick Check**: Each file should show 2 checked boxes under Target Membership:
- ✅ iosApp
- ✅ NotificationServiceExtension

---

## Step 4: Configure App Groups in Apple Developer Portal

### 4.1 Create App Group

1. Go to [Apple Developer Portal](https://developer.apple.com)
2. Certificates, Identifiers & Profiles
3. Identifiers → App Groups
4. Click **+** to create new App Group
5. **Identifier**: `group.me.calebjones.spacelaunchnow`
6. Save

### 4.2 Enable for App ID

1. Identifiers → App IDs
2. Select `me.calebjones.spacelaunchnow`
3. Edit → Capabilities → App Groups → Enable
4. Select `group.me.calebjones.spacelaunchnow`
5. Save

### 4.3 Enable for NSE App ID

1. Identifiers → App IDs
2. Select `me.calebjones.spacelaunchnow.NotificationServiceExtension`
3. Edit → Capabilities → App Groups → Enable
4. Select `group.me.calebjones.spacelaunchnow`
5. Save

### 4.4 Regenerate Provisioning Profiles

1. Profiles → Development/Distribution
2. Select profile for main app → Edit → Save
3. Select profile for NSE → Edit → Save
4. Download and install updated profiles in Xcode

---

## Step 5: Verify Xcode Configuration

### 5.1 Main App Capabilities

1. Select `iosApp` target
2. Signing & Capabilities tab
3. **Verify** App Groups capability:
   - ✅ `group.me.calebjones.spacelaunchnow`
   - ✅ `group.me.spacelaunchnow.spacelaunchnow` (existing)

### 5.2 NSE Capabilities

1. Select `NotificationServiceExtension` target
2. Signing & Capabilities tab
3. **Verify** App Groups capability:
   - ✅ `group.me.calebjones.spacelaunchnow`

### 5.3 Build Settings

**NotificationServiceExtension target**:
- Deployment Target: iOS 15.0
- Swift Language Version: Swift 5
- Product Bundle Identifier: `me.calebjones.spacelaunchnow.NotificationServiceExtension`

---

## Step 6: Build and Test ⚠️ DO THIS AFTER STEP 3

### 6.1 Clean and Build NSE

**Do this after adding files to NSE target (Step 3)**:

1. Clean Build Folder: Product → Clean Build Folder (⇧⌘K)
2. Select `NotificationServiceExtension` scheme in Xcode
3. Product → Build (⌘B)
4. **Expected**: Build succeeds with no errors
5. **If errors persist**: Restart Xcode and rebuild

### 6.2 Build Main App

1. Select `iosApp` scheme in Xcode
2. Product → Build (⌘B)
3. **Expected**: Build succeeds with no errors

### 6.3 Test on Device

**IMPORTANT**: NSE requires physical device, does NOT work in Simulator.

1. Connect iPhone with iOS 15+
2. Select `iosApp` scheme + physical device
3. Run app (⌘R)
4. Grant notification permissions when prompted
5. Send test notification (see Testing section below)

---

## Step 7: Testing V5 Notifications

### 7.1 Send Test Notification via Firebase Console

1. Go to Firebase Console → Cloud Messaging
2. Send test message
3. Configure notification:

```json
{
  "to": "/topics/debug_v5_ios",
  "mutable_content": true,
  "data": {
    "notification_type": "tenMinutes",
    "title": "🚀 SpaceX Launch in 10 Minutes",
    "body": "Falcon 9 Block 5 | Starlink Group 6-32",
    "launch_uuid": "550e8400-e29b-41d4-a716-446655440000",
    "lsp_id": "121",
    "location_id": "27",
    "webcast": "true",
    "launch_name": "Falcon 9 Block 5 | Starlink",
    "launch_net": "2026-01-27T10:00:00Z",
    "launch_location": "Cape Canaveral SLC-40"
  }
}
```

### 7.2 Verify NSE Execution

1. Xcode → Window → Devices and Simulators
2. Select your iPhone
3. Open Console (or use macOS Console app)
4. Filter: `process:NotificationServiceExtension`
5. Send notification
6. **Look for logs**:
   ```
   🔔 NSE: Notification received
   ✅ NSE: Detected V5 payload
   ✅ NSE: Parsed V5 data: ...
   ✅ NSE: Loaded preferences: ...
   🔍 NSE: Filter result: ...
   ✅ NSE: ALLOWED - Delivering notification
   ```

### 7.3 Test Filtering

**Test Case 1: Block by LSP**
- Set preferences: `subscribedLspIds = [44]` (NASA only)
- Send notification: `lsp_id = "121"` (SpaceX)
- **Expected**: Notification blocked (not displayed)

**Test Case 2: Allow by LSP**
- Set preferences: `subscribedLspIds = [121]` (SpaceX only)
- Send notification: `lsp_id = "121"` (SpaceX)
- **Expected**: Notification displayed

**Test Case 3: Webcast Only**
- Set preferences: `webcastOnly = true`
- Send notification: `webcast = "false"`
- **Expected**: Notification blocked

---

## Step 8: Integrate with Main App (Future Work)

**TODO** (tracked in tasks.md):

- [ ] T007: Update `NotificationRepository` to call `V5PreferencesSyncManager.shared.savePreferences()` when user changes filters
- [ ] Update `NotificationSettingsScreen` to persist V5 preferences to App Group
- [ ] Add debug menu item to test V5 preference sync

---

## Troubleshooting

### NSE Not Running

**Symptom**: Notifications appear immediately without filtering

**Fix**:
1. Verify `mutable_content: true` in Firebase payload
2. Check NSE target is installed: Settings → General → iPhone Storage → Space Launch Now → Show App Extensions
3. Restart device
4. Reinstall app

### App Group Not Working

**Symptom**: NSE logs "Failed to access App Group UserDefaults"

**Fix**:
1. Verify entitlements files have correct group ID
2. Verify Apple Developer Portal has App Group enabled
3. Regenerate provisioning profiles
4. Clean build folder (⇧⌘K) and rebuild

### Swift Files Not Found

**Symptom**: Compilation errors like "Cannot find V5NotificationData"

**Fix**:
1. Verify all 4 Swift files have **NotificationServiceExtension** checked in Target Membership
2. Clean build folder and rebuild
3. Restart Xcode

### Debugging NSE

**Method 1**: Xcode Console Logs
- Run app on device
- Filter: `process:NotificationServiceExtension`
- Look for `🔔 NSE:` log lines

**Method 2**: Xcode Debugger
1. Select `NotificationServiceExtension` scheme
2. Run → Debug → Attach to Process by PID or Name
3. Enter: `NotificationServiceExtension`
4. Set breakpoints in `NotificationService.swift`
5. Send notification

**Method 3**: macOS Console App
- Connect iPhone
- Open Console.app on Mac
- Select iPhone in left sidebar
- Filter: `process:NotificationServiceExtension`

---

## Architecture Summary

```
┌─────────────────────────────────────────────────────┐
│ Firebase Cloud Messaging (Server)                   │
│ - Sends V5 payload with mutable_content: true       │
└────────────────┬────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────┐
│ Apple Push Notification Service (APNs)              │
│ - Routes to iOS device                              │
└────────────────┬────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────┐
│ iOS: Detects mutable-content flag                   │
│ - Launches Notification Service Extension (NSE)     │
│ - Gives 30 seconds to modify notification           │
└────────────────┬────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────┐
│ NotificationService.swift (NSE Entry Point)         │
│ 1. Parse V5 payload (V5NotificationData)            │
│ 2. Load preferences (V5PreferencesSyncManager)      │
│ 3. Apply filter (V5NotificationFilter)              │
└────────────────┬────────────────────────────────────┘
                 │
         ┌───────┴───────┐
         ▼               ▼
    ALLOWED          BLOCKED
         │               │
         ▼               ▼
 Deliver full     Deliver empty
 notification      notification
         │               │
         ▼               ▼
   User sees        Silent drop
  notification    (not displayed)
```

---

## File Locations

**Created Files**:
```
iosApp/
├── iosApp/
│   ├── V5FilterPreferences.swift               ← NEW (both targets)
│   ├── V5PreferencesSyncManager.swift          ← NEW (both targets)
│   ├── V5NotificationFilter.swift              ← NEW (both targets)
│   ├── V5NotificationData.swift                ← EXISTING (add to NSE target)
│   ├── iosApp.entitlements                     ← UPDATED (App Groups)
│   └── iosAppDebug.entitlements                ← UPDATED (App Groups)
├── NotificationServiceExtension/               ← NEW FOLDER
│   ├── NotificationService.swift               ← NEW (NSE entry point)
│   ├── Info.plist                              ← NEW (NSE config)
│   └── NotificationServiceExtension.entitlements ← NEW (App Groups)
```

---

## Next Steps

1. ✅ Follow this guide to set up NSE target in Xcode
2. ✅ Test V5 notifications on physical device
3. ⏳ Implement main app persistence (T007)
4. ⏳ Add UI for V5 filter settings
5. ⏳ Production testing with real notifications

---

## References

- [Apple NSE Documentation](https://developer.apple.com/documentation/usernotifications/unnotificationserviceextension)
- [App Groups Documentation](https://developer.apple.com/documentation/bundleresources/entitlements/com_apple_security_application-groups)
- [V5 Implementation Plan](/specs/dev/plan.md)
- [iOS Tasks](/specs/dev/ios/tasks.md)

---

## Appendix: Reference Implementation

### NotificationService.swift (Full Code)

**Location**: Copy this into `NotificationServiceExtension/NotificationService.swift` in Xcode

```swift
import UserNotifications

class NotificationService: UNNotificationServiceExtension {

    var contentHandler: ((UNNotificationContent) -> Void)?
    var bestAttemptContent: UNMutableNotificationContent?

    override func didReceive(
        _ request: UNNotificationRequest,
        withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void
    ) {
        self.contentHandler = contentHandler
        bestAttemptContent = (request.content.mutableCopy() as? UNMutableNotificationContent)
        
        print("\n========================================")
        print("🔔 NSE: Notification received")
        print("========================================")
        
        guard let bestAttemptContent = bestAttemptContent else {
            print("❌ NSE: Failed to create mutable content")
            contentHandler(request.content)
            return
        }
        
        let userInfo = request.content.userInfo
        print("📦 NSE: userInfo keys: \(userInfo.keys)")
        
        // 1. Check if this is a V5 notification
        guard V5NotificationData.isV5Payload(userInfo) else {
            print("ℹ️  NSE: Not a V5 payload (no lsp_id field), allowing through")
            contentHandler(bestAttemptContent)
            return
        }
        
        print("✅ NSE: Detected V5 payload")
        
        // 2. Parse V5 notification data
        guard let v5Data = V5NotificationData.fromUserInfo(userInfo) else {
            print("❌ NSE: Failed to parse V5 payload, blocking notification")
            deliverEmptyNotification(contentHandler)
            return
        }
        
        print("✅ NSE: Parsed V5 data: \(v5Data.debugDescription())")
        
        // 3. Load user preferences from App Group
        let preferences = V5PreferencesSyncManager.shared.loadPreferences()
        print("✅ NSE: Loaded preferences: \(preferences.debugDescription())")
        
        // 4. Apply filter logic
        let filterResult = V5NotificationFilter.shouldShow(
            payload: v5Data,
            preferences: preferences
        )
        
        print("🔍 NSE: Filter result: \(filterResult)")
        
        // 5. Deliver or suppress based on filter result
        switch filterResult {
        case .allowed:
            print("✅ NSE: ALLOWED - Delivering notification")
            
            // Use server-provided title and body
            bestAttemptContent.title = v5Data.title
            bestAttemptContent.body = v5Data.body
            
            // Add notification sound
            bestAttemptContent.sound = .default
            
            // Optionally download and attach image
            if let imageUrlString = v5Data.launchImage,
               let imageUrl = URL(string: imageUrlString) {
                downloadAndAttachImage(url: imageUrl, to: bestAttemptContent, contentHandler: contentHandler)
            } else {
                contentHandler(bestAttemptContent)
            }
            
        case .blocked(let reason):
            print("🔇 NSE: BLOCKED - \(reason)")
            deliverEmptyNotification(contentHandler)
        }
        
        print("========================================\n")
    }
    
    override func serviceExtensionTimeWillExpire() {
        print("\n⏰ NSE: Time will expire - delivering best attempt content")
        
        if let contentHandler = contentHandler,
           let bestAttemptContent = bestAttemptContent {
            contentHandler(bestAttemptContent)
        }
    }
    
    // MARK: - Helper Methods
    
    private func deliverEmptyNotification(_ contentHandler: @escaping (UNNotificationContent) -> Void) {
        let emptyContent = UNMutableNotificationContent()
        emptyContent.title = ""
        emptyContent.body = ""
        emptyContent.sound = nil
        emptyContent.badge = nil
        
        contentHandler(emptyContent)
    }
    
    private func downloadAndAttachImage(
        url: URL,
        to content: UNMutableNotificationContent,
        contentHandler: @escaping (UNNotificationContent) -> Void
    ) {
        let task = URLSession.shared.downloadTask(with: url) { localUrl, response, error in
            defer {
                contentHandler(content)
            }
            
            guard let localUrl = localUrl, error == nil else {
                print("⚠️  NSE: Failed to download image: \(error?.localizedDescription ?? "unknown error")")
                return
            }
            
            do {
                let attachment = try UNNotificationAttachment(
                    identifier: "launch-image",
                    url: localUrl,
                    options: nil
                )
                content.attachments = [attachment]
                print("✅ NSE: Image attached successfully")
            } catch {
                print("⚠️  NSE: NSE: Failed to create attachment: \(error.localizedDescription)")
            }
        }
        
        task.resume()
    }
}
```

**Note**: This code is also available at `iosApp/NotificationServiceExtension/NotificationService.swift` in your repository.
