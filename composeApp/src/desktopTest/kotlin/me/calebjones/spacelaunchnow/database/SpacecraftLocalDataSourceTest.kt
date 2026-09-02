package me.calebjones.spacelaunchnow.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.domain.model.Spacecraft
import me.calebjones.spacelaunchnow.domain.model.SpacecraftConfig
import me.calebjones.spacelaunchnow.domain.model.SpacecraftStatus
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpacecraftLocalDataSourceTest {

    private fun newDatabase(): SpaceLaunchDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        return SpaceLaunchDatabase(driver)
    }

    private fun newDataSource(database: SpaceLaunchDatabase) =
        SpacecraftLocalDataSource(database, AppPreferences(InMemoryPreferencesDataStore()))

    @Test
    fun cacheSpacecraftRoundTripsDomainModel() = runBlocking {
        val database = newDatabase()
        val dataSource = newDataSource(database)
        val spacecraft = Spacecraft(
            id = 205,
            name = "Ship 33",
            serialNumber = "S33",
            status = SpacecraftStatus(id = 2, name = "Active"),
            config = SpacecraftConfig(id = 1, name = "Starship")
        )

        dataSource.cacheSpacecraft(spacecraft)

        assertEquals(spacecraft, dataSource.getSpacecraft(205))
        assertEquals(listOf(spacecraft), dataSource.getSpacecraftByConfigId(1, 10))
    }

    @Test
    fun legacyShapedBlobIsTreatedAsCacheMissNotCrash() = runBlocking {
        val database = newDatabase()
        val dataSource = newDataSource(database)
        val now = 0L

        // Write a row directly via the generated queries object (bypassing cacheSpacecraft's
        // domain serializer). The json_data blob's "id" is a string, which is the shape a
        // pre-migration LL-typed cache entry could leave behind - it fails to decode as the
        // domain Spacecraft (id: Int), and must be treated as a cache miss rather than crashing.
        database.spacecraftQueries.insertOrReplaceSpacecraft(
            id = 205L,
            name = "Legacy Ship",
            serial_number = null,
            status_id = null,
            status_name = null,
            description = null,
            spacecraft_config_id = 1L,
            spacecraft_config_name = "Starship",
            json_data = """{"id":"not-an-int","name":"Legacy Ship"}""",
            cached_at = now,
            expires_at = now + 100_000L
        )

        assertNull(dataSource.getSpacecraft(205))
        assertNull(dataSource.getSpacecraftStale(205))
        assertTrue(dataSource.getSpacecraftByConfigId(1, 10).isEmpty())
    }
}
