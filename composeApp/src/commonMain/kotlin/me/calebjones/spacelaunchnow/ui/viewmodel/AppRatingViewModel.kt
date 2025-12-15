package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.rating.AppRatingManager
import kotlin.time.Clock

/**
 * ViewModel that manages app rating logic and triggers.
 *
 * Trigger conditions:
 * - Minimum 10 app launches
 * - At least 7 days since first install (tracked by first launch)
 * - 90 days cooldown between prompts
 * - User hasn't already rated
 */
class AppRatingViewModel(
    private val appRatingManager: AppRatingManager,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val log = Logger.withTag("AppRatingViewModel")

    // First show the "Are you enjoying?" dialog
    private val _shouldShowEnjoymentDialog = MutableStateFlow(false)
    val shouldShowEnjoymentDialog: StateFlow<Boolean> = _shouldShowEnjoymentDialog.asStateFlow()

    // Then show feedback dialog if user says no
    private val _shouldShowFeedbackDialog = MutableStateFlow(false)
    val shouldShowFeedbackDialog: StateFlow<Boolean> = _shouldShowFeedbackDialog.asStateFlow()

    // Trigger native review if user says yes
    private val _shouldShowNativeReview = MutableStateFlow(false)
    val shouldShowNativeReview: StateFlow<Boolean> = _shouldShowNativeReview.asStateFlow()

    companion object {
        private const val MIN_LAUNCHES = 7L
        private const val INITIAL_DELAY_DAYS = 7L
        private const val COOLDOWN_DAYS = 90L
        private const val FEEDBACK_COOLDOWN_DAYS = 30L // Shorter cooldown if they gave feedback
        private const val MILLIS_PER_DAY = 86400000L
    }

    init {
        viewModelScope.launch {
            checkRatingConditions()
        }
    }

    /**
     * Increments the app launch counter.
     * Call this when the app starts or resumes.
     */
    fun recordAppLaunch() {
        viewModelScope.launch {
            val previousCount = appPreferences.getAppLaunchCount()
            appPreferences.incrementLaunchCount()
            val newCount = previousCount + 1

            // Log detailed app rating state at INFO level for visibility
            val hasRated = appPreferences.hasUserRated()
            val shownCount = appPreferences.getRatingDialogShownCount()
            val lastPromptDate = appPreferences.getLastRatingPromptDate()
            val daysSinceLastPrompt = if (lastPromptDate != null) {
                (Clock.System.now().toEpochMilliseconds() - lastPromptDate) / MILLIS_PER_DAY
            } else {
                null
            }

            log.i {
                "=== APP RATING STATE ===" +
                        "\n  Launch Count: $newCount (min: $MIN_LAUNCHES)" +
                        "\n  User Has Rated: $hasRated" +
                        "\n  Times Shown: $shownCount" +
                        "\n  Last Prompt: ${if (lastPromptDate != null) "$daysSinceLastPrompt days ago" else "never"}" +
                        "\n  Cooldown: $COOLDOWN_DAYS days" +
                        "\n  Will Show: ${newCount >= MIN_LAUNCHES && !hasRated && (daysSinceLastPrompt == null || daysSinceLastPrompt >= COOLDOWN_DAYS)}" +
                        "\n========================"
            }

            checkRatingConditions()
        }
    }

    /**
     * Checks if all conditions are met to show the rating prompt.
     */
    private suspend fun checkRatingConditions() {

        // Don't show if user already rated
        if (appPreferences.hasUserRated()) {
            log.d { "User has already rated, not showing prompt" }
            return
        }

        // Check minimum launches
        val launchCount = appPreferences.getAppLaunchCount()
        if (launchCount < MIN_LAUNCHES) {
            log.d { "Not enough launches: $launchCount/$MIN_LAUNCHES" }
            return
        }

        // Check initial delay (assume first launch was when counter started)
        // For simplicity, we'll check if we have enough launches as proxy for time
        // More sophisticated: track first install date separately

        // Check cooldown since last prompt (shorter for users who gave feedback)
        val lastPromptDate = appPreferences.getLastRatingPromptDate()
        if (lastPromptDate != null) {
            val daysSinceLastPrompt =
                (Clock.System.now().toEpochMilliseconds() - lastPromptDate) / MILLIS_PER_DAY
            val gaveFeedback = appPreferences.hasUserGivenFeedback()
            val requiredCooldown = if (gaveFeedback) FEEDBACK_COOLDOWN_DAYS else COOLDOWN_DAYS
            
            if (daysSinceLastPrompt < requiredCooldown) {
                log.d { "Cooldown active: $daysSinceLastPrompt/$requiredCooldown days (feedback user: $gaveFeedback)" }
                return
            }
        }

        // All conditions met - show enjoyment dialog first
        log.i { "✅ App rating conditions met! Showing enjoyment dialog" }
        _shouldShowEnjoymentDialog.value = true
    }

    /**
     * User confirmed they're enjoying the app - trigger native review.
     */
    fun onUserEnjoyingApp() {
        log.i { "User enjoying app - triggering native review" }
        _shouldShowEnjoymentDialog.value = false
        _shouldShowNativeReview.value = true
    }

    /**
     * User indicated they're not enjoying the app - show feedback dialog.
     * Note: We don't update the prompt date here - only when they actually send feedback.
     * This way if they dismiss without sending, we can ask sooner.
     */
    fun onUserNotEnjoyingApp() {
        log.i { "User not enjoying app - showing feedback dialog" }
        _shouldShowEnjoymentDialog.value = false
        _shouldShowFeedbackDialog.value = true
    }

    /**
     * User chose "Not Now" - dismiss and ask later.
     */
    fun onNotNow() {
        log.i { "User chose 'Not Now' - will ask again later" }
        _shouldShowEnjoymentDialog.value = false
        // Don't update last prompt date, so we can ask sooner
    }

    /**
     * User dismissed the enjoyment dialog.
     */
    fun dismissEnjoymentDialog() {
        log.i { "Enjoyment dialog dismissed" }
        _shouldShowEnjoymentDialog.value = false
        // Treat same as "Not Now"
    }

    /**
     * User dismissed the feedback dialog without sending feedback.
     * Don't update cooldown so we can ask again sooner.
     */
    fun dismissFeedbackDialog() {
        log.i { "Feedback dialog dismissed without sending - will ask again sooner" }
        _shouldShowFeedbackDialog.value = false
        // Don't update last prompt date - they didn't engage
    }

    /**
     * User sent feedback via email or GitHub.
     * Mark as feedback user for shorter cooldown (30 days instead of 90).
     * This lets us follow up to see if their issues were resolved.
     */
    fun onFeedbackSent() {
        log.i { "User sent feedback - setting 30-day cooldown to follow up" }
        _shouldShowFeedbackDialog.value = false
        viewModelScope.launch {
            appPreferences.setUserGaveFeedback(true)
            appPreferences.updateLastRatingPromptDate(
                Clock.System.now().toEpochMilliseconds()
            )
        }
    }

    /**
     * Requests the native in-app review.
     * Call this when ready to show the rating prompt.
     * 
     * @param activity The activity context (required for Android, ignored on other platforms)
     */
    fun requestReview(activity: Any? = null) {
        viewModelScope.launch {
            if (appRatingManager.canRequestReview()) {
                log.i { "🌟 Requesting native in-app review prompt..." }

                val success = appRatingManager.requestReview(activity)

                if (success) {
                    // Update tracking
                    appPreferences.updateLastRatingPromptDate(
                        Clock.System.now().toEpochMilliseconds()
                    )
                    appPreferences.incrementRatingDialogShownCount()
                    val totalShown = appPreferences.getRatingDialogShownCount()
                    log.i { "✅ Native review prompt shown successfully (total times: $totalShown)" }
                } else {
                    log.w { "❌ Native review prompt request failed (platform may have rejected)" }
                }

                // Reset state regardless of success
                _shouldShowNativeReview.value = false
            } else {
                log.w { "⏸️ Cannot request review at this time (platform rate limited)" }
            }
        }
    }

    /**
     * Marks that the user has rated the app.
     * Call this if you have external confirmation that user left a review.
     */
    fun markUserHasRated() {
        viewModelScope.launch {
            appPreferences.setUserHasRated(true)
            _shouldShowEnjoymentDialog.value = false
            _shouldShowNativeReview.value = false
            log.d { "User marked as having rated the app" }
        }
    }

    /**
     * Manually shows the feedback dialog.
     * Used for the "Send Feedback" button in Settings.
     */
    fun showFeedbackDialog() {
        log.i { "Manually showing feedback dialog from settings" }
        _shouldShowFeedbackDialog.value = true
    }
}
