import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.openApiGenerator)
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
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.koin.android)
                implementation(libs.androidx.palette)
                implementation(libs.coil.compose.android)
                implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

                // Firebase dependencies with explicit versions
                implementation("com.google.firebase:firebase-messaging-ktx:24.1.0")
                implementation("androidx.work:work-runtime-ktx:2.10.0")
            }
        }

          iosMain {

              dependencies{
                  implementation(libs.coil.compose.ios)
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

                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.koin.compose.viewmodel.nav)

                implementation(libs.ktor.client.core)          // Core client dependency
                implementation(libs.ktor.client.cio)           // CIO engine for Ktor
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
            }
        }
        
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
                implementation("io.ktor:ktor-client-mock:2.3.12")
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
        applicationId = "me.calebjones.spacelaunchnow.kmp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
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
        mainClass = "me.calebjones.spacelaunchnow.MainKt" // Correct path to the Kotlin file containing the main function
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SpaceLaunchNow"
            packageVersion = "1.0.0"
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