package me.calebjones.spacelaunchnow.ui.onboarding

import me.calebjones.spacelaunchnow.data.model.OnboardingVariant

/** Pager pages, with stable analytics names — `step` indices renumber between variants; `page` does not. */
enum class OnboardingPage(val analyticsName: String) {
    WELCOME("welcome"),
    LAUNCH_CARD("launch_card"),
    NEWS_EVENTS("news_events"),
    WIDGETS("widgets"),
    NOTIFICATION_PERMISSION("notification_permission")
}

fun pagesFor(variant: OnboardingVariant): List<OnboardingPage> = when (variant) {
    OnboardingVariant.CONTROL -> listOf(
        OnboardingPage.WELCOME,
        OnboardingPage.LAUNCH_CARD,
        OnboardingPage.NEWS_EVENTS,
        OnboardingPage.WIDGETS,
        OnboardingPage.NOTIFICATION_PERMISSION
    )
    OnboardingVariant.SHORT -> listOf(
        OnboardingPage.WELCOME,
        OnboardingPage.NOTIFICATION_PERMISSION
    )
}
