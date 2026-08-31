package me.calebjones.spacelaunchnow.data.repository

/**
 * Platform notification permission state as a tristate. [NOT_DETERMINED] means the
 * OS permission dialog has never been answered and can still be shown.
 */
enum class NotificationPermissionStatus {
    GRANTED,
    DENIED,
    NOT_DETERMINED,
}

/**
 * What the UI should do to recover when notifications are not enabled.
 */
enum class NotificationPermissionRemedy {
    NONE,
    REQUEST_PERMISSION,
    OPEN_SYSTEM_SETTINGS,
}

fun NotificationPermissionStatus.remedy(): NotificationPermissionRemedy = when (this) {
    NotificationPermissionStatus.GRANTED -> NotificationPermissionRemedy.NONE
    // The OS dialog has never been answered -- it can still be shown. On iOS the
    // Settings deep link is a dead end in this state (no Notifications row exists
    // until the app has requested authorization once), so request instead.
    NotificationPermissionStatus.NOT_DETERMINED -> NotificationPermissionRemedy.REQUEST_PERMISSION
    NotificationPermissionStatus.DENIED -> NotificationPermissionRemedy.OPEN_SYSTEM_SETTINGS
}
