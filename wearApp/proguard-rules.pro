# Wear OS app — keep rules alongside the phone app's proguard-rules.pro

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Koin
-keep class org.koin.** { *; }

# Wear OS DataLayer
-keep class com.google.android.gms.wearable.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }
