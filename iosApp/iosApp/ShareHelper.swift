import UIKit

// This file is only for the main app target, not extensions.
// Widget extensions cannot use UIApplication.shared.
// Exclude this file from extension targets in Xcode Build Phases.
#if !os(watchOS) && canImport(UIKit) && !targetEnvironment(appExtension)

/// Helper class for handling share functionality with proper iPad popover support.
/// This is needed because Kotlin/Native UIKit bindings don't expose popoverPresentationController.
/// 
/// Listens for NSNotifications from Kotlin code and presents the share sheet.
@objc public class ShareHelper: NSObject {
    
    /// Shared singleton instance
    @objc public static let shared = ShareHelper()
    
    /// Notification names - must match PlatformSharingService.kt
    private static let shareTextNotification = Notification.Name("SpaceLaunchNow.ShareText")
    private static let shareUrlNotification = Notification.Name("SpaceLaunchNow.ShareURL")
    
    private override init() {
        super.init()
        setupNotificationObservers()
    }
    
    /// Setup notification observers to receive share requests from Kotlin
    private func setupNotificationObservers() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleShareTextNotification(_:)),
            name: Self.shareTextNotification,
            object: nil
        )
        
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleShareUrlNotification(_:)),
            name: Self.shareUrlNotification,
            object: nil
        )
        
        print("📤 ShareHelper: Notification observers registered")
    }
    
    @objc private func handleShareTextNotification(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
              let text = userInfo["text"] as? String else {
            print("ShareHelper: Invalid share text notification")
            return
        }
        shareText(text)
    }
    
    @objc private func handleShareUrlNotification(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
              let url = userInfo["url"] as? String else {
            print("ShareHelper: Invalid share URL notification")
            return
        }
        let text = userInfo["text"] as? String
        shareUrl(url, text: text)
    }
    
    /// Share text content with proper iPad popover support
    /// - Parameter text: The text to share
    @objc public func shareText(_ text: String) {
        DispatchQueue.main.async {
            let activityItems: [Any] = [text]
            let activityViewController = UIActivityViewController(
                activityItems: activityItems,
                applicationActivities: nil
            )
            
            guard let rootViewController = self.getRootViewController() else {
                print("ShareHelper: Could not find root view controller")
                return
            }
            
            // Configure popover for iPad (required for UIActivityViewController)
            if let popover = activityViewController.popoverPresentationController {
                popover.sourceView = rootViewController.view
                // Center the popover on screen
                popover.sourceRect = CGRect(
                    x: rootViewController.view.bounds.midX,
                    y: rootViewController.view.bounds.midY,
                    width: 0,
                    height: 0
                )
                popover.permittedArrowDirections = [] // No arrows for centered presentation
            }
            
            rootViewController.present(activityViewController, animated: true)
        }
    }
    
    /// Share a URL with optional text
    /// - Parameters:
    ///   - urlString: The URL string to share
    ///   - text: Optional accompanying text
    @objc public func shareUrl(_ urlString: String, text: String?) {
        DispatchQueue.main.async {
            var activityItems: [Any] = []
            
            if let text = text, !text.isEmpty {
                activityItems.append(text)
            }
            
            if let url = URL(string: urlString) {
                activityItems.append(url)
            } else {
                // If URL parsing fails, just share as text
                activityItems.append(urlString)
            }
            
            let activityViewController = UIActivityViewController(
                activityItems: activityItems,
                applicationActivities: nil
            )
            
            guard let rootViewController = self.getRootViewController() else {
                print("ShareHelper: Could not find root view controller")
                return
            }
            
            // Configure popover for iPad
            if let popover = activityViewController.popoverPresentationController {
                popover.sourceView = rootViewController.view
                popover.sourceRect = CGRect(
                    x: rootViewController.view.bounds.midX,
                    y: rootViewController.view.bounds.midY,
                    width: 0,
                    height: 0
                )
                popover.permittedArrowDirections = []
            }
            
            rootViewController.present(activityViewController, animated: true)
        }
    }
    
    /// Gets the current root view controller using the modern scene-based API
    private func getRootViewController() -> UIViewController? {
        guard let windowScene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first(where: { $0.activationState == .foregroundActive }),
              let keyWindow = windowScene.windows.first(where: { $0.isKeyWindow }),
              let rootVC = keyWindow.rootViewController else {
            return nil
        }
        
        // Find the topmost presented view controller
        var topController = rootVC
        while let presentedVC = topController.presentedViewController {
            topController = presentedVC
        }
        
        return topController
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}

#endif
