package me.calebjones.spacelaunchnow.util.logging

import java.io.File

class DesktopDiagnosticsFileStore : DiagnosticsFileStore {
    private val file = File(System.getProperty("user.home"), ".spacelaunchnow/sln_diagnostics.log")

    override fun read(): String? = try {
        if (file.exists()) file.readText() else null
    } catch (_: Exception) {
        null
    }

    override fun write(content: String) {
        try {
            file.parentFile?.mkdirs()
            file.writeText(content)
        } catch (_: Exception) {
            // best-effort
        }
    }
}
