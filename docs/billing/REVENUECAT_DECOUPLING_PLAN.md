# RevenueCat Decoupling Architecture Plan

**Version:** 1.0  
**Date:** November 10, 2025  
**Status:** Draft for Implementation

---

## 📋 Executive Summary

This document outlines the plan to decouple RevenueCat from `commonMain` and implement it cleanly using proper KMP expect/actual architecture. The goal is to maintain all current functionality while improving code organization, testability, and platform-specific optimizations.

### Current State

- ✅ RevenueCat SDK installed for Android and iOS
- ✅ Basic functionality working (initialization, purchases, entitlements)
- ⚠️ **Problem:** Most billing logic is in `commonMain`, tightly coupled to RevenueCat
- ⚠️ **Problem:** Desktop has stub implementation that doesn't need RevenueCat
- ⚠️ **Problem:** Difficult to test without real RevenueCat connection
- ⚠️ **Problem:** Platform-specific optimizations are limited

### Desired State

- ✅ Clean separation of concerns using expect/actual
- ✅ Platform-agnostic interfaces in `commonMain`
- ✅ Platform-specific implementations in `androidMain` and `iosMain`
- ✅ Desktop gets no-op implementation without RevenueCat dependency
- ✅ Easy to test with mock implementations
- ✅ All current functionality preserved

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                           COMMON MAIN                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │                   BillingRepository                         │    │
│  │  (Platform-agnostic interface)                              │    │
│  │  - suspend fun initialize()                                 │    │
│  │  - suspend fun queryPurchases(): PurchaseState              │    │
│  │  - suspend fun launchPurchaseFlow(productId)                │    │
│  │  - suspend fun restorePurchases()                           │    │
│  │  - val purchaseState: StateFlow<PurchaseState>              │    │
│  └────────────────────────────────────────────────────────────┘    │
│                              ▲                                        │
│                              │                                        │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │            expect interface BillingManager                  │    │
│  │  (Platform-specific billing operations)                     │    │
│  └────────────────────────────────────────────────────────────┘    │
│                              │                                        │
│                              │                                        │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │                 Data Models (Common)                        │    │
│  │  - data class PurchaseState                                 │    │
│  │  - data class SubscriptionInfo                              │    │
│  │  - data class ProductInfo                                   │    │
│  │  - enum class SubscriptionType                              │    │
│  │  - enum class PremiumFeature                                │    │
│  └────────────────────────────────────────────────────────────┘    │
│                              │                                        │
│                              │                                        │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │              LocalSubscriptionStorage                       │    │
│  │  (KStore - platform-agnostic file storage)                  │    │
│  │  - Cache subscription state                                 │    │
│  │  - Persist across app restarts                              │    │
│  └────────────────────────────────────────────────────────────┘    │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
                                 │
                ┌────────────────┴────────────────┐
                │                                  │
┌───────────────▼───────────────┐  ┌──────────────▼──────────────┐
│       ANDROID MAIN             │  │         IOS MAIN            │
├────────────────────────────────┤  ├─────────────────────────────┤
│                                │  │                             │
│ ┌────────────────────────────┐ │  │ ┌─────────────────────────┐ │
│ │ actual class               │ │  │ │ actual class            │ │
│ │ AndroidBillingManager      │ │  │ │ IosBillingManager       │ │
│ │                            │ │  │ │                         │ │
│ │ Uses:                      │ │  │ │ Uses:                   │ │
│ │ - RevenueCat SDK           │ │  │ │ - RevenueCat SDK        │ │
│ │ - Android Context          │ │  │ │ - iOS Platform APIs     │ │
│ │ - Android-specific config  │ │  │ │ - iOS-specific config   │ │
│ └────────────────────────────┘ │  │ └─────────────────────────┘ │
│                                │  │                             │
│ ┌────────────────────────────┐ │  │ ┌─────────────────────────┐ │
│ │ RevenueCatConfig.android   │ │  │ │ RevenueCatConfig.ios    │ │
│ │ - API Key from BuildConfig │ │  │ │ - API Key from Secrets  │ │
│ └────────────────────────────┘ │  │ └─────────────────────────┘ │
│                                │  │                             │
└────────────────────────────────┘  └─────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                         DESKTOP MAIN                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌────────────────────────────────────────────────────────────┐    │
│  │      actual class DesktopBillingManager                     │    │
│  │                                                              │    │
│  │      - No RevenueCat dependency                              │    │
│  │      - Returns FREE state always                             │    │
│  │      - No-op implementations                                 │    │
│  └────────────────────────────────────────────────────────────┘    │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📦 Current Implementation Analysis

