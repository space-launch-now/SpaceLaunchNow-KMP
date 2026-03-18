package me.calebjones.spacelaunchnow.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".spacelaunchnow")
        databasePath.mkdirs()
        val databaseFile = File(databasePath, "spacelaunchnow.db")
        val dbExists = databaseFile.exists()

        return JdbcSqliteDriver(
            url = "jdbc:sqlite:${databaseFile.absolutePath}",
        ).also { driver ->
            if (!dbExists) {
                SpaceLaunchDatabase.Schema.create(driver)
            } else {
                val currentVersion = SpaceLaunchDatabase.Schema.version
                // Migrate if needed in the future
            }
        }
    }
}
