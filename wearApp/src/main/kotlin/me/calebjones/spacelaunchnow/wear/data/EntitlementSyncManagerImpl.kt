package me.calebjones.spacelaunchnow.wear.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.wear.data.model.SyncSource
import me.calebjones.spacelaunchnow.wear.data.model.WearEntitlementState
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class EntitlementSyncManagerImpl(
    private val dataStore: DataStore<Preferences>,
    private val context: Context,
) : EntitlementSyncManager {

    private val json = Json { ignoreUnknownKeys = true }

    override val entitlementState: Flow<WearEntitlementState> = dataStore.data.map { prefs ->
        val state = prefs[ENTITLEMENT_STATE_KEY]?.let { raw ->
            try {
                json.decodeFromString<WearEntitlementState>(raw)
            } catch (e: Exception) {
                Logger.w(e) { "Failed to parse cached entitlement state" }
                null
            }
        } ?: WearEntitlementState()

        applyGracePeriod(state)
    }

    override suspend fun isWearOsPremium(): Boolean {
        val state = readCachedState()
        return applyGracePeriod(state).hasWearOs
    }

    override suspend fun onEntitlementReceived(active: Boolean, expiresAt: Instant?) {
        val newState = WearEntitlementState(
            hasWearOs = active,
            lastSyncTimestamp = Clock.System.now(),
            source = SyncSource.PHONE_SYNC,
            expiresAt = expiresAt,
        )
        dataStore.edit { prefs ->
            prefs[ENTITLEMENT_STATE_KEY] = json.encodeToString(WearEntitlementState.serializer(), newState)
        }
        Logger.i { "Entitlement received: active=$active, expiresAt=$expiresAt" }
    }

    override suspend fun requestSync() {
        try {
            val messageClient = Wearable.getMessageClient(context)
            val nodes: List<Node> = suspendCancellableCoroutine { cont ->
                Wearable.getNodeClient(context).connectedNodes
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            for (node in nodes) {
                suspendCancellableCoroutine { cont ->
                    messageClient.sendMessage(node.id, PATH_REQUEST_SYNC, byteArrayOf())
                        .addOnSuccessListener { cont.resume(Unit) }
                        .addOnFailureListener { cont.resumeWithException(it) }
                }
                Logger.d { "Sent sync request to node ${node.displayName}" }
            }
        } catch (e: Exception) {
            Logger.e(e) { "Failed to request entitlement sync from phone" }
        }
    }

    private suspend fun readCachedState(): WearEntitlementState {
        val prefs = dataStore.data.first()
        val raw = prefs[ENTITLEMENT_STATE_KEY] ?: return WearEntitlementState()
        return try {
            json.decodeFromString<WearEntitlementState>(raw)
        } catch (e: Exception) {
            Logger.w(e) { "Failed to parse cached entitlement state" }
            WearEntitlementState()
        }
    }

    private fun applyGracePeriod(state: WearEntitlementState): WearEntitlementState {
        if (!state.hasWearOs) return state
        if (state.source != SyncSource.LOCAL_CACHE) return state

        val now = Clock.System.now()
        val elapsed = now - state.lastSyncTimestamp
        if (elapsed > GRACE_PERIOD) {
            Logger.w { "Grace period expired ($elapsed), revoking cached entitlement" }
            return state.copy(hasWearOs = false)
        }
        return state
    }

    companion object {
        private val ENTITLEMENT_STATE_KEY = stringPreferencesKey("entitlement_state_json")
        private val GRACE_PERIOD = 24.hours
        private const val PATH_REQUEST_SYNC = "/spacelaunchnow/request-sync"
    }
}
