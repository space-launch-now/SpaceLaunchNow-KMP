package me.calebjones.spacelaunchnow.wear.data

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.wear.data.model.WearEntitlementState

interface EntitlementSyncManager {
    val entitlementState: Flow<WearEntitlementState>
    suspend fun isWearOsPremium(): Boolean
    suspend fun onEntitlementReceived(active: Boolean, expiresAt: Instant?)
    suspend fun requestSync()
}
