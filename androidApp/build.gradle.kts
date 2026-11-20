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

fun computeVersionCode(): Int {
    val buildNumber = versionProps["versionBuildNumber"].toString().toInt()
    // Monotonic counter scheme — the previous semver-encoded formula overflowed slots
    // when patch hit double digits (5.28.11 outranked 5.29.0). versionCode no longer
    // encodes major/minor/patch (versionName already does that); it just needs to be a
    // forward-only integer per applicationId.
    //
    // Phone takes the even slot, wear takes the odd slot above it (see wearApp). Bump
    // versionBuildNumber by 1 per release; both APKs get unique forward-only codes.
    //
    // Base 1_100_000_000 sits above the highest previously-published code for this
    // applicationId (wear 1_007_910_066) so the new scheme stays monotonic across the
    // package's full release history.
    return 1_100_000_000 + (buildNumber * 2)
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

// Load keystore properties (optional for local development)
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
val hasKeystoreProperties = keystorePropertiesFile.canRead()

if (hasKeystoreProperties) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

android {
    signingConfigs {
        if (hasKeystoreProperties) {
            create("releaseConfig") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }
    namespace = "me.calebjones.spacelaunchnow"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

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
        // Google Maps API Key
        val mapsApiKey = envProps.getProperty("MAPS_API_KEY") ?: ""

        // AdMob App ID (production ID set in .env; debug builds use Google's test App ID)
        val admobAppId = envProps.getProperty("ADMOB_APP_ID") ?: ""

        // The other .env values (API keys, ad unit IDs, Datadog) reach Android code through
        // AndroidEnvConfig, generated in :composeApp. Only variant data stays in BuildConfig.
        // Version information, read by MainApplication and handed to the shared module
        buildConfigField("String", "VERSION_NAME", "\"${computeVersionName()}\"")
        buildConfigField("int", "VERSION_CODE", "${computeVersionCode()}")

        versionCode = computeVersionCode()
        versionName = computeVersionName()
        manifestPlaceholders["appName"] = "Space Launch Now"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        manifestPlaceholders["ADMOB_APP_ID"] = admobAppId
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
            if (hasKeystoreProperties) {
                signingConfig = signingConfigs.getByName("releaseConfig")
            }
        }
        getByName("debug") {
            applicationIdSuffix = ".kmpdebug"
            versionNameSuffix = "-DEBUG"
            buildConfigField("boolean", "IS_DEBUG", "true")
            manifestPlaceholders["appName"] = "@string/app_name_debug"
            // Debug builds use Google's standard test App ID
            manifestPlaceholders["ADMOB_APP_ID"] = "ca-app-pub-3940256099942544~3347511713"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests {
            // AGP 9 defaults this to false; the Compose preview tooling needs compiled resources.
            isIncludeAndroidResources = true
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Shared Kotlin Multiplatform code and the Android code behind its expect declarations.
    implementation(projects.composeApp)

    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.work.runtime)
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.coil.compose)
    implementation(libs.compose.media.player)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kermit)

    // The Crashlytics Gradle plugin and the manifest need the SDK on this module's classpath.
    implementation(platform(libs.firebase.bom))
    implementation(libs.android.firebase.analytics)
    implementation(libs.android.firebase.crashlytics)

    debugImplementation(compose.uiTooling)
}

// AGP 9 Compose Preview packaging needs host (unit) tests on every variant, release included.
androidComponents {
    beforeVariants { variantBuilder ->
        variantBuilder.hostTests[com.android.build.api.variant.HostTestBuilder.UNIT_TEST_TYPE]?.enable = true
    }
}

// Helper to safely copy build outputs (APKs/AABs). Checks for existence, avoids copying
// when source == destination, and catches IO errors to prevent task failure.
fun safeCopyOutputFiles(
    outputsDir: File,
    extension: String,
    versionName: String,
    buildType: String
) {
    try {
        if (!outputsDir.exists() || !outputsDir.isDirectory) {
            logger.debug("Output directory does not exist: ${outputsDir.absolutePath}")
            return
        }
        val files = outputsDir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(extension, ignoreCase = true) }
        if (files.isNullOrEmpty()) {
            logger.debug("No output files with extension '$extension' found in ${outputsDir.absolutePath}")
            return
        }
        files.forEach { src ->
            val newName = "spacelaunchnow-kmp-v${versionName}-${buildType}${extension}"
            val dest = File(outputsDir, newName)
            // If the source file already has the target name, skip to avoid FileAlreadyExistsException
            if (src.absoluteFile == dest.absoluteFile) {
                logger.debug("Source already matches destination name, skipping: ${src.name}")
                return@forEach
            }
            try {
                src.copyTo(dest, overwrite = true)
                logger.lifecycle("Copied ${src.name} -> ${dest.name}")
            } catch (e: java.io.IOException) {
                logger.warn("I/O error copying ${src.absolutePath} to ${dest.absolutePath}: ${e.message}")
            } catch (e: Exception) {
                logger.warn("Unexpected error copying ${src.absolutePath} to ${dest.absolutePath}: ${e.message}")
            }
        }
    } catch (e: Exception) {
        logger.warn("safeCopyOutputFiles encountered an error: ${e.message}")
    }
}

// Replace the previous inline copy logic with calls to the safe helper
tasks.whenTaskAdded {
    if (name.startsWith("package") && name.endsWith("Debug")) {
        doLast {
            val buildType = "debug"
            val versionName = android.defaultConfig.versionName ?: computeVersionName()
            val apkDir = layout.buildDirectory.asFile.get().resolve("outputs/apk/$buildType/")
            safeCopyOutputFiles(apkDir, ".apk", versionName, buildType)
        }
    }
    if (name.startsWith("package") && name.endsWith("Release")) {
        doLast {
            val buildType = "release"
            val versionName = android.defaultConfig.versionName ?: computeVersionName()
            val apkDir = layout.buildDirectory.asFile.get().resolve("outputs/apk/$buildType/")
            safeCopyOutputFiles(apkDir, ".apk", versionName, buildType)
        }
    }
    if (name.startsWith("bundle") && name.endsWith("Debug")) {
        doLast {
            val buildType = "debug"
            val versionName = android.defaultConfig.versionName ?: computeVersionName()
            val bundleDir = layout.buildDirectory.asFile.get().resolve("outputs/bundle/$buildType/")
            safeCopyOutputFiles(bundleDir, ".aab", versionName, buildType)
        }
    }
    if (name.startsWith("bundle") && name.endsWith("Release")) {
        doLast {
            val buildType = "release"
            val versionName = android.defaultConfig.versionName ?: computeVersionName()
            val bundleDir = layout.buildDirectory.asFile.get().resolve("outputs/bundle/$buildType/")
            safeCopyOutputFiles(bundleDir, ".aab", versionName, buildType)
        }
    }
}
