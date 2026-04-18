# Quickstart: Wear OS Premium Experience

**Date**: 2026-04-15 | **Status**: Complete

## 1. Add Wear OS Dependencies to Version Catalog

In `gradle/libs.versions.toml`, add:

```toml
[versions]
# Wear OS
wear-compose = "1.5.0"
wear-tiles = "1.6.0"
wear-protolayout = "1.4.0"
wear-watchface = "1.3.0"
play-services-wearable = "21.0.0"

[libraries]
# Wear Compose M3
wear-compose-foundation = { module = "androidx.wear.compose:compose-foundation", version.ref = "wear-compose" }
wear-compose-material3 = { module = "androidx.wear.compose:compose-material3", version.ref = "wear-compose" }
wear-compose-navigation = { module = "androidx.wear.compose:compose-navigation", version.ref = "wear-compose" }
wear-compose-ui-tooling = { module = "androidx.wear.compose:compose-ui-tooling", version.ref = "wear-compose" }

# Wear Tiles
wear-tiles = { module = "androidx.wear.tiles:tiles", version.ref = "wear-tiles" }
wear-protolayout = { module = "androidx.wear.protolayout:protolayout", version.ref = "wear-protolayout" }
wear-protolayout-material3 = { module = "androidx.wear.protolayout:protolayout-material3", version.ref = "wear-protolayout" }
wear-protolayout-expression = { module = "androidx.wear.protolayout:protolayout-expression", version.ref = "wear-protolayout" }

# Wear Complications
wear-complications-data-source = { module = "androidx.wear.watchface:watchface-complications-data-source", version.ref = "wear-watchface" }
wear-complications-data-source-ktx = { module = "androidx.wear.watchface:watchface-complications-data-source-ktx", version.ref = "wear-watchface" }

# Phone-Watch Communication
play-services-wearable = { module = "com.google.android.gms:play-services-wearable", version.ref = "play-services-wearable" }
```

## 2. Include wearApp Module

In `settings.gradle.kts`, add:

```kotlin
include(":wearApp")
```

## 3. Create wearApp Module

Create `wearApp/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "me.calebjones.spacelaunchnow.wear"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.calebjones.spacelaunchnow.wear"
        minSdk = 30  // Wear OS 3.0 minimum
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
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

    // Complications
    implementation(libs.wear.complications.data.source)
    implementation(libs.wear.complications.data.source.ktx)

    // DataLayer Communication
    implementation(libs.play.services.wearable)

    // Shared (Koin, Ktor, DataStore, kotlinx-serialization)
    implementation(libs.koin.android)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    // WorkManager for background refresh
    implementation(libs.androidx.work.runtime.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
```

## 4. Create Wear AndroidManifest.xml

Create `wearApp/src/main/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-feature android:name="android.hardware.type.watch" />

    <application
        android:name=".WearApplication"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.DeviceDefault">

        <!-- Main Activity -->
        <activity
            android:name=".WearActivity"
            android:exported="true"
            android:taskAffinity=""
            android:theme="@android:style/Theme.DeviceDefault">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Complication Data Source -->
        <service
            android:name=".complication.NextLaunchComplicationService"
            android:exported="true"
            android:permission="com.google.android.wearable.permission.BIND_COMPLICATION_PROVIDER">
            <intent-filter>
                <action android:name="android.support.wearable.complications.ACTION_COMPLICATION_UPDATE_REQUEST" />
            </intent-filter>
            <meta-data
                android:name="android.support.wearable.complications.SUPPORTED_TYPES"
                android:value="SHORT_TEXT,LONG_TEXT,RANGED_VALUE" />
            <meta-data
                android:name="android.support.wearable.complications.UPDATE_PERIOD_SECONDS"
                android:value="300" />
        </service>

        <!-- Tile Service -->
        <service
            android:name=".tile.NextLaunchTileService"
            android:exported="true"
            android:permission="com.google.android.wearable.permission.BIND_TILE_PROVIDER">
            <intent-filter>
                <action android:name="androidx.wear.tiles.action.BIND_TILE_PROVIDER" />
            </intent-filter>
        </service>

        <!-- DataLayer Listener -->
        <service
            android:name=".data.DataLayerListenerService"
            android:exported="true">
            <intent-filter>
                <action android:name="com.google.android.gms.wearable.DATA_CHANGED" />
            </intent-filter>
            <data
                android:host="*"
                android:pathPrefix="/spacelaunchnow"
                android:scheme="wear" />
        </service>

    </application>
</manifest>
```

## 5. Phone-Side DataLayer Setup

Add to `composeApp/build.gradle.kts` androidMain dependencies:

```kotlin
// In the android sourceSet or appropriate configuration
implementation(libs.play.services.wearable)
```

Register `PhoneDataLayerListenerService` in `composeApp/src/androidMain/AndroidManifest.xml`:

```xml
<service
    android:name=".data.PhoneDataLayerListenerService"
    android:exported="true">
    <intent-filter>
        <action android:name="com.google.android.gms.wearable.MESSAGE_RECEIVED" />
    </intent-filter>
    <data
        android:host="*"
        android:pathPrefix="/spacelaunchnow/request-sync"
        android:scheme="wear" />
</service>
```

## 6. Project Directory Structure

```
wearApp/
├── build.gradle.kts
└── src/main/
    ├── AndroidManifest.xml
    ├── res/
    │   ├── mipmap-*/ic_launcher.webp
    │   └── values/strings.xml
    └── kotlin/me/calebjones/spacelaunchnow/wear/
        ├── WearApplication.kt          # Koin initialization
        ├── WearActivity.kt             # Main entry, hosts SwipeDismissableNavHost
        ├── WearScreen.kt               # Sealed class navigation
        ├── complication/
        │   └── NextLaunchComplicationService.kt
        ├── tile/
        │   └── NextLaunchTileService.kt
        ├── data/
        │   ├── DataLayerListenerService.kt
        │   ├── WatchLaunchRepositoryImpl.kt
        │   ├── EntitlementSyncManagerImpl.kt
        │   └── model/
        │       ├── CachedLaunch.kt
        │       ├── WearEntitlementState.kt
        │       ├── DataLayerSyncPayload.kt
        │       └── SyncLaunch.kt
        ├── di/
        │   └── WearModule.kt           # Koin module
        ├── ui/
        │   ├── LaunchListScreen.kt
        │   ├── LaunchDetailScreen.kt
        │   ├── PremiumGateScreen.kt
        │   └── SettingsScreen.kt
        ├── viewmodel/
        │   ├── LaunchListViewModel.kt
        │   └── LaunchDetailViewModel.kt
        └── worker/
            └── WatchDataRefreshWorker.kt
```

## 7. Verification Steps

After creating the module:

1. `./gradlew :wearApp:assembleDebug` — Verify wearApp compiles
2. `./gradlew compileKotlinDesktop` — Verify existing targets still build (Constitution Principle VI)
3. Deploy to Wear OS emulator (API 34+) and verify app launches
4. Verify complication appears in watch face complication picker
5. Verify tile appears in tile carousel editor
