import java.util.Properties

// Read version from the root version.properties — same source of truth as composeApp
val versionProps = Properties().apply {
    rootProject.file("version.properties").inputStream().use { load(it) }
}

fun wearVersionCode(): Int {
    val major = versionProps["versionMajor"].toString().toInt()
    val minor = versionProps["versionMinor"].toString().toInt()
    val patch = versionProps["versionPatch"].toString().toInt()
    val build = versionProps["versionBuildNumber"].toString().toInt()
    // +1 offset so the wear AAB always has a unique versionCode from the phone AAB
    // in the same Play Console release (Play requires each AAB to be unique).
    return (major * 1000000) + (minor * 100000) + (patch * 10000) + build + 1
}

fun wearVersionName(): String {
    val major = versionProps["versionMajor"].toString()
    val minor = versionProps["versionMinor"].toString()
    val patch = versionProps["versionPatch"].toString()
    return "$major.$minor.$patch"
}

plugins {
    alias(libs.plugins.androidApplication)
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

android {
    namespace = "me.calebjones.spacelaunchnow.wear"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "me.calebjones.spacelaunchnow"
        minSdk = libs.versions.android.wear.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = wearVersionCode()
        versionName = wearVersionName()
    }

    signingConfigs {
        create("release") {
            // Populated via environment variables in CI (same keystore as composeApp).
            // Locally, falls back to the debug keystore so release builds still compile.
            val keystoreFile = System.getenv("KEYSTORE_FILE")
            if (keystoreFile != null) {
                storeFile = file(keystoreFile)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".kmpdebug"
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Wear Compose M3
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.material3)
    implementation(libs.wear.compose.navigation)
    debugImplementation(libs.wear.compose.ui.tooling)

    // Wear Tiles + ProtoLayout
    implementation(libs.wear.tiles)
    implementation(libs.wear.protolayout)
    implementation(libs.wear.protolayout.material3)
    implementation(libs.wear.protolayout.expression)

    // Wear Watchface Complications
    implementation(libs.wear.watchface.complications)

    // DataLayer Communication
    implementation(libs.play.services.wearable)
    implementation(libs.wear.remote.interactions)

    // Koin DI
    implementation(libs.koin.android)
    implementation(libs.koin.compose.viewmodel)

    // Ktor (direct API calls from watch WiFi/LTE)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // DataStore (local cache)
    implementation(libs.androidx.datastore.preferences)

    // Kotlinx
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.coroutines.guava)

    // WorkManager (background refresh)
    implementation(libs.androidx.work.runtime.ktx)

    // AndroidX Activity + Lifecycle
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)

    // Logging
    implementation(libs.kermit)
}
