package me.calebjones.spacelaunchnow.ui.ads

/**
 * Desktop implementation of AdInitializer (no-op)
 * Desktop doesn't support ads, so all operations are no-ops
 */
actual object AdInitializer {
    actual val isSupported: Boolean = false
    
    actual fun initialize(context: Any?): Boolean {
        println("🎯 AdInitializer (Desktop): Ads not supported on desktop, skipping initialization")
        return false
    }
    
    actual fun configure(isDebug: Boolean, testDeviceIds: List<String>) {
        println("🎯 AdInitializer (Desktop): Ads not supported on desktop, skipping configuration")
    }
}
