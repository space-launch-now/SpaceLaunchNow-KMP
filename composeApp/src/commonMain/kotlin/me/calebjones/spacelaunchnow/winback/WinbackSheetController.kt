package me.calebjones.spacelaunchnow.winback

/**
 * Decides whether the win-back sheet may open and records that it did. The host composable
 * calls [isEligible] once per app open and [markShown] when the sheet appears, so a sheet
 * that was shown is never offered again, whatever the user does next.
 */
class WinbackSheetController(
    private val flagEnabled: suspend () -> Boolean,
    private val onboardingDismissedAtMs: suspend () -> Long?,
    private val alreadyShown: suspend () -> Boolean,
    private val saveShown: suspend () -> Unit,
    private val nowMs: () -> Long,
) {
    /** [isSubscribed] is read by the caller at the moment of the check, from live subscription state. */
    suspend fun isEligible(isSubscribed: Boolean): Boolean =
        WinbackSheetRule.evaluate(
            WinbackSheetRule.Input(
                flagEnabled = flagEnabled(),
                onboardingDismissedAtMs = onboardingDismissedAtMs(),
                alreadyShown = alreadyShown(),
                isSubscribed = isSubscribed,
                nowMs = nowMs(),
            )
        ) == WinbackSheetRule.Decision.SHOW

    suspend fun markShown() = saveShown()
}
