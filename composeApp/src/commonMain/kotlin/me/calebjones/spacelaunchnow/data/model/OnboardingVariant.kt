package me.calebjones.spacelaunchnow.data.model

/**
 * Onboarding flow variant for the shortened-onboarding A/B test
 * (docs/superpowers/specs/2026-08-24-onboarding-ab-test-design.md).
 * Served by the `onboarding_variant` Remote Config parameter.
 */
enum class OnboardingVariant(val value: String) {
    CONTROL("control"),
    SHORT("short");

    companion object {
        fun fromString(raw: String?): OnboardingVariant =
            entries.find { it.value == raw } ?: CONTROL
    }
}

/**
 * The variant actually shown must never change mid-flow: first resolution wins
 * and is persisted; later Remote Config updates are ignored for this install.
 */
suspend fun resolveOnboardingVariant(
    persisted: String?,
    fetchRemote: suspend () -> OnboardingVariant,
    persist: suspend (String) -> Unit
): OnboardingVariant {
    persisted?.let { return OnboardingVariant.fromString(it) }
    val resolved = fetchRemote()
    persist(resolved.value)
    return resolved
}
