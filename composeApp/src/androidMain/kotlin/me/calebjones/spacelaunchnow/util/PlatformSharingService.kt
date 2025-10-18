package me.calebjones.spacelaunchnow.util

import android.content.Context
import android.content.Intent
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal

/**
 * Android-specific sharing implementation using native Android sharing intents
 */
class AndroidSharingService(private val context: Context) : LaunchSharingService {
    
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
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "Space Launch Information")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        val chooserIntent = Intent.createChooser(intent, "Share launch info via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        context.startActivity(chooserIntent)
    }
}

/**
 * Factory function to create Android sharing service
 */
actual fun createPlatformSharingService(): LaunchSharingService {
    throw IllegalStateException("Android sharing service requires context. Use AndroidSharingService(context) directly.")
}