import Foundation

// MARK: - Widget Access State

/// Cached subscription state read from the shared App Group UserDefaults.
/// Provides fail-safe access determination for the widget extension without requiring
/// the main app process to be running.
struct WidgetAccessState {
    let hasAccess: Bool
    let subscriptionExpiry: Date?
    let lastVerified: Date
    let wasEverPremium: Bool
    let subscriptionType: String

    // MARK: - Fail-Safe Logic

    /// Returns true if the widget should show unlocked content.
    ///
    /// Rules (in priority order):
    /// 1. Explicit access granted → UNLOCKED
    /// 2. Never been premium   → LOCKED
    /// 3. Was premium + expiry in future → UNLOCKED
    /// 4. Was premium + expiry passed   → LOCKED
    /// 5. Was premium + no expiry (lifetime or data gap) → UNLOCKED
    var shouldShowUnlocked: Bool {
        if hasAccess {
            print("🔓 Widget access: explicit access granted")
            return true
        }
        guard wasEverPremium else {
            print("🔒 Widget access: user was never premium")
            return false
        }
        if let expiry = subscriptionExpiry {
            let unlocked = expiry > Date()
            print("🕐 Widget access: expiry check → \(unlocked ? "UNLOCKED" : "LOCKED") (expires \(expiry))")
            return unlocked
        }
        // wasEverPremium is true with no expiry date — lifetime subscription or data gap.
        // Default to unlocked so paying users are never incorrectly locked.
        print("🔓 Widget access: was premium, no expiry stored → unlocked (lifetime or data gap)")
        return true
    }

    // MARK: - Cache Reader

    /// Read cached subscription state from the App Group UserDefaults.
    /// Returns `.locked` if the App Group container is unavailable.
    static func readFromCache() -> WidgetAccessState {
        guard let defaults = UserDefaults(suiteName: "group.me.calebjones.spacelaunchnow") else {
            print("⚠️ Widget: Failed to open App Group UserDefaults — defaulting to locked")
            return .locked
        }

        let hasAccess = defaults.bool(forKey: "widget_has_access")

        let expirySeconds = defaults.double(forKey: "widget_subscription_expiry")
        let subscriptionExpiry: Date? = expirySeconds > 0
            ? Date(timeIntervalSince1970: expirySeconds)
            : nil

        let lastVerifiedSeconds = defaults.double(forKey: "widget_last_verified")
        let lastVerified = lastVerifiedSeconds > 0
            ? Date(timeIntervalSince1970: lastVerifiedSeconds)
            : Date.distantPast

        let wasEverPremium = defaults.bool(forKey: "widget_was_ever_premium")
        let subscriptionType = defaults.string(forKey: "widget_subscription_type") ?? "FREE"

        print(
            "📋 Widget cache: hasAccess=\(hasAccess), wasEverPremium=\(wasEverPremium), type=\(subscriptionType), expiry=\(String(describing: subscriptionExpiry))"
        )

        return WidgetAccessState(
            hasAccess: hasAccess,
            subscriptionExpiry: subscriptionExpiry,
            lastVerified: lastVerified,
            wasEverPremium: wasEverPremium,
            subscriptionType: subscriptionType
        )
    }

    // MARK: - Defaults

    /// Safe default used when the App Group container cannot be opened.
    static let locked = WidgetAccessState(
        hasAccess: false,
        subscriptionExpiry: nil,
        lastVerified: Date.distantPast,
        wasEverPremium: false,
        subscriptionType: "FREE"
    )
}
