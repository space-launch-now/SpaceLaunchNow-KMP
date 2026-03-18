package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.util.logging.logger
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

/**
 * Desktop-specific sharing implementation that copies text to clipboard
 */
class DesktopSharingService : LaunchSharingService {
    private val log = logger()
    
    /**
     * Shares a launch by copying formatted text to clipboard
     */
    override suspend fun shareLaunch(launch: LaunchNormal) {
        val shareText = SharingFormatUtil.formatLaunchShareText(launch)
        copyToClipboard(shareText)
        
        // Optional: Show a system notification that text was copied
        log.i { "Launch info copied to clipboard: ${launch.name}" }
    }
    
    /**
     * Shares a URL by copying to clipboard and optionally opening in browser
     */
    override suspend fun shareUrl(url: String, text: String?) {
        shareUrlWithBrowserOption(url, text, openInBrowser = false)
    }
    
    /**
     * Desktop-specific method to share URL with browser option
     */
    fun shareUrlWithBrowserOption(url: String, text: String? = null, openInBrowser: Boolean = false) {
        val shareContent = if (text != null) {
            "$text\n$url"
        } else {
            url
        }
        
        copyToClipboard(shareContent)
        
        if (openInBrowser && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI(url))
            } catch (e: Exception) {
                log.w(e) { "Failed to open URL in browser: $e" }
            }
        }
        
        log.i { "URL copied to clipboard: $url" }
    }
    
    /**
     * Copies text to system clipboard
     */
    private fun copyToClipboard(text: String) {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val selection = StringSelection(text)
            clipboard.setContents(selection, selection)
        } catch (e: Exception) {
            log.e(e) { "Failed to copy to clipboard: $e" }
        }
    }
}

/**
 * Factory function to create Desktop sharing service
 */
actual fun createPlatformSharingService(): LaunchSharingService {
    return DesktopSharingService()
}