package me.calebjones.spacelaunchnow.data.subscription

import kotlinx.io.files.Path
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation of AppDirectories
 */
actual object AppDirectories {
    
    actual fun getAppDataDir(): Path {
        val fileManager = NSFileManager.defaultManager
        val documentsUrl = fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            appropriateForURL = null,
            create = false,
            inDomain = NSUserDomainMask,
            error = null
        )!!
        
        return Path(documentsUrl.path!!)
    }
}