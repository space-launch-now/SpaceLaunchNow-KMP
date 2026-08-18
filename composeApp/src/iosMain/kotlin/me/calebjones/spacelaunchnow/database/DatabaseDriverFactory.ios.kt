package me.calebjones.spacelaunchnow.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseFileContext
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = NativeSqliteDriver(
            schema = SpaceLaunchDatabase.Schema,
            name = "spacelaunchnow.db"
        )
        excludeDatabaseFromBackup()
        return driver
    }

    // The TopicSubscription ledger records THIS installation's FCM
    // subscriptions; restored from iCloud onto a new install it would claim
    // subscriptions the install does not have, and reconciliation would never
    // subscribe -- silent under-delivery. The rest of the DB is TTL'd cache.
    // Runs on every launch: the attribute does not always survive restores.
    private fun excludeDatabaseFromBackup() {
        val path = DatabaseFileContext.databasePath("spacelaunchnow.db", null)
        NSURL.fileURLWithPath(path)
            .setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)
    }
}
