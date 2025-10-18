package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.Foundation.NSString
import platform.Foundation.create
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS-specific sharing implementation using UIActivityViewController
 */
@OptIn(ExperimentalForeignApi::class)
class IOSSharingService : LaunchSharingService {
    
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
        val nsString = NSString.create(string = text)
        val activityItems = listOf(nsString)
        
        val activityViewController = UIActivityViewController(
            activityItems = activityItems,
            applicationActivities = null
        )
        
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null
        )
    }
}

/**
 * Factory function to create iOS sharing service
 */
actual fun createPlatformSharingService(): LaunchSharingService {
    return IOSSharingService()
}