package me.calebjones.spacelaunchnow.widgets

import platform.Foundation.NSUserDefaults

actual object WidgetAccessSharer {
    private const val SUITE_NAME = "group.me.calebjones.spacelaunchnow"

    actual fun syncWidgetAccess(hasAccess: Boolean) {
        syncWidgetAccessCache(WidgetAccessCache(hasAccess = hasAccess))
    }

    actual fun syncWidgetAccessCache(cache: WidgetAccessCache) {
        val defaults = NSUserDefaults(suiteName = SUITE_NAME) ?: return

        // Legacy key — kept for backward compatibility with older widget builds
        defaults.setBool(cache.hasAccess, forKey = "widget_has_access")

        // Subscription expiry: stored as Unix seconds (Double) for Swift compatibility
        val expirySeconds = cache.subscriptionExpiryMs?.let { it.toDouble() / 1000.0 }
        if (expirySeconds != null) {
            defaults.setDouble(expirySeconds, forKey = "widget_subscription_expiry")
        } else {
            defaults.removeObjectForKey("widget_subscription_expiry")
        }

        // Last verified timestamp (seconds)
        defaults.setDouble(
            cache.lastVerifiedMs.toDouble() / 1000.0,
            forKey = "widget_last_verified"
        )

        // Sticky flag: once true, never written back as false
        if (cache.wasEverPremium) {
            defaults.setBool(true, forKey = "widget_was_ever_premium")
        }

        // Subscription type string
        defaults.setObject(cache.subscriptionType.name, forKey = "widget_subscription_type")
    }
}
