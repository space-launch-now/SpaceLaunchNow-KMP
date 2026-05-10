package me.calebjones.spacelaunchnow.data.billing

/** Desktop has no RevenueCat SDK — these are intentional no-ops. */
internal actual fun platformSetAttributes(attributes: Map<String, String?>) = Unit

internal actual fun platformSetPushToken(token: String) = Unit
