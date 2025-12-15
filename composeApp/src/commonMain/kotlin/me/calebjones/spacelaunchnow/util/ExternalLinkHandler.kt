package me.calebjones.spacelaunchnow.util

/**
 * Platform-specific implementation for opening external links.
 */
expect object ExternalLinkHandler {
    /**
     * Opens an email client with pre-filled recipient and subject.
     * 
     * @param recipient Email address
     * @param subject Email subject line
     * @param body Email body content (optional)
     */
    fun openEmail(recipient: String, subject: String, body: String = "")
    
    /**
     * Opens a URL in the default browser.
     * 
     * @param url The URL to open
     */
    fun openUrl(url: String)
}
