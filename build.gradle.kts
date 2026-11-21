plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.openApiGenerator).apply(false)
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.aboutLibraries) apply false
    // Declared here so its kotlin-gradle-plugin 2.3.10 dependency resolves against the
    // project's Kotlin version instead of landing in composeApp's script classpath.
    alias(libs.plugins.sqldelight) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("com.google.firebase.crashlytics") version "3.0.3" apply false

}