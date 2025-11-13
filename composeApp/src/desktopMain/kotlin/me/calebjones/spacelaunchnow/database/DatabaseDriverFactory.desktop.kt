package me.calebjones.spacelaunchnow.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val databasePath = File(System.getProperty("user.home"), ".spacelaunchnow")
        databasePath.mkdirs()
        val databaseFile = File(databasePath, "spacelaunchnow.db")
        
        return JdbcSqliteDriver(
            url = "jdbc:sqlite:${databaseFile.absolutePath}",
        ).also { driver ->
            SpaceLaunchDatabase.Schema.create(driver)
        }
    }
}
