package me.calebjones.spacelaunchnow.ui.ads

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepository
import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepositoryImpl
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import kotlin.time.Clock

/** [InterstitialGateStore] backed by the app settings DataStore. */
internal class AppPreferencesInterstitialStore(
    private val prefs: AppPreferences,
) : InterstitialGateStore {
    override suspend fun load() = InterstitialGateState(
        visitCount = prefs.getInterstitialVisitCount(),
        lastShownAtMs = prefs.getInterstitialLastShownMs(),
    )

    override suspend fun save(state: InterstitialGateState) =
        prefs.setInterstitialState(state.visitCount, state.lastShownAtMs)
}

/**
 * Builds the app's gate. Thresholds start at the Remote Config defaults and update from
 * [remoteConfig]; saved counters load in the background, so callers never block.
 */
fun createInterstitialGate(
    prefs: AppPreferences,
    remoteConfig: RemoteConfigRepository,
): InterstitialGate {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    var visits = RemoteConfigRepositoryImpl.DEFAULT_INTERSTITIAL_VISITS
    var minIntervalMs = RemoteConfigRepositoryImpl.DEFAULT_INTERSTITIAL_MIN_INTERVAL_S * 1_000L
    val gate = InterstitialGate(
        visitsBetween = { visits },
        minIntervalMs = { minIntervalMs },
        now = { Clock.System.now().toEpochMilliseconds() },
        store = AppPreferencesInterstitialStore(prefs),
        scope = scope,
        refreshThresholds = {
            visits = remoteConfig.getInterstitialVisits()
            minIntervalMs = remoteConfig.getInterstitialMinIntervalSeconds() * 1_000L
        },
    )
    scope.launch {
        gate.restore()
        visits = remoteConfig.getInterstitialVisits()
        minIntervalMs = remoteConfig.getInterstitialMinIntervalSeconds() * 1_000L
    }
    return gate
}
