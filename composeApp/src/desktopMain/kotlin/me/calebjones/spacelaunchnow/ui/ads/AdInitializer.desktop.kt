package me.calebjones.spacelaunchnow.ui.ads

import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Desktop implementation of AdInitializer (no-op)
 * Desktop doesn't support ads, so all operations are no-ops
 */
actual object AdInitializer {
    private val log = logger()
    actual val isSupported: Boolean = false
    
    actual fun initialize(context: Any?): Boolean {
        log.d { "🎯 AdInitializer (Desktop): Ads not supported on desktop, skipping initialization" }
        return false
    }
    
    actual fun configure(isDebug: Boolean, testDeviceIds: List<String>) {
        log.d { "🎯 AdInitializer (Desktop): Ads not supported on desktop, skipping configuration" }
    }
}
