package me.calebjones.spacelaunchnow.util.logging

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class UserContextTest {

    @BeforeTest
    fun setUp() = UserContext.clear()

    @AfterTest
    fun tearDown() = UserContext.clear()

    @Test
    fun logAttributesCarryRcUserIdAndPremiumOnceSet() {
        UserContext.setPremiumStatus(true)
        UserContext.setRevenueCatUserId("\$RCAnonymousID:abc123")

        val attrs = UserContext.getLogAttributes()
        assertEquals("\$RCAnonymousID:abc123", attrs["rc_user_id"])
        assertEquals(true, attrs["is_premium"])
    }

    @Test
    fun logAttributesOmitRcUserIdWhenUnset() {
        val attrs = UserContext.getLogAttributes()
        assertFalse(attrs.containsKey("rc_user_id"))
        assertEquals(false, attrs["is_premium"])
    }

    @Test
    fun clearResetsAttributes() {
        UserContext.setPremiumStatus(true)
        UserContext.setRevenueCatUserId("user-1")
        UserContext.clear()

        val attrs = UserContext.getLogAttributes()
        assertFalse(attrs.containsKey("rc_user_id"))
        assertEquals(false, attrs["is_premium"])
    }

    @Test
    fun setBillingIdentitySetsBothFlowsAndAttributes() {
        UserContext.setBillingIdentity("user-2", true)

        assertEquals("user-2", UserContext.revenueCatUserId.value)
        assertEquals(true, UserContext.isPremium.value)
        val attrs = UserContext.getLogAttributes()
        assertEquals("user-2", attrs["rc_user_id"])
        assertEquals(true, attrs["is_premium"])
    }

    @Test
    fun setBillingIdentityAcceptsNullUserId() {
        UserContext.setBillingIdentity("user-2", true)
        UserContext.setBillingIdentity(null, false)

        val attrs = UserContext.getLogAttributes()
        assertFalse(attrs.containsKey("rc_user_id"))
        assertEquals(false, attrs["is_premium"])
    }
}
