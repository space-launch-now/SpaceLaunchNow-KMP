package me.calebjones.spacelaunchnow.data.repository.terminus

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource.CachedValue
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log get() = SpaceLogger.getLogger("TerminusFilterOptionStore")

/**
 * Disk copy of the Terminus filter lists, one entry per list key. It only stores: the caller
 * decides whether a copy is fresh from [CachedValue.cachedAt].
 */
internal interface FilterOptionStore {
    /** The stored list and its write time, or null when none is stored or it cannot be read. */
    suspend fun read(key: String): CachedValue<List<FilterOption>>?

    /** Replaces the list stored under [key]; an empty list is stored like any other. */
    suspend fun write(key: String, options: List<FilterOption>, cachedAt: Long)
}

/** [FilterOptionStore] over the `TerminusFilterList` table, apart from the LL `Filterable*` tables. */
internal class SqlFilterOptionStore(database: SpaceLaunchDatabase) : FilterOptionStore {
    private val queries = database.terminusFilterListQueries
    private val serializer = ListSerializer(FilterOption.serializer())

    override suspend fun read(key: String): CachedValue<List<FilterOption>>? {
        val row = queries.getList(key).executeAsOneOrNull() ?: return null
        return try {
            CachedValue(Json.decodeFromString(serializer, row.options_json), row.cached_at)
        } catch (e: Exception) {
            log.w(e) { "Unreadable stored filter list $key; treating it as a miss" }
            null
        }
    }

    override suspend fun write(key: String, options: List<FilterOption>, cachedAt: Long) {
        queries.insertOrReplaceList(key, Json.encodeToString(serializer, options), cachedAt)
    }
}
