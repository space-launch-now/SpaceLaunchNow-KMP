package me.calebjones.spacelaunchnow.analytics.events

/**
 * Where a paywall presentation came from. [value] is the `source` parameter on every
 * paywall and purchase funnel event (view, tier selected, dismissed, purchase started /
 * completed / failed / restored). A new placement adds a value here, not a new event.
 */
enum class PaywallSource(val value: String) {
    /** Opened with no specific entry (default for the route). */
    SUPPORT_US("support_us"),
    ONBOARDING("onboarding"),
    HOME_HEART("home_heart"),
    HOME_REMOVE_ADS("home_remove_ads"),
    LAUNCH_DETAIL_REMOVE_ADS("launch_detail_remove_ads"),
    THEME_PROMPT("theme_prompt"),
    THEME_WIDGET_APPLY("theme_widget_apply"),
    THEME_RESET("theme_reset"),
    NOTIFICATION_SETTINGS("notification_settings"),
    CALENDAR_SYNC("calendar_sync"),
    SETTINGS_ROW("settings_row"),
    WIDGET_LOCK("widget_lock"),
    /** A free user tapped a locked notification control (flag `paywall_locked_notification_tap_enabled`). */
    LOCKED_NOTIFICATION_CONTROL("locked_notification_control"),
    WINBACK("winback");
}
