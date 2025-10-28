# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Koin
-keep class org.koin.** { *; }

# Keep RevenueCat
-keep class com.revenuecat.** { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }

# Keep serialization classes
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# Keep API models (generated from OpenAPI)
-keep class me.calebjones.spacelaunchnow.api.** { *; }

# Keep data models
-keep class me.calebjones.spacelaunchnow.data.** { *; }

# Remove debug logging in release builds
-assumenosideeffects class kotlin.io.ConsoleKt {
    public static *** println(...);
}

# Optimize and remove unused code
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Keep line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable

# Keep generic signatures for reflection
-keepattributes Signature

# Keep annotations
-keepattributes *Annotation*