### Files Currently in CommonMain (Need to Relocate)

1. **`RevenueCatManager.kt`** (576 lines)
   - ❌ Directly uses `com.revenuecat.purchases.kmp.Purchases`
   - ❌ Platform-specific initialization logic
   - ❌ Extensive logging and error handling tied to RevenueCat SDK
   - **Action:** Split into expect/actual pattern

2. **`RevenueCatBillingClient.kt`** (262 lines)
   - ❌ Wraps RevenueCat SDK calls
   - ❌ Converts RevenueCat types to platform-agnostic types
   - **Action:** Move to platform-specific implementations

3. **`BillingClient.kt`** (284 lines)
   - ✅ Already a wrapper class (good!)
   - ⚠️ Still tightly coupled to `RevenueCatBillingClient`
   - **Action:** Convert to use expect/actual `BillingManager`

### Files That Should Stay in CommonMain

1. **`data/model/PlatformPurchase.kt`**
   - ✅ Platform-agnostic data model
   
2. **`data/model/SubscriptionType.kt`**
   - ✅ Enum for subscription tiers
   
3. **`data/model/PremiumFeature.kt`**
   - ✅ Enum for feature access

4. **`data/subscription/LocalSubscriptionStorage.kt`**
   - ✅ Uses KStore (platform-agnostic)
   
5. **`data/subscription/SubscriptionSyncer.kt`**
   - ⚠️ References `RevenueCatManager` directly
   - **Action:** Update to use expect/actual `BillingManager`

6. **`data/repository/SubscriptionRepository.kt` (interface)**
   - ✅ Platform-agnostic interface

7. **`data/repository/SimpleSubscriptionRepository.kt`**
   - ⚠️ References `BillingClient`
   - **Action:** Update to use new architecture

---

## 🎯 Implementation Plan

### Phase 1: Define Platform-Agnostic Interfaces (2-3 hours)

#### 1.1 Create Core Interface in CommonMain

**File:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/BillingManager.kt`

```kotlin
package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.flow.StateFlow
import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.model.ProductInfo

/**
 * Platform-agnostic billing manager interface
 * Implementations handle RevenueCat on iOS/Android, no-op on Desktop
 */
interface BillingManager {
    
    /**
     * Initialization state
     */
    val isInitialized: StateFlow<Boolean>
    
    /**
     * Current purchase state
     * Emits whenever entitlements change
     */
    val purchaseState: StateFlow<PurchaseState>
    
    /**
     * Initialize the billing system
     * Must be called before any other operations
     */
    suspend fun initialize(appUserId: String? = null): Result<Unit>
    
    /**
     * Refresh purchase state from server
     * Returns true if successful
     */
    suspend fun refreshPurchaseState(): Boolean
    
    /**
     * Get available products for purchase
     */
    suspend fun getAvailableProducts(): Result<List<ProductInfo>>
    
    /**
     * Launch purchase flow for a product
     */
    suspend fun launchPurchaseFlow(productId: String, basePlanId: String? = null): Result<Unit>
    
    /**
     * Restore previous purchases
     * Useful for users who reinstalled the app
     */
    suspend fun restorePurchases(): Result<PurchaseState>
    
    /**
     * Check if user has a specific entitlement
     */
    fun hasEntitlement(entitlementId: String): Boolean
    
    /**
     * Get all active entitlements
     */
    fun getActiveEntitlements(): Set<String>
    
    /**
     * Sync purchases from store (silent, no UI)
     */
    suspend fun syncPurchases()
}

/**
 * Factory function to create platform-specific BillingManager
 */
expect fun createBillingManager(): BillingManager
```

#### 1.2 Update Data Models

**File:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/PurchaseState.kt`

