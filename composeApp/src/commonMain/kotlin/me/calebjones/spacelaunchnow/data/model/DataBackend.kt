package me.calebjones.spacelaunchnow.data.model

/**
 * Which repository implementation set (Trantor vs. the original LL2 direct client) the app
 * binds against. Production revert lever for the Phase 5 KMP Trantor adoption (amendment
 * 2026-09-02): served by the `data_backend` Firebase Remote Config parameter, with a
 * DebugPreferences local override for testing either side without a Remote Config change.
 * Resolved once at app start (Koin singleton construction) — no hot swap.
 */
enum class DataBackend(val value: String) {
    TRANTOR("trantor"),
    LL("ll");

    companion object {
        /**
         * Fail-safe side of the kill switch. A missing key, an unparseable value, a failed
         * fetch, or an unavailable Firebase all land here, so production stays on LL until
         * `data_backend` is explicitly set to `trantor` (the Phase 6.5 cutover flip).
         */
        val DEFAULT: DataBackend = LL

        fun fromString(raw: String?): DataBackend =
            entries.find { it.value == raw } ?: DEFAULT
    }
}

/**
 * Resolution order: a non-null local override always wins (debug testing lever); otherwise
 * fall back to the Remote Config value passed in, which itself falls back to
 * [DataBackend.DEFAULT] when Remote Config is unset, unreadable, or Firebase is
 * unavailable — see RemoteConfigRepository.getDataBackend().
 */
fun resolveDataBackend(override: DataBackend?, remote: DataBackend): DataBackend =
    override ?: remote
