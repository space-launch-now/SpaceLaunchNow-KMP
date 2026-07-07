package me.calebjones.spacelaunchnow.util.logging

import android.content.Context
import java.io.File

class AndroidDiagnosticsFileStore(context: Context) : DiagnosticsFileStore {
    private val file = File(context.filesDir, "sln_diagnostics.log")

    override fun read(): String? = try {
        if (file.exists()) file.readText() else null
    } catch (_: Exception) {
        null
    }

    override fun write(content: String) {
        try {
            file.writeText(content)
        } catch (_: Exception) {
            // best-effort
        }
    }
}
