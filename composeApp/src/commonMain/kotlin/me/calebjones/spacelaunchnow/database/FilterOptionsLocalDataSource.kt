package me.calebjones.spacelaunchnow.database

import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Clock.System
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Local data source for filter options using SQLDelight
 * Provides caching with automatic expiration
 */
class FilterOptionsLocalDataSource(
    database: SpaceLaunchDatabase,
    private val appPreferences: AppPreferences
) {
    private val queries = database.filterOptionsQueries
    private val log = logger()

    // Filter options cache for 7 days (they don't change often)
    private val cacheDuration = 7.days

    // Debug cache duration (1 hour for testing)
    private val debugCacheDuration = 1.hours

    private suspend fun getEffectiveCacheDuration(): kotlin.time.Duration {
        return if (appPreferences.isDebugShortCacheTtlEnabled()) {
            log.w { "⚠️ DEBUG MODE: Using short cache TTL (1 hour) instead of ${cacheDuration.inWholeDays} days" }
            debugCacheDuration
        } else {
            cacheDuration
        }
    }

    // Agency operations
    suspend fun cacheAgency(
        id: Int,
        name: String,
        abbreviation: String?,
        isFeatured: Boolean = true
    ) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds

        queries.insertOrReplaceAgency(
            id = id.toLong(),
            name = name,
            abbreviation = abbreviation,
            is_featured = if (isFeatured) 1L else 0L,
            cached_at = now,
            expires_at = expiresAt
        )
    }

    suspend fun cacheAgencies(agencies: List<Triple<Int, String, String?>>) {
        agencies.forEach { (id, name, abbrev) -> cacheAgency(id, name, abbrev) }
    }

    suspend fun getAllAgencies(): List<FilterableAgency> {
        val now = System.now().toEpochMilliseconds()
        return queries.getAllAgencies(now).executeAsList()
    }

    suspend fun getAllAgenciesStale(): List<FilterableAgency> {
        return queries.getAllAgenciesStale().executeAsList()
    }

    suspend fun deleteExpiredAgencies() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredAgencies(now)
    }

    suspend fun clearAllAgencies() {
        queries.clearAllAgencies()
    }

    // Program operations
    suspend fun cacheProgram(id: Int, name: String, abbreviation: String?) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds

        queries.insertOrReplaceProgram(
            id = id.toLong(),
            name = name,
            abbreviation = abbreviation,
            cached_at = now,
            expires_at = expiresAt
        )
    }

    suspend fun cachePrograms(programs: List<Triple<Int, String, String?>>) {
        programs.forEach { (id, name, abbrev) -> cacheProgram(id, name, abbrev) }
    }

    suspend fun getAllPrograms(): List<FilterableProgram> {
        val now = System.now().toEpochMilliseconds()
        return queries.getAllPrograms(now).executeAsList()
    }

    suspend fun getAllProgramsStale(): List<FilterableProgram> {
        return queries.getAllProgramsStale().executeAsList()
    }

    suspend fun deleteExpiredPrograms() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredPrograms(now)
    }

    suspend fun clearAllPrograms() {
        queries.clearAllPrograms()
    }

    // Rocket operations
    suspend fun cacheRocket(id: Int, name: String, abbreviation: String?) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds

        queries.insertOrReplaceRocket(
            id = id.toLong(),
            name = name,
            abbreviation = abbreviation,
            cached_at = now,
            expires_at = expiresAt
        )
    }

    suspend fun cacheRockets(rockets: List<Triple<Int, String, String?>>) {
        rockets.forEach { (id, name, abbrev) -> cacheRocket(id, name, abbrev) }
    }

    suspend fun getAllRockets(): List<FilterableRocket> {
        val now = System.now().toEpochMilliseconds()
        return queries.getAllRockets(now).executeAsList()
    }

    suspend fun getAllRocketsStale(): List<FilterableRocket> {
        return queries.getAllRocketsStale().executeAsList()
    }

    suspend fun deleteExpiredRockets() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredRockets(now)
    }

    suspend fun clearAllRockets() {
        queries.clearAllRockets()
    }

    // Location operations
    suspend fun cacheLocation(id: Int, name: String) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds

        queries.insertOrReplaceLocation(
            id = id.toLong(),
            name = name,
            cached_at = now,
            expires_at = expiresAt
        )
    }

    suspend fun cacheLocations(locations: List<Pair<Int, String>>) {
        locations.forEach { (id, name) -> cacheLocation(id, name) }
    }

    suspend fun getAllLocations(): List<FilterableLocation> {
        val now = System.now().toEpochMilliseconds()
        return queries.getAllLocations(now).executeAsList()
    }

    suspend fun getAllLocationsStale(): List<FilterableLocation> {
        return queries.getAllLocationsStale().executeAsList()
    }

    suspend fun deleteExpiredLocations() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredLocations(now)
    }

    suspend fun clearAllLocations() {
        queries.clearAllLocations()
    }

    // Status operations
    suspend fun cacheStatus(id: Int, name: String, abbreviation: String?, description: String?) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds

        queries.insertOrReplaceStatus(
            id = id.toLong(),
            name = name,
            abbreviation = abbreviation,
            description = description,
            cached_at = now,
            expires_at = expiresAt
        )
    }

    suspend fun cacheStatuses(statuses: List<Tuple4<Int, String, String?, String?>>) {
        statuses.forEach { (id, name, abbrev, description) ->
            cacheStatus(
                id,
                name,
                abbrev,
                description
            )
        }
    }

    suspend fun getAllStatuses(): List<FilterableStatus> {
        val now = System.now().toEpochMilliseconds()
        return queries.getAllStatuses(now).executeAsList()
    }

    suspend fun getAllStatusesStale(): List<FilterableStatus> {
        return queries.getAllStatusesStale().executeAsList()
    }

    suspend fun deleteExpiredStatuses() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredStatuses(now)
    }

    suspend fun clearAllStatuses() {
        queries.clearAllStatuses()
    }

    // Orbit operations
    suspend fun cacheOrbit(id: Int, name: String, abbreviation: String?) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds

        queries.insertOrReplaceOrbit(
            id = id.toLong(),
            name = name,
            abbreviation = abbreviation,
            cached_at = now,
            expires_at = expiresAt
        )
    }

    suspend fun cacheOrbits(orbits: List<Triple<Int, String, String?>>) {
        orbits.forEach { (id, name, abbrev) -> cacheOrbit(id, name, abbrev) }
    }

    suspend fun getAllOrbits(): List<FilterableOrbit> {
        val now = System.now().toEpochMilliseconds()
        return queries.getAllOrbits(now).executeAsList()
    }

    suspend fun getAllOrbitsStale(): List<FilterableOrbit> {
        return queries.getAllOrbitsStale().executeAsList()
    }

    suspend fun deleteExpiredOrbits() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredOrbits(now)
    }

    suspend fun clearAllOrbits() {
        queries.clearAllOrbits()
    }

    // Mission Type operations
    suspend fun cacheMissionType(id: Int, name: String) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds

        queries.insertOrReplaceMissionType(
            id = id.toLong(),
            name = name,
            cached_at = now,
            expires_at = expiresAt
        )
    }

    suspend fun cacheMissionTypes(missionTypes: List<Pair<Int, String>>) {
        missionTypes.forEach { (id, name) -> cacheMissionType(id, name) }
    }

    suspend fun getAllMissionTypes(): List<FilterableMissionType> {
        val now = System.now().toEpochMilliseconds()
        return queries.getAllMissionTypes(now).executeAsList()
    }

    suspend fun getAllMissionTypesStale(): List<FilterableMissionType> {
        return queries.getAllMissionTypesStale().executeAsList()
    }

    suspend fun deleteExpiredMissionTypes() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredMissionTypes(now)
    }

    suspend fun clearAllMissionTypes() {
        queries.clearAllMissionTypes()
    }

    // Launcher Config Family operations
    suspend fun cacheLauncherConfigFamily(id: Int, name: String) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds

        queries.insertOrReplaceLauncherConfigFamily(
            id = id.toLong(),
            name = name,
            cached_at = now,
            expires_at = expiresAt
        )
    }

    suspend fun cacheLauncherConfigFamilies(families: List<Pair<Int, String>>) {
        families.forEach { (id, name) -> cacheLauncherConfigFamily(id, name) }
    }

    suspend fun getAllLauncherConfigFamilies(): List<FilterableLauncherConfigFamily> {
        val now = System.now().toEpochMilliseconds()
        return queries.getAllLauncherConfigFamilies(now).executeAsList()
    }

    suspend fun getAllLauncherConfigFamiliesStale(): List<FilterableLauncherConfigFamily> {
        return queries.getAllLauncherConfigFamiliesStale().executeAsList()
    }

    suspend fun deleteExpiredLauncherConfigFamilies() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredLauncherConfigFamilies(now)
    }

    suspend fun clearAllLauncherConfigFamilies() {
        queries.clearAllLauncherConfigFamilies()
    }
}

// Helper data class for status tuples
data class Tuple4<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

operator fun <A, B, C, D> Tuple4<A, B, C, D>.component1() = first
operator fun <A, B, C, D> Tuple4<A, B, C, D>.component2() = second
operator fun <A, B, C, D> Tuple4<A, B, C, D>.component3() = third
operator fun <A, B, C, D> Tuple4<A, B, C, D>.component4() = fourth

