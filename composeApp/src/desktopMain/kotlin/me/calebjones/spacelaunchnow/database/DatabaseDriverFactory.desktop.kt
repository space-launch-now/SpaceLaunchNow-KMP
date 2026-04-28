package me.calebjones.spacelaunchnow.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

// The version at which the desktop DB was originally created before version tracking was added.
// DBs created by old code have user_version = 0 (never set), but contain the full schema up to v7.
private const val LEGACY_SCHEMA_VERSION = 7

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".spacelaunchnow")
        databasePath.mkdirs()
        val databaseFile = File(databasePath, "spacelaunchnow.db")
        val dbExists = databaseFile.exists()
        val schemaVersion = SpaceLaunchDatabase.Schema.version.toInt()

        val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databaseFile.absolutePath}")

        if (!dbExists) {
            SpaceLaunchDatabase.Schema.create(driver)
            driver.execute(null, "PRAGMA user_version = $schemaVersion", 0)
        } else {
            // Read user_version via a separate JDBC connection before applying migrations.
            // Legacy DBs created before version tracking was added will have user_version = 0.
            // Treat those as LEGACY_SCHEMA_VERSION since Schema.create() at that time only
            // included tables up to that version.
            val storedVersion = readUserVersion(databaseFile)
            val effectiveVersion = if (storedVersion == 0) LEGACY_SCHEMA_VERSION else storedVersion

            if (effectiveVersion < schemaVersion) {
                try {
                    SpaceLaunchDatabase.Schema.migrate(
                        driver = driver,
                        oldVersion = effectiveVersion.toLong(),
                        newVersion = schemaVersion.toLong()
                    )
                    driver.execute(null, "PRAGMA user_version = $schemaVersion", 0)
                } catch (e: Exception) {
                    // Migration failed — DB is likely corrupted. Drop and recreate for desktop.
                    driver.close()
                    databaseFile.delete()
                    val freshDriver = JdbcSqliteDriver(url = "jdbc:sqlite:${databaseFile.absolutePath}")
                    SpaceLaunchDatabase.Schema.create(freshDriver)
                    freshDriver.execute(null, "PRAGMA user_version = $schemaVersion", 0)
                    return freshDriver
                }
            }
        }

        return driver
    }

    private fun readUserVersion(databaseFile: File): Int {
        return java.sql.DriverManager.getConnection("jdbc:sqlite:${databaseFile.absolutePath}").use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery("PRAGMA user_version").use { rs ->
                    if (rs.next()) rs.getInt(1) else 0
                }
            }
        }
    }
}
