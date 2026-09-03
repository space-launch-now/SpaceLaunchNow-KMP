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
        fun fromString(raw: String?): DataBackend =
            entries.find { it.value == raw } ?: TRANTOR
    }
}

/**
 * Resolution order: a non-null local override always wins (debug testing lever); otherwise
 * fall back to the Remote Config value passed in, which itself defaults to TRANTOR when
 * Remote Config is unset, unreadable, or Firebase is unavailable — see
 * RemoteConfigRepository.getDataBackend().
 */
fun resolveDataBackend(override: DataBackend?, remote: DataBackend): DataBackend =
    override ?: remote
