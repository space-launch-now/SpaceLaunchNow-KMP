package me.calebjones.spacelaunchnow.data.billing

import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Production implementation of [RevenueCatAttributes].
 * Delegates to the platform-specific factory so that the RC SDK import
 * is confined to androidMain / iosMain (it is not available on Desktop).
 */
class DefaultRevenueCatAttributes : RevenueCatAttributes {

    private val log = logger()

    override fun set(attributes: Map<String, String?>) {
        try {
            platformSetAttributes(attributes)
            log.d { "RC attributes pushed: ${attributes.keys}" }
        } catch (e: Exception) {
            log.w(e) { "Failed to push RC attributes" }
        }
    }

    override fun setPushToken(token: String) {
        try {
            platformSetPushToken(token)
            log.i { "RC push token set (length=${token.length})" }
        } catch (e: Exception) {
            log.w(e) { "Failed to set RC push token" }
        }
    }
}

/**
 * Platform-specific attribute setter.
 * androidMain / iosMain call Purchases.sharedInstance.setAttributes(…);
 * desktopMain is a no-op.
 */
internal expect fun platformSetAttributes(attributes: Map<String, String?>)

/**
 * Platform-specific push-token setter.
 * androidMain / iosMain call Purchases.sharedInstance.setPushToken(…);
 * desktopMain is a no-op.
 */
internal expect fun platformSetPushToken(token: String)
