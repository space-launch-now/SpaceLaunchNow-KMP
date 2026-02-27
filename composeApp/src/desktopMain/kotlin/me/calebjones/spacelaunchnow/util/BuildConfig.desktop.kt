package me.calebjones.spacelaunchnow.util

import java.io.File
import java.util.Properties

actual fun initializeBuildConfig() {
    // Read version from version.properties file
    try {
        val propsFile = File(\"version.properties\")
        if (propsFile.exists()) {
            val props = Properties()
            propsFile.inputStream().use { props.load(it) }
            
            val major = props.getProperty(\"versionMajor\", \"0\").toInt()
            val minor = props.getProperty(\"versionMinor\", \"0\").toInt()
            val patch = props.getProperty(\"versionPatch\", \"0\").toInt()
            val buildNumber = props.getProperty(\"versionBuildNumber\", \"0\").toInt()
            
            BuildConfig.VERSION_NAME = String.format(\"%d.%d.%d-b%d\", major, minor, patch, buildNumber)
            BuildConfig.VERSION_CODE = (major * 1000000) + (minor * 100000) + (patch * 10000) + buildNumber
        } else {
            // Fallback if version.properties not found
            BuildConfig.VERSION_NAME = \"unknown\"\n            BuildConfig.VERSION_CODE = 0
        }
    } catch (e: Exception) {
        // Fallback on error
        BuildConfig.VERSION_NAME = \"unknown\"
        BuildConfig.VERSION_CODE = 0
    }

    // For desktop, we can check system properties or default to debug mode
    BuildConfig.IS_DEBUG = System.getProperty("app.debug", "true").toBoolean()
}