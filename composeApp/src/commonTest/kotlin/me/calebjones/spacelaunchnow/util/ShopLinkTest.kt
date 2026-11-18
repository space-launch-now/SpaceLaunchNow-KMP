package me.calebjones.spacelaunchnow.util

import kotlin.test.Test
import kotlin.test.assertTrue

class ShopLinkTest {
    @Test
    fun urlIsHttpsShopHostWithAppSource() {
        assertTrue(ShopLink.URL.startsWith("https://shop.spacelaunchnow.app/"))
        assertTrue(ShopLink.URL.contains("utm_source=app"))
    }
}
