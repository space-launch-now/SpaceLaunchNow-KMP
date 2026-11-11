package me.calebjones.spacelaunchnow.data.model

/**
 * Which Terminus host the app talks to. Prod is the default in every build; debug builds can
 * pick Staging from the Debug Settings Data Source card. Resolved once per Koin graph (no hot
 * swap), like [DataBackend].
 */
enum class TerminusHost(val value: String, val baseUrl: String) {
    PROD("prod", "https://terminus.spacelaunchnow.app"),
    STAGING("staging", "https://staging-terminus.spacelaunchnow.app");

    companion object {
        val DEFAULT: TerminusHost = PROD

        fun parseOrNull(raw: String?): TerminusHost? = entries.find { it.value == raw }

        fun fromBaseUrl(url: String?): TerminusHost? = entries.find { it.baseUrl == url }
    }
}

/** Release builds ignore the stored choice; debug builds follow it, defaulting to prod. */
fun resolveTerminusHost(isDebug: Boolean, stored: TerminusHost?): TerminusHost =
    if (isDebug) stored ?: TerminusHost.DEFAULT else TerminusHost.DEFAULT
