package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.PlatformType
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.platform.AppEnvironmentInfo
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Builds and pushes the RevenueCat subscriber-attribute map.
 *
 * - `pushSnapshot()` reads providers synchronously and pushes one map.
 *   Used at app cold-start.
 * - `start(scope, ...)` collects flows and pushes deltas with a 1s debounce.
 *
 * Provider lambdas decouple the syncer from concrete repositories so it
 * remains unit-testable without a Koin graph.
 */
class RevenueCatAttributesSyncer(
    private val attributes: RevenueCatAttributes,
    private val envInfo: AppEnvironmentInfo,
    private val subscriptionStateProvider: () -> String,
    private val themeModeProvider: () -> String,
    private val hasCustomThemeProvider: () -> Boolean,
    private val grantsTotalProvider: () -> Long,
    private val adsShownTotalProvider: () -> Long,
    private val tempAccessActiveProvider: () -> Boolean,
) {
    private val log = logger()
    @Volatile private var started = false

    fun pushSnapshot() {
        val map = buildMap()
        attributes.set(map)
        log.i { "RC snapshot pushed (${map.size} attrs)" }
    }

    @OptIn(FlowPreview::class)
    fun start(
        scope: CoroutineScope,
        subscriptionStateFlow: Flow<String>,
        themeModeFlow: Flow<String>,
        hasCustomThemeFlow: Flow<Boolean>,
        grantsTotalFlow: Flow<Long>,
        adsShownTotalFlow: Flow<Long>,
        tempAccessActiveFlow: Flow<Boolean>,
    ) {
        if (started) {
            log.w { "RC syncer already started — ignoring duplicate start() call" }
            return
        }
        started = true

        // Initial push runs on the scope's dispatcher so providers that
        // block (e.g., runBlocking { flow.first() } from the Koin wiring)
        // never stall the caller's thread (matters for iOS main-thread).
        scope.launch { pushSnapshot() }

        // Debounced delta pusher.
        combine(
            subscriptionStateFlow.distinctUntilChanged(),
            themeModeFlow.distinctUntilChanged(),
            hasCustomThemeFlow.distinctUntilChanged(),
            grantsTotalFlow.distinctUntilChanged(),
            adsShownTotalFlow.distinctUntilChanged(),
            tempAccessActiveFlow.distinctUntilChanged(),
        ) { _ -> Unit }
            .debounce(1_000)
            .onEach { pushSnapshot() }
            .launchIn(scope)
    }

    private fun buildMap(): Map<String, String?> = mapOf(
        "app_version" to envInfo.appVersionName,
        "app_build" to envInfo.appBuildNumber,
        "platform" to platformString(),
        "os_version" to envInfo.osVersion,
        "device_model" to envInfo.deviceModel,
        "locale" to envInfo.locale,
        "country" to envInfo.country.ifEmpty { null },
        "form_factor" to envInfo.formFactor,
        "subscription_state" to subscriptionStateProvider(),
        "theme_mode" to themeModeProvider(),
        "has_custom_theme" to hasCustomThemeProvider().toString(),
        "temporary_access_active" to tempAccessActiveProvider().toString(),
        "temporary_access_grants_total" to grantsTotalProvider().toString(),
        "rewarded_ads_shown_total" to adsShownTotalProvider().toString(),
    )

    private fun platformString(): String = when (getPlatform().type) {
        PlatformType.ANDROID -> "android"
        PlatformType.IOS -> "ios"
        PlatformType.DESKTOP -> "desktop"
    }
}
