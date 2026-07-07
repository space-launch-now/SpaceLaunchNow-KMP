package me.calebjones.spacelaunchnow.util

import platform.Foundation.NSNotificationCenter

actual fun sharePlainText(text: String, subject: String) {
    // Must match ShareHelper.swift (same channel PlatformSharingService uses).
    NSNotificationCenter.defaultCenter.postNotificationName(
        aName = "SpaceLaunchNow.ShareText",
        `object` = null,
        userInfo = mapOf("text" to text)
    )
}
