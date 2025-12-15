package me.calebjones.spacelaunchnow.rating

import android.app.Activity
import co.touchlab.kermit.Logger
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation using Google Play In-App Review API.
 * Shows the native in-app review dialog within the app.
 */
actual class AppRatingManager {
    
    private val log = Logger.withTag("AppRatingManager.Android")
    
    actual suspend fun requestReview(activity: Any?): Boolean {
        log.i { "requestReview called with activity: ${activity?.javaClass?.simpleName}" }
        
        if (activity !is Activity) {
            log.e { "Activity is null or not an Activity instance! Type: ${activity?.javaClass?.name}" }
            return false
        }
        
        log.i { "Creating ReviewManager for activity: ${activity.javaClass.simpleName}" }
        
        return suspendCancellableCoroutine { continuation ->
            val reviewManager = ReviewManagerFactory.create(activity)
            log.i { "ReviewManager created, requesting review flow..." }
            val request = reviewManager.requestReviewFlow()
            
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    log.i { "Review flow request successful, launching review dialog..." }
                    val reviewInfo = task.result
                    val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                    
                    flow.addOnCompleteListener { flowTask ->
                        // The review flow has finished, regardless of whether the user reviewed or not
                        // Google Play APIs don't indicate if user actually left a review
                        if (flowTask.isSuccessful) {
                            log.i { "✅ Review flow completed successfully" }
                        } else {
                            log.w { "⚠️ Review flow completed but not successful: ${flowTask.exception?.message}" }
                        }
                        continuation.resume(true)
                    }
                } else {
                    // Review request failed
                    log.e { "❌ Review request failed: ${task.exception?.message}" }
                    continuation.resume(false)
                }
            }
            
            continuation.invokeOnCancellation {
                // Handle cancellation if needed
            }
        }
    }
    
    actual fun canRequestReview(): Boolean {
        // Google Play In-App Review has its own rate limiting
        // We can always attempt to request, the API will handle rate limiting
        return true
    }
}
