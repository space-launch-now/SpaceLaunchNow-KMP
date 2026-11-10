package me.calebjones.spacelaunchnow.data.model

/**
 * Which data source the app reads: the original LL-compatible server (SLN-Server) or
 * Terminus. Served by the `data_backend` Firebase Remote Config parameter, with a
 * DebugPreferences local override (debug builds only) for testing either side without a
 * Remote Config change. Resolved once at app start (Koin singleton construction): no hot swap.
 */
enum class DataBackend(val value: String) {
    TERMINUS("terminus"),
    LL("ll");

    companion object {
        /**
         * Fail-safe side of the switch. A missing key, an unparseable value, a failed fetch,
         * or an unavailable Firebase all land here, so production stays on LL until
         * `data_backend` is explicitly set to `terminus`.
         */
        val DEFAULT: DataBackend = LL

        fun fromString(raw: String?): DataBackend =
            entries.find { it.value == raw } ?: DEFAULT
    }
}

/**
 * Resolution order: a non-null local override always wins (debug testing lever); otherwise
 * the Remote Config value passed in, which itself falls back to [DataBackend.DEFAULT].
 */
fun resolveDataBackend(override: DataBackend?, remote: DataBackend): DataBackend =
    override ?: remote
