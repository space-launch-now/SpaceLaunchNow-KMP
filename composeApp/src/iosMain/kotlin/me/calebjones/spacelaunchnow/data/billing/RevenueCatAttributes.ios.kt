package me.calebjones.spacelaunchnow.data.billing

import com.revenuecat.purchases.kmp.Purchases

internal actual fun platformSetAttributes(attributes: Map<String, String?>) {
    Purchases.sharedInstance.setAttributes(attributes)
}

internal actual fun platformSetPushToken(token: String) {
    Purchases.sharedInstance.setPushToken(token)
}
