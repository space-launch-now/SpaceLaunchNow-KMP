package me.calebjones.spacelaunchnow.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.domain.model.Program
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProgramLocalDataSourceTest {

    private fun newDatabase(): SpaceLaunchDatabase {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        return SpaceLaunchDatabase(driver)
    }

    private fun newDataSource(database: SpaceLaunchDatabase) =
        ProgramLocalDataSource(database, AppPreferences(InMemoryPreferencesDataStore()))

    @Test
    fun cacheProgramRoundTripsDomainModel() = runBlocking {
        val database = newDatabase()
        val dataSource = newDataSource(database)
        val program = Program(
            id = 1,
            name = "Starship",
            description = "SpaceX's next-gen launch system",
            imageUrl = "https://example.com/starship.png"
        )

        dataSource.cacheProgram(program)

        assertEquals(program, dataSource.getProgram(1))
    }

    @Test
    fun legacyShapedBlobIsTreatedAsCacheMissNotCrash() = runBlocking {
        val database = newDatabase()
        val dataSource = newDataSource(database)
        val now = 0L

        // Write a row directly via the generated queries object (bypassing cacheProgram's
        // domain serializer). The json_data blob's "id" is a string, which is the shape a
        // pre-migration LL-typed cache entry could leave behind - it fails to decode as the
        // domain Program (id: Int), and must be treated as a cache miss rather than crashing.
        database.programQueries.insertOrReplaceProgram(
            id = 1L,
            name = "Legacy Program",
            description = null,
            image_url = null,
            info_url = null,
            wiki_url = null,
            start_date = null,
            end_date = null,
            json_data = """{"id":"not-an-int","name":"Legacy Program"}""",
            cached_at = now,
            expires_at = now + 100_000L
        )

        assertNull(dataSource.getProgram(1))
        assertNull(dataSource.getProgramStale(1))
    }
}
