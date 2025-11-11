package me.calebjones.spacelaunchnow.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ProductInfoTest {
    
    @Test
    fun `ProductInfo should store all properties correctly`() {
        val product = ProductInfo(
            productId = "monthly_sub",
            basePlanId = "base-monthly",
            title = "Monthly Premium",
            description = "MONTHLY - Month",
            formattedPrice = "$4.99",
            priceAmountMicros = 4990000L,
            currencyCode = "USD"
        )
        
        assertEquals("monthly_sub", product.productId)
        assertEquals("base-monthly", product.basePlanId)
        assertEquals("Monthly Premium", product.title)
        assertEquals("MONTHLY - Month", product.description)
        assertEquals("$4.99", product.formattedPrice)
        assertEquals(4990000L, product.priceAmountMicros)
        assertEquals("USD", product.currencyCode)
    }
    
    @Test
    fun `ProductInfo with null basePlanId should be allowed`() {
        val product = ProductInfo(
            productId = "lifetime",
            basePlanId = null,
            title = "Lifetime Access",
            description = "One-time purchase",
            formattedPrice = "$49.99",
            priceAmountMicros = 49990000L,
            currencyCode = "USD"
        )
        
        assertEquals(null, product.basePlanId)
        assertEquals("lifetime", product.productId)
    }
    
    @Test
    fun `ProductInfo with different currencies should be supported`() {
        val usdProduct = ProductInfo(
            productId = "test",
            basePlanId = null,
            title = "Test",
            description = "Test",
            formattedPrice = "$4.99",
            priceAmountMicros = 4990000L,
            currencyCode = "USD"
        )
        
        val eurProduct = ProductInfo(
            productId = "test",
            basePlanId = null,
            title = "Test",
            description = "Test",
            formattedPrice = "€4.99",
            priceAmountMicros = 4990000L,
            currencyCode = "EUR"
        )
        
        assertEquals("USD", usdProduct.currencyCode)
        assertEquals("EUR", eurProduct.currencyCode)
        assertEquals("$4.99", usdProduct.formattedPrice)
        assertEquals("€4.99", eurProduct.formattedPrice)
    }
    
    @Test
    fun `ProductInfo equality should work correctly`() {
        val product1 = ProductInfo(
            productId = "monthly",
            basePlanId = "base",
            title = "Monthly",
            description = "Monthly subscription",
            formattedPrice = "$4.99",
            priceAmountMicros = 4990000L,
            currencyCode = "USD"
        )
        
        val product2 = ProductInfo(
            productId = "monthly",
            basePlanId = "base",
            title = "Monthly",
            description = "Monthly subscription",
            formattedPrice = "$4.99",
            priceAmountMicros = 4990000L,
            currencyCode = "USD"
        )
        
        val product3 = ProductInfo(
            productId = "yearly",
            basePlanId = "base",
            title = "Yearly",
            description = "Yearly subscription",
            formattedPrice = "$39.99",
            priceAmountMicros = 39990000L,
            currencyCode = "USD"
        )
        
        assertEquals(product1, product2)
        assertNotEquals(product1, product3)
    }
    
    @Test
    fun `ProductInfo copy should work correctly`() {
        val original = ProductInfo(
            productId = "monthly",
            basePlanId = "base",
            title = "Monthly",
            description = "Monthly subscription",
            formattedPrice = "$4.99",
            priceAmountMicros = 4990000L,
            currencyCode = "USD"
        )
        
        val modified = original.copy(formattedPrice = "$5.99", priceAmountMicros = 5990000L)
        
        assertEquals("monthly", modified.productId)
        assertEquals("$5.99", modified.formattedPrice)
        assertEquals(5990000L, modified.priceAmountMicros)
        assertEquals("$4.99", original.formattedPrice) // Original unchanged
    }
    
    @Test
    fun `ProductInfo with various price formats should be supported`() {
        val products = listOf(
            ProductInfo(
                productId = "test1",
                basePlanId = null,
                title = "Test",
                description = "Test",
                formattedPrice = "Free",
                priceAmountMicros = 0L,
                currencyCode = "USD"
            ),
            ProductInfo(
                productId = "test2",
                basePlanId = null,
                title = "Test",
                description = "Test",
                formattedPrice = "$0.99",
                priceAmountMicros = 990000L,
                currencyCode = "USD"
            ),
            ProductInfo(
                productId = "test3",
                basePlanId = null,
                title = "Test",
                description = "Test",
                formattedPrice = "$999.99",
                priceAmountMicros = 999990000L,
                currencyCode = "USD"
            )
        )
        
        assertEquals("Free", products[0].formattedPrice)
        assertEquals(0L, products[0].priceAmountMicros)
        assertEquals("$0.99", products[1].formattedPrice)
        assertEquals(990000L, products[1].priceAmountMicros)
        assertEquals("$999.99", products[2].formattedPrice)
        assertEquals(999990000L, products[2].priceAmountMicros)
    }
}
