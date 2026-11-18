package me.calebjones.spacelaunchnow.util

import kotlin.test.Test
import kotlin.test.assertEquals

class ShopLinkTest {
    @Test
    fun urlPointsToShopWithAppSource() {
        assertEquals("https://shop.spacelaunchnow.app/?utm_source=app", ShopLink.URL)
    }
}
