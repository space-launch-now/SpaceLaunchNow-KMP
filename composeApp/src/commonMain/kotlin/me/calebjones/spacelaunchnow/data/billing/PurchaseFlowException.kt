package me.calebjones.spacelaunchnow.data.billing

/**
 * Platform-neutral purchase failure carrying the coarse funnel step and error code
 * for the purchase_failed analytics event (spec 018 FR-1.1). Platform billing
 * managers wrap RevenueCat's typed exceptions into this so commonMain code can
 * attribute failures without depending on purchases-kmp (which has no desktop target).
 *
 * @param step "setup" (offerings/product lookup) or "store_purchase" (native store flow)
 * @param errorCode "user_cancelled" for sheet dismissal, else the RevenueCat
 *   PurchasesErrorCode name — coarse, never PII
 * @param userCancelled true when the user dismissed the native purchase sheet
 */
class PurchaseFlowException(
    val step: String,
    val errorCode: String,
    val userCancelled: Boolean,
    message: String?,
    cause: Throwable? = null
) : Exception(message, cause)
