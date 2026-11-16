package me.calebjones.spacelaunchnow.data.model

/**
 * Which data source the app reads: Terminus (the default) or the original LL-compatible
 * server (SLN-Server). Remote Config serves one emergency switch, the boolean
 * `use_legacy_backend` (default `false`); `true` selects [LL]. The old `data_backend` key is
 * not read: builds older than 5.51.0 still read it and must keep seeing `ll`. A
 * DebugPreferences local override (debug builds only) wins over Remote Config. Resolved once at
 * app start (Koin singleton construction): no hot swap.
 */
enum class DataBackend(val value: String) {
    TERMINUS("terminus"),
    LL("ll");

    companion object {
        /**
         * Fail-safe side of the switch. A missing key, an unparseable value, a failed fetch,
         * or an unavailable Firebase all land here, so the app stays on Terminus unless
         * `use_legacy_backend` is explicitly set to `true`.
         */
        val DEFAULT: DataBackend = TERMINUS

        fun parseOrNull(raw: String?): DataBackend? = entries.find { it.value == raw }

        /**
         * Maps the raw `use_legacy_backend` Remote Config value to a backend: only an explicit
         * `true` (any case, surrounding spaces ignored) gives [LL]; anything else gives [DEFAULT].
         */
        fun fromLegacyFlag(raw: String?): DataBackend =
            if (raw?.trim().equals("true", ignoreCase = true)) LL else DEFAULT
    }
}

/**
 * Resolution order: a non-null local override always wins (debug testing lever); otherwise
 * the Remote Config value passed in, which itself falls back to [DataBackend.DEFAULT].
 */
fun resolveDataBackend(override: DataBackend?, remote: DataBackend): DataBackend =
    override ?: remote
