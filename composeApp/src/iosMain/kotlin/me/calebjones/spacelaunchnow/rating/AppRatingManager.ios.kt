package me.calebjones.spacelaunchnow.rating

import platform.StoreKit.SKStoreReviewController

/**
 * iOS implementation using StoreKit's SKStoreReviewController.
 * Shows the native iOS app review prompt.
 */
actual class AppRatingManager {
    
    actual suspend fun requestReview(activity: Any?): Boolean {
        // SKStoreReviewController.requestReview() is a void method
        // It will show the review prompt if conditions are met (rate limiting, etc.)
        // Activity parameter is ignored on iOS
        SKStoreReviewController.requestReview()
        
        // We return true to indicate we attempted the request
        // iOS doesn't provide feedback on whether the prompt was shown or completed
        return true
    }
    
    actual fun canRequestReview(): Boolean {
        // iOS has built-in rate limiting (max 3 times per year automatically)
        // We can always attempt to request, the system will handle rate limiting
        return true
    }
}
