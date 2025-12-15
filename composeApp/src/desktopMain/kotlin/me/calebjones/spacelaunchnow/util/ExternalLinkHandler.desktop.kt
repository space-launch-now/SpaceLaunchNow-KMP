package me.calebjones.spacelaunchnow.util

import java.awt.Desktop
import java.net.URI

/**
 * Desktop implementation for opening external links.
 */
actual object ExternalLinkHandler {
    
    actual fun openEmail(recipient: String, subject: String, body: String) {
        val subjectEncoded = URI("", "", subject, null).rawFragment ?: subject
        val bodyEncoded = URI("", "", body, null).rawFragment ?: body
        val mailtoUri = URI("mailto:$recipient?subject=$subjectEncoded&body=$bodyEncoded")
        
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().mail(mailtoUri)
        }
    }
    
    actual fun openUrl(url: String) {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(url))
        }
    }
}
