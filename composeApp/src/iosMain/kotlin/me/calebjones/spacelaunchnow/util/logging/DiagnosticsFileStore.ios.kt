package me.calebjones.spacelaunchnow.util.logging

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

class IosDiagnosticsFileStore : DiagnosticsFileStore {
    private val path: String by lazy {
        val fileManager = NSFileManager.defaultManager
        val documentsUrl = fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            appropriateForURL = null,
            create = false,
            inDomain = NSUserDomainMask,
            error = null
        )
        val dir = documentsUrl?.path ?: ""
        "$dir/sln_diagnostics.log"
    }

    override fun read(): String? =
        NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun write(content: String) {
        (content as NSString).writeToFile(
            path,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null
        )
    }
}
