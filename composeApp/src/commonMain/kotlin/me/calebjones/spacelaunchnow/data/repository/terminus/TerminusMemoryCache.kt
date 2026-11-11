package me.calebjones.spacelaunchnow.data.repository.terminus

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.calebjones.spacelaunchnow.database.LaunchLocalDataSource.CachedValue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A small in-process cache for Terminus reads that have no SQLDelight table of their own
 * (events and updates). An entry is fresh for [ttl]; after that only the stale read returns it,
 * so [terminusCachedFetch] can serve it when the network fails. The map is bounded: at
 * [maxEntries] the oldest entry goes. Nothing is persisted, so a restart starts empty.
 *
 * [now] is the time source in epoch milliseconds, injectable for tests.
 */
internal class TerminusMemoryCache(
    private val ttl: Duration = DEFAULT_TTL,
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) {
    private class Entry(val value: Any, val storedAt: Long)

    private val mutex = Mutex()
    private val entries = LinkedHashMap<String, Entry>()

    suspend fun <T : Any> fresh(key: String): CachedValue<T>? = mutex.withLock {
        val entry = entries[key] ?: return null
        if (now() - entry.storedAt < ttl.inWholeMilliseconds) entry.toCached() else null
    }

    suspend fun <T : Any> stale(key: String): CachedValue<T>? = mutex.withLock {
        entries[key]?.toCached()
    }

    suspend fun put(key: String, value: Any) = mutex.withLock {
        entries.remove(key)
        entries[key] = Entry(value, now())
        while (entries.size > maxEntries) {
            entries.remove(entries.keys.first())
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> Entry.toCached(): CachedValue<T> = CachedValue(value as T, storedAt)

    companion object {
        /** Longest a fresh entry lives (Tasks#1009: 30 s max for Terminus event and update reads). */
        val DEFAULT_TTL = 30.seconds
        const val DEFAULT_MAX_ENTRIES = 64
    }
}
