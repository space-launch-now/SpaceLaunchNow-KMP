# Wear OS app — ProGuard / R8 rules
# Keep this in sync with composeApp/proguard-rules.pro for shared libraries.

# ---- Kotlin / Coroutines ----
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ---- Kotlin Serialization ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }
-keep @kotlinx.serialization.Serializable class * { *; }
-keep class kotlinx.serialization.** { *; }

# Keep companions for @Serializable classes (needed by reflective .serializer() lookups)
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# ---- kotlinx.datetime ----
-keep class kotlinx.datetime.** { *; }
-dontwarn kotlinx.datetime.**

# ---- Ktor ----
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**

# OkHttp/Okio (transitively used by some Ktor engines)
-dontwarn okhttp3.**
-dontwarn okio.**

# ---- Koin ----
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# ---- Wear OS / DataLayer / Wearable ----
-keep class com.google.android.gms.wearable.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**

# ---- Wear Tiles + ProtoLayout ----
-keep class androidx.wear.tiles.** { *; }
-keep class androidx.wear.protolayout.** { *; }
-dontwarn androidx.wear.tiles.**
-dontwarn androidx.wear.protolayout.**

# Protobuf (used by ProtoLayout/Tiles)
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# ---- Wear Watchface Complications ----
-keep class androidx.wear.watchface.** { *; }
-dontwarn androidx.wear.watchface.**

# ---- AndroidX DataStore ----
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ---- AndroidX WorkManager ----
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker { public <init>(...); }

# ---- App data + API models (kept by name, used by serialization) ----
-keep class me.calebjones.spacelaunchnow.api.** { *; }
-keep class me.calebjones.spacelaunchnow.data.** { *; }
-keep class me.calebjones.spacelaunchnow.domain.** { *; }
-keep class me.calebjones.spacelaunchnow.wear.** { *; }

# ---- Misc ----
-keepattributes Signature, SourceFile, LineNumberTable, InnerClasses, EnclosingMethod, *Annotation*
-renamesourcefileattribute SourceFile

# Strip println in release
-assumenosideeffects class kotlin.io.ConsoleKt {
    public static *** println(...);
}
