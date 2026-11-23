package me.calebjones.spacelaunchnow.analytics

/**
 * Maps a platform locale tag to the language the app UI really shows, for the
 * `app_language` user property (spec 017, T27).
 *
 * The app ships 11 string sets (English plus 10 translations). A device whose
 * language has no set shows English, so the property must say `en`, not the
 * device tag. Keep [SUPPORTED] in step with `locales_config.xml` and the
 * `values-<lang>` folders.
 */
object AppLanguage {
    const val DEFAULT = "en"

    val SUPPORTED: List<String> = listOf(
        "en", "de", "es", "fr", "zh-CN", "nl", "uk", "it", "pl", "pt-BR", "cs"
    )

    // Languages that ship one regional set only: other regions fall back to English.
    private val allowedRegions: Map<String, Set<String>> = mapOf(
        "zh" to setOf("CN", "SG"),
        "pt" to setOf("BR"),
    )

    /** Returns one of [SUPPORTED] for any BCP 47 or POSIX-style tag. */
    fun resolve(tag: String): String {
        val parts = tag.trim().replace('_', '-').split('-').filter { it.isNotEmpty() }
        val language = parts.firstOrNull()?.lowercase() ?: return DEFAULT
        val script = parts.drop(1).firstOrNull { it.length == 4 }?.lowercase()
        val region = parts.drop(1).firstOrNull { it.length == 2 || it.length == 3 && it.all(Char::isDigit) }
            ?.uppercase()

        val match = SUPPORTED.firstOrNull { it.substringBefore('-').equals(language, ignoreCase = true) }
            ?: return DEFAULT
        val allowed = allowedRegions[language]
        if (allowed != null) {
            if (script == "hant") return DEFAULT
            if (region != null && region !in allowed) return DEFAULT
        }
        return match
    }
}

/**
 * The language tag the platform uses to pick the app's UI strings.
 * Android and desktop: the default locale (Android sets it from the per-app language).
 * iOS: the main bundle's best localization (follows the per-app language).
 */
expect fun getPlatformAppLanguageTag(): String
