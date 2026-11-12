package me.calebjones.spacelaunchnow.wear.data

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.calebjones.spacelaunchnow.wear.data.model.CachedLaunch

/**
 * Persisted filter preferences synced from the phone app.
 */
@Serializable
internal data class WatchFilterPreferences(
    val followAllLaunches: Boolean = true,
    val agencyIds: List<Int>? = null,
    val locationIds: List<Int>? = null,
)

/**
 * Direct launch-list calls from the watch (Tier 1). Reads LL or Terminus according to the
 * [WatchBackendConfig] the phone sent. The LL branch is the original request, unchanged; the
 * Terminus branch mirrors the phone's `getUpcomingLaunchesNormalDomain` query.
 */
internal class WatchLaunchApi(private val httpClient: HttpClient) {

    private val log = Logger.withTag("WatchLaunchApi")

    suspend fun fetchUpcoming(
        config: WatchBackendConfig,
        filters: WatchFilterPreferences,
        limit: Int,
    ): List<CachedLaunch> = when (config.backend) {
        WatchDataBackend.LL -> fetchFromLl(filters, limit)
        WatchDataBackend.TERMINUS -> fetchFromTerminus(config.terminusBaseUrl, filters, limit)
    }

    private suspend fun fetchFromLl(filters: WatchFilterPreferences, limit: Int): List<CachedLaunch> {
        val response = httpClient.get(LL_API_URL) {
            parameter("limit", limit)
            parameter("mode", "list")
            parameter("format", "json")
            applyFilters(filters, normalize = false)
        }
        return response.body<ApiLaunchListResponse>().results.mapNotNull { it.toCachedLaunch() }
    }

    private suspend fun fetchFromTerminus(
        baseUrl: String,
        filters: WatchFilterPreferences,
        limit: Int,
    ): List<CachedLaunch> {
        log.d { "Reading launches from Terminus ($baseUrl)" }
        val response = httpClient.get(baseUrl + TERMINUS_LAUNCHES_PATH) {
            parameter("limit", limit)
            parameter("upcoming", true)
            parameter("ordering", "net")
            applyFilters(filters, normalize = true)
        }
        return response.body<TerminusLaunchListResponse>().results.mapNotNull { it.toCachedLaunch() }
    }

    /** Id filters apply only when the user does not follow all launches. Terminus gets sorted, distinct ids. */
    private fun HttpRequestBuilder.applyFilters(filters: WatchFilterPreferences, normalize: Boolean) {
        if (filters.followAllLaunches) return
        filters.agencyIds.asFilter(normalize)?.let { parameter("lsp__id", it) }
        filters.locationIds.asFilter(normalize)?.let { parameter("location__ids", it) }
    }

    private fun List<Int>?.asFilter(normalize: Boolean): String? =
        this?.takeIf { it.isNotEmpty() }
            ?.let { ids -> if (normalize) ids.distinct().sorted() else ids }
            ?.joinToString(",")

    companion object {
        const val LL_API_URL = "https://ll.thespacedevs.com/2.4.0/launches/upcoming/"
        const val TERMINUS_LAUNCHES_PATH = "/api/v1/launches/"
    }
}

// -- LL list-mode response (minimal: only the fields the watch needs) ---------------------

@Serializable
private data class ApiLaunchListResponse(
    val count: Int,
    val results: List<ApiLaunchBasic>,
    val next: String? = null,
    val previous: String? = null,
)

@Serializable
private data class ApiLaunchBasic(
    val id: String,
    val name: String? = null,
    val net: String? = null,
    val status: ApiLaunchStatus? = null,
    @SerialName("launch_service_provider") val launchServiceProvider: ApiAgencyMini? = null,
    @SerialName("launch_designator") val launchDesignator: String? = null,
    @SerialName("location_name") val locationName: String? = null,
    val image: ApiImage? = null,
)

@Serializable
private data class ApiLaunchStatus(
    val id: Int? = null,
    val name: String? = null,
    val abbrev: String? = null,
)

@Serializable
private data class ApiAgencyMini(
    val id: Int? = null,
    val name: String? = null,
    val abbrev: String? = null,
)

@Serializable
private data class ApiImage(
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
)

private fun ApiLaunchBasic.toCachedLaunch(): CachedLaunch? {
    val parsedNet = net.parseInstantOrNull() ?: return null
    return CachedLaunch(
        id = id,
        name = name ?: "Unknown",
        net = parsedNet,
        statusAbbrev = status?.abbrev,
        statusName = status?.name,
        lspName = launchServiceProvider?.name,
        lspAbbrev = launchServiceProvider?.abbrev,
        rocketConfigName = launchDesignator,
        missionName = null, // Not available in basic/list mode
        missionDescription = null, // Not available in basic/list mode
        padLocationName = locationName,
        imageUrl = image?.imageUrl,
    )
}

// -- Terminus `/api/v1/launches/` (normal mode) response, same minimal approach ------------

@Serializable
private data class TerminusLaunchListResponse(
    val count: Int = 0,
    val results: List<TerminusLaunch> = emptyList(),
)

@Serializable
private data class TerminusLaunch(
    val id: String,
    val name: String? = null,
    val net: String? = null,
    val status: ApiLaunchStatus? = null,
    @SerialName("launch_service_provider") val launchServiceProvider: ApiAgencyMini? = null,
    val rocket: TerminusRocket? = null,
    val mission: TerminusMission? = null,
    val pad: TerminusPad? = null,
    val image: ApiImage? = null,
)

@Serializable
private data class TerminusRocket(val configuration: TerminusRocketConfig? = null)

@Serializable
private data class TerminusRocketConfig(
    val name: String? = null,
    @SerialName("full_name") val fullName: String? = null,
)

@Serializable
private data class TerminusMission(val name: String? = null, val description: String? = null)

@Serializable
private data class TerminusPad(val location: TerminusLocation? = null)

@Serializable
private data class TerminusLocation(val name: String? = null)

/** Same field choices as the phone's Data Layer sync (`toSyncLaunch`), so both paths fill the watch model alike. */
private fun TerminusLaunch.toCachedLaunch(): CachedLaunch? {
    val parsedNet = net.parseInstantOrNull() ?: return null
    return CachedLaunch(
        id = id,
        name = name ?: "Unknown",
        net = parsedNet,
        statusAbbrev = status?.abbrev,
        statusName = status?.name,
        lspName = launchServiceProvider?.name,
        lspAbbrev = launchServiceProvider?.abbrev,
        rocketConfigName = rocket?.configuration?.let { it.fullName ?: it.name },
        missionName = mission?.name,
        missionDescription = mission?.description,
        padLocationName = pad?.location?.name,
        imageUrl = image?.imageUrl,
    )
}

private fun String?.parseInstantOrNull(): Instant? =
    this?.let { runCatching { Instant.parse(it) }.getOrNull() }
