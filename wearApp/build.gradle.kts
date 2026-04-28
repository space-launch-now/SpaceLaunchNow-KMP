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
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".kmpdebug"
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
