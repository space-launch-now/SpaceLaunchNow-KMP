package me.calebjones.spacelaunchnow.rating

/**
 * Platform-specific app rating manager that uses native APIs to request app reviews.
 * 
 * Android: Uses Google Play In-App Review API (ReviewManager)
 * iOS: Uses StoreKit's SKStoreReviewController
 * Desktop: No-op implementation
 */
expect class AppRatingManager() {
    /**
     * Requests a review from the user using the platform's native in-app review API.
     * 
     * On Android: Shows the in-app review dialog if available
     * On iOS: Triggers SKStoreReviewController review prompt
     * On Desktop: Does nothing
     * 
     * @param activity The activity context (Android only, ignored on other platforms)
     * @return true if the review request was initiated, false otherwise
     */
    suspend fun requestReview(activity: Any? = null): Boolean
    
    /**
     * Checks if the platform can currently request a review.
     * Some platforms have rate limiting or other restrictions.
     * 
     * @return true if review can be requested, false otherwise
     */
    fun canRequestReview(): Boolean
}
