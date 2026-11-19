package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource.CachedValue

/** In-memory [FilterOptionStore] for tests. */
internal class FakeFilterOptionStore : FilterOptionStore {
    val rows = mutableMapOf<String, CachedValue<List<FilterOption>>>()
    override suspend fun read(key: String) = rows[key]
    override suspend fun write(key: String, options: List<FilterOption>, cachedAt: Long) {
        rows[key] = CachedValue(options, cachedAt)
    }
}
