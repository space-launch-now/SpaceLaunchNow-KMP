package me.calebjones.spacelaunchnow.util

/**
 * Trantor contract: the client's agency logo is `social_logo_url`; `logo_url` is its fallback.
 * Returns the URLs to try in order (blank values dropped, duplicates removed), so a caller can
 * advance to the next one when an image fails to load.
 */
fun agencyLogoCandidates(socialLogo: String?, logoUrl: String?): List<String> =
    listOfNotNull(socialLogo, logoUrl).filter { it.isNotBlank() }.distinct()
