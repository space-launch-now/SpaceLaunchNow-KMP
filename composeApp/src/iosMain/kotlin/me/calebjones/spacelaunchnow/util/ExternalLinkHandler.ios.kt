package me.calebjones.spacelaunchnow.util

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * iOS implementation for opening external links.
 */
actual object ExternalLinkHandler {
    
    actual fun openEmail(recipient: String, subject: String, body: String) {
        val subjectEncoded = subject.replace(" ", "%20")
        val bodyEncoded = body.replace(" ", "%20").replace("\n", "%0A")
        val urlString = "mailto:$recipient?subject=$subjectEncoded&body=$bodyEncoded"
        
        NSURL.URLWithString(urlString)?.let { url ->
            if (UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(url)
            }
        }
    }
    
    actual fun openUrl(url: String) {
        NSURL.URLWithString(url)?.let { nsUrl ->
            if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
                UIApplication.sharedApplication.openURL(nsUrl)
            }
        }
    }
}
