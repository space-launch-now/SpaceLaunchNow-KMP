package me.calebjones.spacelaunchnow.rating

/**
 * Pure rule for the review prompt shown after a user opens a launch from a notification.
 * No I/O: the caller reads preferences and the clock and passes them in.
 */
object NotificationRatingRule {
    /** The user must have opened the app at least this many times. Also blocks the first session. */
    const val MIN_LAUNCH_COUNT = 5L

    /**
     * Days that must pass since the last prompt. This flow and the enjoyment-dialog flow share the
     * last-prompt date key, but this rule deliberately uses 120 days while AppRatingViewModel uses 90
     * (task requirement).
     */
    const val COOLDOWN_DAYS = 120L
    const val COOLDOWN_MS = COOLDOWN_DAYS * 24L * 60L * 60L * 1000L

    /** LL2 launch status ids that may prompt: Go, Success, In Flight. */
    val ALLOWED_STATUS_IDS: Set<Int> = setOf(1, 3, 6)

    data class Input(
        val launchCount: Long,
        val lastPromptAtMs: Long?,
        val nowMs: Long,
        val hasRated: Boolean,
        val statusId: Int?,
        val flagEnabled: Boolean,
    )

    enum class Decision(val reason: String) {
        SHOW("all conditions met"),
        BLOCKED_FLAG_OFF("remote config flag is off"),
        BLOCKED_TOO_FEW_LAUNCHES("app launch count is below the minimum"),
        BLOCKED_COOLDOWN("a prompt was requested within the cooldown"),
        BLOCKED_ALREADY_RATED("user has already rated"),
        BLOCKED_STATUS("launch status is not Go, Success or In Flight"),
    }

    fun evaluate(input: Input): Decision = when {
        !input.flagEnabled -> Decision.BLOCKED_FLAG_OFF
        input.launchCount < MIN_LAUNCH_COUNT -> Decision.BLOCKED_TOO_FEW_LAUNCHES
        input.hasRated -> Decision.BLOCKED_ALREADY_RATED
        input.lastPromptAtMs != null && input.nowMs - input.lastPromptAtMs < COOLDOWN_MS ->
            Decision.BLOCKED_COOLDOWN
        input.statusId == null || input.statusId !in ALLOWED_STATUS_IDS -> Decision.BLOCKED_STATUS
        else -> Decision.SHOW
    }
}
