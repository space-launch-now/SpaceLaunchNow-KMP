package me.calebjones.spacelaunchnow.util.logging

import io.ktor.client.plugins.logging.LogLevel
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Shared Ktor clients must log URL + status only (spec D3, phase 4). */
class HttpLoggingLevelTest {

    @Test
    fun sharedLevelIsInfo() {
        assertEquals(LogLevel.INFO, SHARED_HTTP_LOG_LEVEL)
    }

    @Test
    fun noCommonMainSourceInstallsVerboseHttpLogging() {
        var dir: File? = File(System.getProperty("user.dir")).absoluteFile
        var root: File? = null
        while (dir != null && root == null) {
            val c = File(dir, "composeApp/src/commonMain/kotlin")
            val d = File(dir, "src/commonMain/kotlin")
            root = if (c.isDirectory) c else if (d.isDirectory) d else null
            dir = dir.parentFile
        }
        assertTrue(root != null, "commonMain source root not found")
        val banned = Regex("""LogLevel\.(ALL|BODY|HEADERS)\b""")
        val offenders = root.walkTopDown().filter { it.extension == "kt" }
            .filter { banned.containsMatchIn(it.readText()) }
            .map { it.name }.toList()
        assertTrue(offenders.isEmpty(), "Verbose Ktor LogLevel in: $offenders")
    }
}
