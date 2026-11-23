package me.calebjones.spacelaunchnow.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.calebjones.spacelaunchnow.PlatformType
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.model.SubscriptionState
import me.calebjones.spacelaunchnow.getPlatform

/**
 * Mirrors the conversion-funnel dimensions (spec 014 FR-4) to Firebase user
 * properties so ALL events are segmentable, not just funnel steps.
 *
 * App-scoped like [me.calebjones.spacelaunchnow.data.billing.RevenueCatAttributesSyncer],
 * and deliberately NOT a ViewModel collector: a per-ViewModel infinite collect
 * duplicates per instance and — because unit tests never clear ViewModels —
 * leaks across tests and trips kotlinx-coroutines-test's Dispatchers.Main
 * concurrency guard in unrelated test classes.
 */
class FunnelUserPropertySyncer(
    private val analyticsManager: AnalyticsManager,
    private val billingManager: BillingManager,
    private val appLanguageTag: () -> String = ::getPlatformAppLanguageTag,
) {

    fun start(scope: CoroutineScope, subscriptionStateFlow: Flow<SubscriptionState>) {
        analyticsManager.setUserProperty("platform", platformName())
        syncAppLanguage()
        subscriptionStateFlow
            .onEach { state ->
                analyticsManager.setUserProperty(
                    "subscription_type",
                    state.subscriptionType.name.lowercase()
                )
                analyticsManager.setUserProperty("is_trial", state.isInTrialPeriod.toString())
                analyticsManager.setUserProperty(
                    "active_entitlements",
                    billingManager.getActiveEntitlements().sorted().joinToString(",")
                )
            }
            .launchIn(scope)
    }

    /**
     * Sets `app_language` to the UI language the app shows (not the raw device
     * locale). Call at start and whenever the app language changes.
     */
    fun syncAppLanguage() {
        analyticsManager.setUserProperty("app_language", AppLanguage.resolve(appLanguageTag()))
    }

    private fun platformName(): String = when (getPlatform().type) {
        PlatformType.ANDROID -> "android"
        PlatformType.IOS -> "ios"
        PlatformType.DESKTOP -> "desktop"
    }
}
