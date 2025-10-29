import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties


// Load version properties
val versionProps = Properties().apply {
    file("../version.properties").inputStream().use { load(it) }
}

// Version management functions
fun computeVersionName(): String {
    val major = versionProps["versionMajor"].toString().toInt()
    val minor = versionProps["versionMinor"].toString().toInt()
    val patch = versionProps["versionPatch"].toString().toInt()
    val buildNumber = versionProps["versionBuildNumber"].toString().toInt()
    return String.format("%d.%d.%d-b%d", major, minor, patch, buildNumber)
}

fun computeVersionNameDesktop(): String {
    val major = versionProps["versionMajor"].toString().toInt()
    val minor = versionProps["versionMinor"].toString().toInt()
    val patch = versionProps["versionPatch"].toString().toInt()
    return String.format("%d.%d.%d", major, minor, patch)
}

fun computeVersionCode(): Int {
    val major = versionProps["versionMajor"].toString().toInt()
    val minor = versionProps["versionMinor"].toString().toInt()
    val patch = versionProps["versionPatch"].toString().toInt()
    val buildNumber = versionProps["versionBuildNumber"].toString().toInt()
    return (major * 1000000) + (minor * 100000) + (patch * 10000) + buildNumber
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.openApiGenerator)
    alias(libs.plugins.aboutLibraries)
    id("com.google.gms.google-services")
}


kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    sourceSets {
        named { it.lowercase().startsWith("ios") }.configureEach {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                implementation(compose.materialIconsExtended)

                implementation(libs.kotlinx.coroutines.swing)

                implementation(libs.slf4j.simple)
                implementation(libs.koin.core)
                implementation(libs.koin.logger.slf4j)
                implementation(libs.coil.compose.jvm)

                // Desktop-specific HTTP client engine (CIO)
                implementation(libs.ktor.client.cio)

                // JDK cryptography provider for Desktop
                implementation(libs.cryptography.provider.jdk)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.koin.android)
                implementation(libs.androidx.palette)
                implementation(libs.coil.compose.android)
                implementation(libs.lifecycle.viewmodel.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.work.runtime)

                // Glance for Android Widgets
                implementation(libs.androidx.glance)
                implementation(libs.androidx.glance.material3)

                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.android.firebase.auth)
                implementation(libs.android.firebase.analytics)
                implementation(libs.android.firebase.messaging)

                implementation(libs.androidx.core.splashscreen)

                // Android-specific HTTP client engine
                implementation(libs.ktor.client.android)

                implementation(libs.billing.ktx)

                // Google AdMob and UMP for Android
                implementation(libs.googleAds)
                implementation(libs.androidUmp)

                // JDK cryptography provider for Android
                implementation(libs.cryptography.provider.jdk)

                // ShadowGlow library for advanced drop shadows
                implementation(libs.shadowglow)
                // Purchases-KMP (RevenueCat): ANDROID ONLY
                implementation(libs.purchases.core)
                implementation(libs.purchases.ui)
                implementation(libs.purchases.either)
                implementation(libs.purchases.result)
                implementation(libs.basicAds)
            }
        }

        iosMain {

            dependencies {
                implementation(libs.coil.compose.ios)

                // iOS-specific HTTP client engine (Darwin)
                implementation(libs.ktor.client.darwin)

                // Apple cryptography provider for iOS
                implementation(libs.cryptography.provider.apple)
                // Purchases-KMP (RevenueCat): IOS ONLY
                implementation(libs.purchases.core)
                implementation(libs.purchases.ui)
                implementation(libs.purchases.either)
                implementation(libs.purchases.result)
            }
        }

        commonMain {
            kotlin.srcDir("$projectDir/src/openApiLL/src/commonMain/kotlin")
            kotlin.srcDir("$projectDir/src/openApiSNAPI/src/commonMain/kotlin")
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.compose.shimmer)

                implementation(compose.material3)
                implementation(compose.materialIconsExtended)

                // Adaptive layouts for responsive UI
                implementation(libs.compose.material3.adaptive)

                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.koin.compose.viewmodel.nav)

                implementation(libs.ktor.client.core)          // Core client dependency
                // NOTE: Platform-specific engines are added in platform sourcesets
                // Android uses ktor-client-android
                // iOS uses ktor-client-darwin  
                // Desktop uses ktor-client-cio
                implementation(libs.ktor.client.serialization) // Serialization support
                implementation(libs.ktor.client.logging)       // Logging support
                implementation(libs.ktor.utils)                // Utilities (for IOException)

                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization)
                api(libs.kotlinx.datetime)

                implementation(libs.compose.icons.fontAwesome)
                implementation(libs.compose.icons.weatherIcons)
                implementation(libs.coil.compose)
                implementation(libs.coil.compose.ktor)
                implementation(libs.insetsx)

                implementation(libs.dotenv)

                implementation(libs.materialKolor)

                // Compose Multiplatform Media Player
                implementation(libs.compose.media.player)

                // DataStore for persistent storage
                implementation(libs.androidx.datastore.preferences)

                implementation(libs.aboutlibraries.core)
                implementation(libs.aboutlibraries.compose.m2)
                implementation(libs.aboutlibraries.compose.m3)

                // Cryptography library for secure hashing
                implementation(libs.cryptography.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }
    }
}

