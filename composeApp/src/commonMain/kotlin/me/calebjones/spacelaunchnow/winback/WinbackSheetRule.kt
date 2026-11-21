package me.calebjones.spacelaunchnow.winback

/**
 * Pure rule for the day-7 win-back sheet. No I/O: the caller reads preferences, the flag
 * and the clock, and passes them in.
 */
object WinbackSheetRule {
    /** Days after the onboarding paywall was dismissed (the install session) before the sheet may show. */
    const val DELAY_DAYS = 7L
    const val DELAY_MS = DELAY_DAYS * 24L * 60L * 60L * 1000L

    data class Input(
        val flagEnabled: Boolean,
        /** Epoch ms of the first onboarding paywall dismissal, or null if the user never dismissed it. */
        val onboardingDismissedAtMs: Long?,
        val alreadyShown: Boolean,
        val isSubscribed: Boolean,
        val nowMs: Long,
    )

    enum class Decision(val reason: String) {
        SHOW("all conditions met"),
        BLOCKED_FLAG_OFF("remote config flag is off"),
        BLOCKED_SUBSCRIBED("user is subscribed"),
        BLOCKED_ALREADY_SHOWN("the sheet was already shown"),
        BLOCKED_NOT_DISMISSED("user did not dismiss the onboarding paywall"),
        BLOCKED_TOO_EARLY("fewer than 7 days since the onboarding paywall was dismissed"),
    }

    fun evaluate(input: Input): Decision = when {
        !input.flagEnabled -> Decision.BLOCKED_FLAG_OFF
        input.isSubscribed -> Decision.BLOCKED_SUBSCRIBED
        input.alreadyShown -> Decision.BLOCKED_ALREADY_SHOWN
        input.onboardingDismissedAtMs == null -> Decision.BLOCKED_NOT_DISMISSED
        input.nowMs - input.onboardingDismissedAtMs < DELAY_MS -> Decision.BLOCKED_TOO_EARLY
        else -> Decision.SHOW
    }
}
