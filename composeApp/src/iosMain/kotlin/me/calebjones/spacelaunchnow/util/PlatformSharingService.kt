package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import platform.Foundation.NSNotificationCenter

/**
 * iOS-specific sharing implementation that bridges to Swift's ShareHelper.
 * 
 * This uses NSNotificationCenter to communicate with Swift code because:
 * 1. Kotlin/Native UIKit bindings don't expose popoverPresentationController
 * 2. UIActivityViewController on iPad requires popover configuration
 * 3. Swift can properly handle the UIKit APIs
 * 
 * The ShareHelper.swift in the iOS app receives these notifications and
 * presents the share sheet with proper iPad popover support.
 */
class IOSSharingService : LaunchSharingService {
    
    companion object {
        // Notification names must match ShareHelper.swift
        private const val SHARE_TEXT_NOTIFICATION = "SpaceLaunchNow.ShareText"
        private const val SHARE_URL_NOTIFICATION = "SpaceLaunchNow.ShareURL"
    }
    
    override suspend fun shareLaunch(launch: LaunchNormal) {
        val shareText = SharingFormatUtil.formatLaunchShareText(launch)
        shareText(shareText)
    }
    
    override suspend fun shareUrl(url: String, text: String?) {
        val shareContent = if (text != null) {
            "$text\n$url"
        } else {
            url
        }
        shareText(shareContent)
    }
    
    private fun shareText(text: String) {
        // Post notification to Swift ShareHelper
        NSNotificationCenter.defaultCenter.postNotificationName(
            aName = SHARE_TEXT_NOTIFICATION,
            `object` = null,
            userInfo = mapOf("text" to text)
        )
    }
}

/**
 * Factory function to create iOS sharing service
 */
actual fun createPlatformSharingService(): LaunchSharingService {
    return IOSSharingService()
}