android {
    namespace = "me.calebjones.spacelaunchnow"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")


    defaultConfig {
        applicationId = "me.calebjones.spacelaunchnow"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        val envFile = rootProject.file(".env")
        val envProps = Properties().apply {
            if (envFile.exists()) {
                envFile.inputStream().use { load(it) }
            }
        }
        val apiKey = envProps.getProperty("API_KEY") ?: ""
        val revenueCatAndroidKey = envProps.getProperty("REVENUECAT_ANDROID_KEY") ?: ""
        val revenueCatIosKey = envProps.getProperty("REVENUECAT_IOS_KEY") ?: ""
        
        // AdMob ad unit IDs
        val androidBannerAdUnitId = envProps.getProperty("ANDROID_BANNER_AD_UNIT_ID") ?: ""
        val iosBannerAdUnitId = envProps.getProperty("IOS_BANNER_AD_UNIT_ID") ?: ""
        val androidInterstitialAdUnitId = envProps.getProperty("ANDROID_INTERSTITIAL_AD_UNIT_ID") ?: ""
        val iosInterstitialAdUnitId = envProps.getProperty("IOS_INTERSTITIAL_AD_UNIT_ID") ?: ""
        val androidRewardedAdUnitId = envProps.getProperty("ANDROID_REWARDED_AD_UNIT_ID") ?: ""
        val iosRewardedAdUnitId = envProps.getProperty("IOS_REWARDED_AD_UNIT_ID") ?: ""

        buildConfigField("String", "API_KEY", "\"$apiKey\"")
        buildConfigField("String", "REVENUECAT_ANDROID_KEY", "\"$revenueCatAndroidKey\"")
        buildConfigField("String", "REVENUECAT_IOS_KEY", "\"$revenueCatIosKey\"")
        
        // AdMob ad unit IDs
        buildConfigField("String", "ANDROID_BANNER_AD_UNIT_ID", "\"$androidBannerAdUnitId\"")
        buildConfigField("String", "IOS_BANNER_AD_UNIT_ID", "\"$iosBannerAdUnitId\"")
        buildConfigField("String", "ANDROID_INTERSTITIAL_AD_UNIT_ID", "\"$androidInterstitialAdUnitId\"")
        buildConfigField("String", "IOS_INTERSTITIAL_AD_UNIT_ID", "\"$iosInterstitialAdUnitId\"")
        buildConfigField("String", "ANDROID_REWARDED_AD_UNIT_ID", "\"$androidRewardedAdUnitId\"")
        buildConfigField("String", "IOS_REWARDED_AD_UNIT_ID", "\"$iosRewardedAdUnitId\"")

        // Add version information to BuildConfig
        buildConfigField("String", "VERSION_NAME", "\"${computeVersionName()}\"")
        buildConfigField("int", "VERSION_CODE", "${computeVersionCode()}")

        versionCode = computeVersionCode()
        versionName = computeVersionName()
        manifestPlaceholders["appName"] = "Space Launch Now"
    }
    
    // Bundle configuration for 16KB optimization
    bundle {
        language {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "IS_DEBUG", "false")
        }
        getByName("debug") {
            applicationIdSuffix = ".kmpdebug"
            versionNameSuffix = "-DEBUG"
            buildConfigField("boolean", "IS_DEBUG", "true")
            manifestPlaceholders["appName"] = "@string/app_name_debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

// Fix resource path configuration
tasks.withType<ProcessResources> {
    // Ensure resources from commonMain are copied to all targets
    from("src/commonMain/resources")
}

compose.desktop {
    application {
        mainClass =
            "me.calebjones.spacelaunchnow.MainKt" // Correct path to the Kotlin file containing the main function
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SpaceLaunchNow"
            packageVersion = computeVersionNameDesktop()
        }
    }
}

// OpenAPI Code Generation Configuration for Launch Library 2.4.0
openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/../schema/ll_2.4.0.json")
    outputDir.set("$projectDir/src/openApiLL")
    configFile.set("$projectDir/openapi-config-ll.yaml")
}

