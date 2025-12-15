package me.calebjones.spacelaunchnow.rating

/**
 * Desktop (JVM) implementation - no-op.
 * Desktop platforms don't have native in-app review APIs.
 */
actual class AppRatingManager {
    
    actual suspend fun requestReview(activity: Any?): Boolean {
        // Desktop doesn't support native in-app reviews
        // Could potentially open browser to a review site, but keeping as no-op
        return false
    }
    
    actual fun canRequestReview(): Boolean {
        // Desktop doesn't support reviews
        return false
    }
}