```kotlin
package me.calebjones.spacelaunchnow.data.model

/**
 * Comprehensive purchase state
 * Platform-agnostic representation of subscription status
 */
data class PurchaseState(
    val isSubscribed: Boolean = false,
    val subscriptionType: SubscriptionType = SubscriptionType.FREE,
    val activeEntitlements: Set<String> = emptySet(),
    val activeProductIds: Set<String> = emptySet(),
    val features: Set<PremiumFeature> = emptySet(),
    val lastRefreshed: Long = 0L,
    val userId: String? = null
)
```

**File:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/ProductInfo.kt`

```kotlin
package me.calebjones.spacelaunchnow.data.model

/**
 * Product information for in-app purchases
 */
data class ProductInfo(
    val productId: String,
    val basePlanId: String?,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val priceAmountMicros: Long,
    val currencyCode: String
)
```

---

### Phase 2: Implement Android Billing Manager (4-5 hours)

#### 2.1 Create Android Implementation

**File:** `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/data/billing/AndroidBillingManager.kt`

```kotlin
package me.calebjones.spacelaunchnow.data.billing

import android.content.Context
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import me.calebjones.spacelaunchnow.BuildConfig
import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import kotlin.coroutines.resume

/**
 * Android implementation using RevenueCat
 */
class AndroidBillingManager(
    private val context: Context
) : BillingManager {
    
    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _purchaseState = MutableStateFlow(PurchaseState())
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    private val purchases: Purchases
        get() = Purchases.sharedInstance
    
    override suspend fun initialize(appUserId: String?): Result<Unit> {
        return try {
            // Configure RevenueCat
            Purchases.logLevel = if (BuildConfig.IS_DEBUG) LogLevel.DEBUG else LogLevel.WARN
            
            Purchases.configure(apiKey = BuildConfig.REVENUECAT_ANDROID_KEY) {
                this.appUserId = appUserId
            }
            
            _isInitialized.value = true
            
            // Initial sync
            syncPurchases()
            refreshPurchaseState()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun refreshPurchaseState(): Boolean {
        return try {
            val customerInfo = purchases.awaitCustomerInfo()
            updatePurchaseState(customerInfo)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getAvailableProducts(): Result<List<ProductInfo>> {
        return try {
            val offerings = purchases.awaitOfferings()
            val products = offerings.current?.availablePackages?.map { pkg ->
                ProductInfo(
                    productId = pkg.storeProduct.id,
                    basePlanId = pkg.identifier,
                    title = pkg.storeProduct.title,
                    description = pkg.storeProduct.description,
                    formattedPrice = pkg.storeProduct.price.formatted,
                    priceAmountMicros = pkg.storeProduct.price.amountMicros.toLong(),
                    currencyCode = pkg.storeProduct.price.currencyCode
                )
            } ?: emptyList()
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun launchPurchaseFlow(productId: String, basePlanId: String?): Result<Unit> {
        return try {
            val offerings = purchases.awaitOfferings()
            val pkg = offerings.current?.availablePackages?.find { 
                it.storeProduct.id == productId && 
                (basePlanId == null || it.identifier == basePlanId)
            } ?: return Result.failure(IllegalArgumentException("Product not found"))
            
            val result = purchases.awaitPurchase(pkg)
            updatePurchaseState(result.customerInfo)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun restorePurchases(): Result<PurchaseState> {
        return suspendCancellableCoroutine { continuation ->
            purchases.restorePurchases(
                onError = { error ->
                    continuation.resume(Result.failure(Exception(error.message)))
                },
                onSuccess = { customerInfo ->
                    updatePurchaseState(customerInfo)
                    continuation.resume(Result.success(_purchaseState.value))
                }
            )
        }
    }
    
    override suspend fun syncPurchases() {
        suspendCancellableCoroutine { continuation ->
            purchases.syncPurchases(
                onError = { continuation.resume(Unit) },
                onSuccess = { customerInfo ->
                    updatePurchaseState(customerInfo)
                    continuation.resume(Unit)
                }
            )
        }
    }
    
    override fun hasEntitlement(entitlementId: String): Boolean {
        return _purchaseState.value.activeEntitlements.contains(entitlementId)
    }
    
    override fun getActiveEntitlements(): Set<String> {
        return _purchaseState.value.activeEntitlements
    }
    
    private fun updatePurchaseState(customerInfo: com.revenuecat.purchases.kmp.models.CustomerInfo) {
        val activeEntitlements = customerInfo.entitlements.active.keys
        val productIds = buildSet {
            customerInfo.entitlements.active.values.forEach { 
                it.productIdentifier?.let { add(it) }
            }
            addAll(customerInfo.activeSubscriptions)
            customerInfo.nonSubscriptionTransactions.forEach { 
                add(it.productIdentifier) 
            }
        }
        
        val subscriptionType = determineSubscriptionType(activeEntitlements)
        val features = determineFeatures(subscriptionType)
        
        _purchaseState.value = PurchaseState(
            isSubscribed = subscriptionType != SubscriptionType.FREE,
            subscriptionType = subscriptionType,
            activeEntitlements = activeEntitlements,
            activeProductIds = productIds,
            features = features,
            lastRefreshed = System.currentTimeMillis(),
            userId = customerInfo.originalAppUserId
        )
    }
    
    private fun determineSubscriptionType(entitlements: Set<String>): SubscriptionType {
        return when {
            entitlements.contains("premium") -> SubscriptionType.PREMIUM
            entitlements.contains("founder") -> SubscriptionType.LIFETIME
            else -> SubscriptionType.FREE
        }
    }
    
    private fun determineFeatures(type: SubscriptionType): Set<PremiumFeature> {
        return when (type) {
            SubscriptionType.FREE -> emptySet()
            SubscriptionType.PREMIUM, SubscriptionType.LIFETIME -> PremiumFeature.entries.toSet()
        }
    }
}

/**
 * Factory function for Android
 */
actual fun createBillingManager(): BillingManager {
    // Context must be injected via Koin
    error("Use createBillingManager(context) for Android")
}

/**
 * Android-specific factory with context
 */
fun createBillingManager(context: Context): BillingManager {
    return AndroidBillingManager(context)
}
```

#### 2.2 Update Android Koin Module

**File:** `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/di/KoinAndroid.kt`

```kotlin
actual fun nativeConfig(): KoinAppDeclaration = {
    modules(
        module {
            // Platform-specific BillingManager
            single<BillingManager> {
                createBillingManager(androidContext())
            }
            
            // ... rest of Android modules
        }
    )
}
```

---

### Phase 3: Implement iOS Billing Manager (4-5 hours)

#### 3.1 Create iOS Implementation

**File:** `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/billing/IosBillingManager.kt`

```kotlin
package me.calebjones.spacelaunchnow.data.billing

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.util.AppSecrets
import kotlin.coroutines.resume

/**
 * iOS implementation using RevenueCat
 */
class IosBillingManager : BillingManager {
    
    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _purchaseState = MutableStateFlow(PurchaseState())
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    private val purchases: Purchases
        get() = Purchases.sharedInstance
    
    override suspend fun initialize(appUserId: String?): Result<Unit> {
        return try {
            // Configure RevenueCat with iOS key from Secrets.plist
            Purchases.logLevel = LogLevel.DEBUG // iOS is always debug for now
            
            Purchases.configure(apiKey = AppSecrets.revenueCatIosKey) {
                this.appUserId = appUserId
            }
            
            _isInitialized.value = true
            
            // Initial sync
            syncPurchases()
            refreshPurchaseState()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Similar implementation to Android...
    // (Copy methods from AndroidBillingManager, adjust as needed)
    
    override suspend fun refreshPurchaseState(): Boolean {
        return try {
            val customerInfo = purchases.awaitCustomerInfo()
            updatePurchaseState(customerInfo)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ... rest of methods identical to Android ...
    
    private fun updatePurchaseState(customerInfo: com.revenuecat.purchases.kmp.models.CustomerInfo) {
        // Same logic as Android
        val activeEntitlements = customerInfo.entitlements.active.keys
        val productIds = buildSet {
            customerInfo.entitlements.active.values.forEach { 
                it.productIdentifier?.let { add(it) }
            }
            addAll(customerInfo.activeSubscriptions)
            customerInfo.nonSubscriptionTransactions.forEach { 
                add(it.productIdentifier) 
            }
        }
        
        val subscriptionType = determineSubscriptionType(activeEntitlements)
        val features = determineFeatures(subscriptionType)
        
        _purchaseState.value = PurchaseState(
            isSubscribed = subscriptionType != SubscriptionType.FREE,
            subscriptionType = subscriptionType,
            activeEntitlements = activeEntitlements,
            activeProductIds = productIds,
            features = features,
            lastRefreshed = System.currentTimeMillis(),
            userId = customerInfo.originalAppUserId
        )
    }
    
    private fun determineSubscriptionType(entitlements: Set<String>): SubscriptionType {
        return when {
            entitlements.contains("premium") -> SubscriptionType.PREMIUM
            entitlements.contains("founder") -> SubscriptionType.LIFETIME
            else -> SubscriptionType.FREE
        }
    }
    
    private fun determineFeatures(type: SubscriptionType): Set<PremiumFeature> {
        return when (type) {
            SubscriptionType.FREE -> emptySet()
            SubscriptionType.PREMIUM, SubscriptionType.LIFETIME -> PremiumFeature.entries.toSet()
        }
    }
}

/**
 * Factory function for iOS
 */
actual fun createBillingManager(): BillingManager {
    return IosBillingManager()
}
```

#### 3.2 Update iOS Koin Module

**File:** `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/di/KoinIos.kt`

```kotlin
actual fun nativeConfig(): KoinAppDeclaration = {
    modules(
        module {
            // Platform-specific BillingManager
            single<BillingManager> {
                createBillingManager()
            }
            
            // ... rest of iOS modules
        }
    )
}
```

---

### Phase 4: Implement Desktop No-Op Manager (1 hour)

#### 4.1 Create Desktop Implementation

**File:** `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/data/billing/DesktopBillingManager.kt`

```kotlin
package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.SubscriptionType

/**
 * Desktop implementation - No billing support
 * Desktop apps don't have in-app purchases
 */
class DesktopBillingManager : BillingManager {
    
    private val _isInitialized = MutableStateFlow(true) // Always initialized
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    // Always FREE on desktop
    private val _purchaseState = MutableStateFlow(
        PurchaseState(
            isSubscribed = false,
            subscriptionType = SubscriptionType.FREE,
            activeEntitlements = emptySet(),
            activeProductIds = emptySet(),
            features = emptySet(),
            lastRefreshed = System.currentTimeMillis()
        )
    )
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()
    
    override suspend fun initialize(appUserId: String?): Result<Unit> {
        println("DesktopBillingManager: No billing support on desktop")
        return Result.success(Unit)
    }
    
    override suspend fun refreshPurchaseState(): Boolean = true
    
    override suspend fun getAvailableProducts(): Result<List<ProductInfo>> {
        return Result.success(emptyList())
    }
    
    override suspend fun launchPurchaseFlow(productId: String, basePlanId: String?): Result<Unit> {
        return Result.failure(UnsupportedOperationException("In-app purchases not supported on desktop"))
    }
    
    override suspend fun restorePurchases(): Result<PurchaseState> {
        return Result.success(_purchaseState.value)
    }
    
    override suspend fun syncPurchases() {
        // No-op
    }
    
    override fun hasEntitlement(entitlementId: String): Boolean = false
    
    override fun getActiveEntitlements(): Set<String> = emptySet()
}

/**
 * Factory function for Desktop
 */
actual fun createBillingManager(): BillingManager {
    return DesktopBillingManager()
}
```

---

### Phase 5: Update Common Components (3-4 hours)

#### 5.1 Update BillingClient Wrapper

**File:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/BillingClient.kt`

```kotlin
package me.calebjones.spacelaunchnow.data.billing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.calebjones.spacelaunchnow.data.model.PlatformPurchase
import me.calebjones.spacelaunchnow.data.model.ProductPricing

/**
 * Legacy BillingClient wrapper
 * Maintains backward compatibility while using new BillingManager
 * 
 * NOTE: This class exists for backward compatibility during migration.
 * New code should use BillingManager directly.
 */
class BillingClient(
    private val billingManager: BillingManager
) {
    
    suspend fun initialize(): Result<Unit> = billingManager.initialize()
    
    suspend fun queryPurchases(): Result<List<PlatformPurchase>> {
        val state = billingManager.purchaseState.value
        return Result.success(
            if (state.isSubscribed) {
                listOf(
                    PlatformPurchase(
                        productId = state.activeProductIds.firstOrNull() ?: "",
                        purchaseToken = "",
                        isAcknowledged = true
                    )
                )
            } else {
                emptyList()
            }
        )
    }
    
    suspend fun launchPurchaseFlow(productId: String, basePlanId: String? = null): Result<String> {
        return billingManager.launchPurchaseFlow(productId, basePlanId).map { "" }
    }
    
    suspend fun acknowledgePurchase(purchaseToken: String): Result<Unit> {
        // RevenueCat auto-acknowledges
        return Result.success(Unit)
    }
    
    suspend fun getAvailableProducts(): Result<List<String>> {
        return billingManager.getAvailableProducts().map { products ->
            products.map { it.productId }
        }
    }
    
    suspend fun getProductPricing(productId: String): Result<List<ProductPricing>> {
        return billingManager.getAvailableProducts().map { products ->
            products.filter { it.productId == productId }.map { product ->
                ProductPricing(
                    productId = product.productId,
                    basePlanId = product.basePlanId,
                    formattedPrice = product.formattedPrice,
                    priceAmountMicros = product.priceAmountMicros,
                    priceCurrencyCode = product.currencyCode
                )
            }
        }
    }
    
    val purchaseUpdates: Flow<PlatformPurchase> = billingManager.purchaseState.map { state ->
        PlatformPurchase(
            productId = state.activeProductIds.firstOrNull() ?: "",
            purchaseToken = "",
            isAcknowledged = true
        )
    }
    
    fun disconnect() {
        // No-op - BillingManager handles lifecycle
    }
}
```

#### 5.2 Update SubscriptionSyncer

**File:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/SubscriptionSyncer.kt`

```kotlin
package me.calebjones.spacelaunchnow.data.subscription

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.BillingManager

/**
 * Handles syncing subscription data between local storage and billing system
 */
class SubscriptionSyncer(
    private val localStorage: LocalSubscriptionStorage,
    private val billingManager: BillingManager
) {
    
    private val syncScope = CoroutineScope(SupervisorJob())
    private var lastSyncTime = 0L
    private val syncCooldownMs = 1000L
    
    fun startSyncing() {
        syncScope.launch {
            // Observe purchase state changes
            billingManager.purchaseState.collect { purchaseState ->
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastSyncTime > syncCooldownMs) {
                    lastSyncTime = currentTime
                    
                    // Update local storage
                    localStorage.update(
                        isSubscribed = purchaseState.isSubscribed,
                        subscriptionType = purchaseState.subscriptionType,
                        productIds = purchaseState.activeProductIds,
                        availableFeatures = purchaseState.features,
                        needsSync = false
                    )
                }
            }
        }
    }
    
    suspend fun syncNow(): Boolean {
        return billingManager.refreshPurchaseState()
    }
}
```

#### 5.3 Update AppModule

**File:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/AppModule.kt`

```kotlin
val appModule = module {
    // ... existing modules ...
    
    // BillingManager is provided by platform-specific modules
    // (see KoinAndroid.kt, KoinIos.kt, KoinDesktop.kt)
    
    // Legacy BillingClient wrapper for backward compatibility
    single {
        BillingClient(billingManager = get())
    }
    
    // SubscriptionSyncer uses BillingManager
    single {
        SubscriptionSyncer(
            localStorage = get(),
            billingManager = get()
        )
    }
    
    // ... rest of modules ...
}
```

---

### Phase 6: Update Initialization Code (1-2 hours)

#### 6.1 Update Android MainApplication

**File:** `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt`

```kotlin
class MainApplication : Application() {
    
    private val billingManager: BillingManager by inject()
    
    override fun onCreate() {
        super.onCreate()
        
        // ... existing initialization ...
        
        // Initialize billing
        GlobalScope.launch {
            try {
                billingManager.initialize(appUserId = null)
                Log.d("MainApplication", "✅ Billing initialized")
            } catch (e: Exception) {
                Log.e("MainApplication", "❌ Failed to initialize billing", e)
            }
        }
    }
}
```

#### 6.2 Update iOS MainViewController

**File:** `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/MainViewController.kt`

```kotlin
fun MainViewController() = ComposeUIViewController {
    if (!koinInitialized) {
        initializeBuildConfig()
        startKoin(koinConfig)
        koinInitialized = true
        
        // Initialize billing
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val billingManager = getKoin().get<BillingManager>()
                billingManager.initialize(appUserId = null)
                println("iOS: Billing initialized successfully")
            } catch (e: Exception) {
                println("iOS: Failed to initialize billing - ${e.message}")
            }
        }
    }
    
    SpaceLaunchNowApp(/* ... */)
}
```

---

### Phase 7: Update ViewModels and Repositories (2-3 hours)

#### 7.1 Update SubscriptionViewModel

**File:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SubscriptionViewModel.kt`

```kotlin
class SubscriptionViewModel(
    private val repository: SubscriptionRepository,
    private val billingManager: BillingManager
) : ViewModel() {
    
    val purchaseState: StateFlow<PurchaseState> = billingManager.purchaseState
        .stateIn(viewModelScope, SharingStarted.Eagerly, PurchaseState())
    
    val availableProducts: StateFlow<List<ProductInfo>> = flow {
        emit(billingManager.getAvailableProducts().getOrDefault(emptyList()))
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun purchaseProduct(productId: String, basePlanId: String? = null) {
        viewModelScope.launch {
            billingManager.launchPurchaseFlow(productId, basePlanId)
        }
    }
    
    fun restorePurchases() {
        viewModelScope.launch {
            billingManager.restorePurchases()
        }
    }
}
```

---

### Phase 8: Testing and Validation (3-4 hours)

#### 8.1 Unit Tests

Create test implementations:

**File:** `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/billing/MockBillingManager.kt`

```kotlin
class MockBillingManager : BillingManager {
    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized = _isInitialized.asStateFlow()
    
    private val _purchaseState = MutableStateFlow(PurchaseState())
    override val purchaseState = _purchaseState.asStateFlow()
    
    var mockPurchases = mutableListOf<String>()
    
    override suspend fun initialize(appUserId: String?): Result<Unit> {
        _isInitialized.value = true
        return Result.success(Unit)
    }
    
    override suspend fun launchPurchaseFlow(productId: String, basePlanId: String?): Result<Unit> {
        mockPurchases.add(productId)
        _purchaseState.value = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            activeProductIds = setOf(productId),
            features = PremiumFeature.entries.toSet()
        )
        return Result.success(Unit)
    }
    
    // ... implement other methods ...
}
```

#### 8.2 Integration Tests

- Test Android purchase flow
- Test iOS purchase flow
- Test Desktop no-op behavior
- Test subscription state syncing
- Test entitlement checking

---

## 📊 Migration Checklist

### Pre-Migration

- [ ] Review all files that reference `RevenueCatManager` or `RevenueCatBillingClient`
- [ ] Create backup branch: `git checkout -b backup/before-revenuecat-decoupling`
- [ ] Document current behavior with screenshots/videos
- [ ] Create test plan for validation

### Phase 1: Interfaces

- [ ] Create `BillingManager` interface in commonMain
- [ ] Create `PurchaseState` data class
- [ ] Create `ProductInfo` data class
- [ ] Add `expect fun createBillingManager()` declaration

### Phase 2: Android

- [ ] Create `AndroidBillingManager.kt` in androidMain
- [ ] Implement all `BillingManager` methods
- [ ] Update Android Koin module
- [ ] Test Android purchase flow

### Phase 3: iOS

- [ ] Create `IosBillingManager.kt` in iosMain
- [ ] Implement all `BillingManager` methods
- [ ] Update iOS Koin module
- [ ] Test iOS purchase flow

### Phase 4: Desktop

- [ ] Create `DesktopBillingManager.kt` in desktopMain
- [ ] Implement no-op methods
- [ ] Update Desktop Koin module
- [ ] Verify desktop builds without RevenueCat

### Phase 5: Common Updates

- [ ] Update `BillingClient` to use `BillingManager`
- [ ] Update `SubscriptionSyncer` to use `BillingManager`
- [ ] Update `SimpleSubscriptionRepository`
- [ ] Update `AppModule` DI configuration

### Phase 6: Initialization

- [ ] Update `MainApplication.kt` (Android)
- [ ] Update `MainViewController.kt` (iOS)
- [ ] Remove old `RevenueCatManager` initialization code

### Phase 7: ViewModels

- [ ] Update `SubscriptionViewModel`
- [ ] Update `DebugSettingsViewModel`
- [ ] Update any other ViewModels using billing

### Phase 8: Cleanup

- [ ] Delete `RevenueCatManager.kt` from commonMain
- [ ] Delete `RevenueCatBillingClient.kt` from commonMain
- [ ] Delete `RevenueCatConfig.kt` expect/actual files (replace with platform-specific config)
- [ ] Remove unused imports
- [ ] Update documentation

### Testing

- [ ] Android: Test free user flow
- [ ] Android: Test purchase flow
- [ ] Android: Test restore purchases
- [ ] Android: Test entitlement checking
- [ ] iOS: Test free user flow
- [ ] iOS: Test purchase flow
- [ ] iOS: Test restore purchases
- [ ] iOS: Test entitlement checking
- [ ] Desktop: Verify no billing functionality
- [ ] Desktop: Verify app works without errors
- [ ] All platforms: Test subscription syncing
- [ ] All platforms: Test widget access updates

---

## 🎯 Success Criteria

1. **✅ All Current Functionality Preserved**
   - Purchase flow works on Android and iOS
   - Restore purchases works
   - Entitlement checking works
   - Subscription syncing works
   - Widget access updates work

2. **✅ Clean Architecture**
   - No RevenueCat code in commonMain
   - Platform-specific implementations isolated
   - Desktop has no RevenueCat dependency

3. **✅ Testability**
   - Can mock BillingManager for testing
   - Unit tests pass
   - Integration tests pass

4. **✅ Performance**
   - No performance regressions
   - Initialization time similar or better
   - Purchase flow responsive

5. **✅ Code Quality**
   - No compiler warnings
   - No runtime errors
   - Clear separation of concerns
   - Good documentation

---

## 📝 Notes and Considerations

### Why This Architecture?

1. **Separation of Concerns**: Platform-specific billing logic stays in platform-specific source sets
2. **Testability**: Easy to mock `BillingManager` for testing
3. **Desktop Support**: Desktop doesn't need RevenueCat dependency
4. **Future-Proofing**: Easy to swap billing providers or add new platforms
5. **Type Safety**: Compile-time guarantees via expect/actual

### Potential Issues

1. **RevenueCat SDK Differences**: Android and iOS SDKs might have subtle differences
   - **Mitigation**: Abstract away differences in platform implementations

2. **Initialization Timing**: Must ensure BillingManager initializes before use
   - **Mitigation**: Use `isInitialized` StateFlow, check before operations

3. **State Synchronization**: Keep local cache in sync with RevenueCat
   - **Mitigation**: Use `SubscriptionSyncer` with proper cooldown

4. **Error Handling**: Platform-specific errors need consistent handling
   - **Mitigation**: Use `Result<T>` for all operations

### Future Enhancements

1. **Offline Support**: Cache purchase state for offline use
2. **Analytics**: Track billing events with Datadog
3. **A/B Testing**: Test different pricing models
4. **Promotional Offers**: Support for trials and discounts
5. **Family Sharing**: Support for iOS family sharing

---

## 📚 References

- [RevenueCat KMP SDK Documentation](https://www.revenuecat.com/docs/getting-started/installation/kotlin-multiplatform)
- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Expect/Actual Pattern](https://kotlinlang.org/docs/multiplatform-expect-actual.html)
- Current Implementation: `docs/billing/REVENUECAT_QUICK_START.md`

---

## ✅ Approval

- [ ] Architecture approved by team lead
- [ ] Estimated effort confirmed (20-25 hours)
- [ ] Migration schedule set
- [ ] Rollback plan confirmed

**Estimated Effort:** 20-25 hours  
**Risk Level:** Medium  
**Rollback Strategy:** Git revert to backup branch, redeploy previous version

---

*Document Version 1.0 - Created November 10, 2025*