// Register separate task for SNAPI v4 generation
tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("generateSnapiClient") {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/../schema/snapi_v4.yaml")
    outputDir.set("$projectDir/src/openApiSNAPI")
    configFile.set("$projectDir/openapi-config-snapi.yaml")
}

// Convenience task to generate both API clients
tasks.register("generateAllApiClients") {
    dependsOn("openApiGenerate", "generateSnapiClient")
    description = "Generate both Launch Library 2.4.0 and SNAPI v4 API clients"
}

tasks.whenTaskAdded {
    if (name.startsWith("package") && name.endsWith("Debug")) {
        doLast {
            val buildType = "debug"
            val versionName = android.defaultConfig.versionName
            val apkDir = file("$buildDir/outputs/apk/$buildType/")
            apkDir.listFiles()?.forEach { apk ->
                if (apk.name.endsWith(".apk")) {
                    val newName = "spacelaunchnow-kmp-v${versionName}-${buildType}.apk"
                    apk.renameTo(File(apkDir, newName))
                }
            }
        }
    }
    if (name.startsWith("package") && name.endsWith("Release")) {
        doLast {
            val buildType = "release"
            val versionName = android.defaultConfig.versionName
            val apkDir = file("$buildDir/outputs/apk/$buildType/")
            apkDir.listFiles()?.forEach { apk ->
                if (apk.name.endsWith(".apk")) {
                    val newName = "spacelaunchnow-kmp-v${versionName}-${buildType}.apk"
                    apk.renameTo(File(apkDir, newName))
                }
            }
        }
    }
    if (name.startsWith("bundle") && name.endsWith("Debug")) {
        doLast {
            val buildType = "debug"
            val versionName = android.defaultConfig.versionName
            val bundleDir = file("$buildDir/outputs/bundle/$buildType/")
            bundleDir.listFiles()?.forEach { bundle ->
                if (bundle.name.endsWith(".aab")) {
                    val newName = "spacelaunchnow-kmp-v${versionName}-${buildType}.aab"
                    bundle.renameTo(File(bundleDir, newName))
                }
            }
        }
    }
    if (name.startsWith("bundle") && name.endsWith("Release")) {
        doLast {
            val buildType = "release"
            val versionName = android.defaultConfig.versionName
            val bundleDir = file("$buildDir/outputs/bundle/$buildType/")
            bundleDir.listFiles()?.forEach { bundle ->
                if (bundle.name.endsWith(".aab")) {
                    val newName = "spacelaunchnow-kmp-v${versionName}-${buildType}.aab"
                    bundle.renameTo(File(bundleDir, newName))
                }
            }
        }
    }
}