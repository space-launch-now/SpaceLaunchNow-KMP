package me.calebjones.spacelaunchnow.sync

import android.content.Context
import co.touchlab.kermit.Logger
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.domain.model.Launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Clock

/**
 * Phone-side sync payload models matching the watch-side DataLayerSyncPayload format.
 * Duplicated here because the phone app cannot depend on the wearApp module.
 */
@Serializable
private data class PhoneSyncPayload(
    val launches: List<PhoneSyncLaunch>,
    val entitlementActive: Boolean,
    val syncTimestamp: String,
    val phoneAppVersion: String,
    val followAllLaunches: Boolean = true,
    val agencyIds: List<Int>? = null,
    val locationIds: List<Int>? = null,
)

@Serializable
private data class PhoneSyncLaunch(
    val id: String,
    val name: String,
    val net: String,
    val statusAbbrev: String? = null,
    val statusName: String? = null,
    val lspName: String? = null,
    val lspAbbrev: String? = null,
    val rocketConfigName: String? = null,
    val missionName: String? = null,
    val missionDescription: String? = null,
    val padLocationName: String? = null,
    val imageUrl: String? = null,
)

interface PhoneDataLayerSync {
    suspend fun syncToWatch()
    suspend fun syncEntitlementToWatch(active: Boolean, expiresAt: Instant?)
}

class PhoneDataLayerService(
    private val context: Context,
    private val launchRepository: LaunchRepository,
    private val notificationStateStorage: NotificationStateStorage,
    private val launchFilterService: LaunchFilterService,
) : PhoneDataLayerSync {

    private val log = Logger.withTag("PhoneDataLayerService")
    private val json = Json { ignoreUnknownKeys = true }
    private val dataClient by lazy { Wearable.getDataClient(context) }

    override suspend fun syncToWatch() {
        try {
            log.d { "Syncing launch data to watch" }

            // Read filter preferences from phone settings
            val notificationState = notificationStateStorage.stateFlow.first()
            val filterParams = launchFilterService.getFilterParams(notificationState)
            log.d { "Wear sync filter params - followAll: ${notificationState.followAllLaunches}, agencyIds: ${filterParams.agencyIds}, locationIds: ${filterParams.locationIds}" }

            val result = launchRepository.getUpcomingLaunchesNormalDomain(
                limit = 20,
                agencyIds = filterParams.agencyIds,
                locationIds = filterParams.locationIds,
            )
            val launchList = result.getOrThrow()

            val syncLaunches = launchList.data.results.map { it.toSyncLaunch() }

            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val payload = PhoneSyncPayload(
                launches = syncLaunches,
                entitlementActive = false, // Entitlement set separately via syncEntitlementToWatch()
                syncTimestamp = Clock.System.now().toString(),
                phoneAppVersion = packageInfo.versionName ?: "unknown",
                followAllLaunches = notificationState.followAllLaunches,
                agencyIds = filterParams.agencyIds,
                locationIds = filterParams.locationIds,
            )

            val payloadJson = json.encodeToString(PhoneSyncPayload.serializer(), payload)
            val putDataMapReq = PutDataMapRequest.create(PATH_SYNC)
            putDataMapReq.dataMap.putByteArray("payload", payloadJson.toByteArray())
            val putDataReq = putDataMapReq.asPutDataRequest().setUrgent()

            withContext(Dispatchers.IO) {
                Tasks.await(dataClient.putDataItem(putDataReq))
            }
            log.i { "Successfully synced ${syncLaunches.size} launches to watch (filtered: ${!notificationState.followAllLaunches})" }
        } catch (e: Exception) {
            log.e(e) { "Failed to sync launch data to watch" }
        }
    }

    override suspend fun syncEntitlementToWatch(active: Boolean, expiresAt: Instant?) {
        try {
            val request = PutDataMapRequest.create(PATH_ENTITLEMENT).apply {
                dataMap.putBoolean(KEY_ACTIVE, active)
                dataMap.putString(KEY_EXPIRES_AT, expiresAt?.toString() ?: "")
                dataMap.putString(KEY_TIMESTAMP, Clock.System.now().toString())
            }.asPutDataRequest().setUrgent()

            suspendCancellableCoroutine { cont ->
                dataClient.putDataItem(request)
                    .addOnSuccessListener { cont.resume(Unit) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
            Logger.i { "Synced entitlement to watch: active=$active, expiresAt=$expiresAt" }
        } catch (e: Exception) {
            Logger.e(e) { "Failed to sync entitlement to watch" }
            throw e
        }
    }

    companion object {
        const val PATH_SYNC = "/spacelaunchnow/sync"
        const val PATH_REQUEST_SYNC = "/spacelaunchnow/request-sync"
        private const val PATH_ENTITLEMENT = "/spacelaunchnow/entitlement"
        private const val KEY_ACTIVE = "active"
        private const val KEY_EXPIRES_AT = "expiresAt"
        private const val KEY_TIMESTAMP = "timestamp"
    }
}

private fun Launch.toSyncLaunch(): PhoneSyncLaunch {
    return PhoneSyncLaunch(
        id = id,
        name = name,
        net = net?.toString() ?: "",
        statusAbbrev = status?.abbrev,
        statusName = status?.name,
        lspName = provider.name,
        lspAbbrev = provider.abbrev,
        rocketConfigName = rocket?.fullName ?: rocket?.name,
        missionName = mission?.name,
        missionDescription = mission?.description,
        padLocationName = pad?.location?.name,
        imageUrl = imageUrl,
    )
}
