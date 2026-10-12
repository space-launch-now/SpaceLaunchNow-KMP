package me.calebjones.spacelaunchnow.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.Provider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The debug-menu "Clear All Caches" action (Trantor migration): every cache table plus the
 * in-memory [LaunchCache] must be emptied so the next launch refetches from whichever
 * backend the DataBackend flag names, while the FCM topic ledger — real server-side
 * subscription state, not a cache — survives untouched.
 */
class LocalCacheWiperTest {

    private fun newDatabase(): SpaceLaunchDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        return SpaceLaunchDatabase(driver)
    }

    private fun sampleLaunch(id: String) = Launch(
        id = id,
        name = "Falcon 9 Test",
        slug = id,
        net = null,
        windowStart = null,
        windowEnd = null,
        lastUpdated = null,
        status = null,
        provider = Provider(
            id = 121,
            name = "SpaceX",
            abbrev = "SpX",
            type = "Commercial",
            countryCode = "US",
            logoUrl = null,
            socialLogo = null,
            imageUrl = null
        ),
        imageUrl = null,
        thumbnailUrl = null,
        infographic = null,
        netPrecision = null
    )

    private fun SpaceLaunchDatabase.seedCaches(now: Long) {
        launchQueries.insertOrReplaceDetailed(
            id = "launch-1",
            name = "seed",
            status_id = null,
            status_name = null,
            net = null,
            window_end = null,
            window_start = null,
            launch_service_provider_id = null,
            launch_service_provider_name = null,
            rocket_configuration_id = null,
            rocket_configuration_name = null,
            pad_name = null,
            location_name = null,
            image_url = null,
            mission_name = null,
            mission_description = null,
            json_data = "{}",
            cached_at = now,
            expires_at = now + 600_000L
        )
        filterOptionsQueries.insertOrReplaceAgency(
            id = 121,
            name = "SpaceX",
            abbreviation = "SpX",
            is_featured = 1,
            cached_at = now,
            expires_at = now + 600_000L
        )
        statsQueries.upsertStat(
            stat_key = "upcoming",
            count = 42,
            cached_at = now,
            expires_at = now + 600_000L
        )
    }

    @Test
    fun clearAll_emptiesEveryCacheTable() = runTest {
        val database = newDatabase()
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        database.seedCaches(now)

        LocalCacheWiper(database, LaunchCache()).clearAll()

        assertNull(database.launchQueries.getDetailedByIdStale("launch-1").executeAsOneOrNull())
        assertTrue(database.filterOptionsQueries.getAllAgenciesStale().executeAsList().isEmpty())
        assertNull(database.statsQueries.getStatCountStale("upcoming").executeAsOneOrNull())
    }

    @Test
    fun clearAll_keepsTheTopicSubscriptionLedger() = runTest {
        val database = newDatabase()
        database.topicSubscriptionQueries.insertDesired("v6_prod_agency_121")

        LocalCacheWiper(database, LaunchCache()).clearAll()

        assertEquals(1L, database.topicSubscriptionQueries.countPendingSubscribes().executeAsOne())
    }

    @Test
    fun clearAll_emptiesTheInMemoryLaunchCache() = runTest {
        val launchCache = LaunchCache().apply { cacheLaunch(sampleLaunch("launch-1")) }

        LocalCacheWiper(newDatabase(), launchCache).clearAll()

        assertFalse(launchCache.hasCachedData("launch-1"))
    }
}
