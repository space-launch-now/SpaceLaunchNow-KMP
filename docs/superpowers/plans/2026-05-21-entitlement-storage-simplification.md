# Entitlement Storage Simplification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove 7 dead fields, derive computed data, persist trial state across restarts, unify the sync update path into a single factory function, and make `needsSync` trigger an automatic retry on cold start.

**Architecture:** Six sequential, independently committable tasks touching five production files and two test files. Tasks 1–3 are pure cleanup with no behavioral change. Tasks 4–5 consolidate update logic. Task 6 adds cold-start retry. Every task leaves the codebase compiling and tests passing.

**Tech Stack:** Kotlin Multiplatform, kotlin.test, kotlinx-coroutines-test, KStore (JSON file persistence via kotlinx-serialization), RevenueCat SDK (Android + iOS), Koin DI.

---

## File Map

| File | Change |
|---|---|
| `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/SubscriptionState.kt` | Remove 4 dead fields, simplify `error()` |
| `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/PurchaseState.kt` | Remove stored `features` + `userId`, make `features` a computed property |
| `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionStorage.kt` | Add `isInTrialPeriod`/`trialExpiresAt`, remove `entitlements`, add `fromPurchaseState()` companion |
| `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/SubscriptionSyncer.kt` | Use `fromPurchaseState()` instead of manual construction |
| `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/SimpleSubscriptionRepository.kt` | Wire trial fields in state mapping, add `needsSync` cold-start retry in `initialize()` |
| `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/data/billing/AndroidBillingManager.kt` | Remove `features` local var + `features`/`userId` from `PurchaseState(...)` constructor |
| Any other `*BillingManager.kt` that constructs `PurchaseState(...)` | Remove same constructor params (found via grep in Task 5 Step 1) |
| `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/model/PurchaseStateTest.kt` | Remove `features =` / `userId =` constructor args, remove `userId` assertions |
| `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionDataTest.kt` | **CREATE** — tests for trial field persistence and `fromPurchaseState()` |
| `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/SubscriptionInitializeRetryTest.kt` | **CREATE** — tests for `needsSync` cold-start retry |

---

## Task 1: Remove dead fields from `SubscriptionState`

Four fields are never written with meaningful values and never read in production:
- `isCached` — always `false`, never set, never read
- `purchasedAt` — always `null`, never set
- `subscriptionId` — always `null`, never set
- `verificationError` — only set inside the `error()` companion, never read anywhere

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/SubscriptionState.kt`

- [ ] **Step 1: Find all usages of the dead fields and `error()` call sites**

```bash
grep -rn "\.isCached\|\.purchasedAt\|\.subscriptionId\|\.verificationError\|SubscriptionState\.error(" \
  composeApp/src --include="*.kt"
```

Expected: hits only inside `SubscriptionState.kt` itself. If `SubscriptionState.error(` appears in another file, that caller passes a `String` — note the file. After Step 2 the `error()` companion takes no parameters, so that call site must be changed to `SubscriptionState.error()`.

- [ ] **Step 2: Replace the `SubscriptionState` data class and its `companion object`**

The full updated class (keep the `@file:OptIn`, `private val log`, and all imports unchanged):

```kotlin
@Serializable
data class SubscriptionState(
    val isSubscribed: Boolean = false,
    val subscriptionType: SubscriptionType = SubscriptionType.FREE,
    val productId: String? = null,
    val expiresAt: Long? = null,
    val lastVerified: Long = 0L,
    val needsVerification: Boolean = false,
    val features: Set<PremiumFeature> = emptySet(),
    val isInTrialPeriod: Boolean = false,
    val trialExpiresAt: Long? = null,
    val isLoading: Boolean = false
) {
    fun isExpired(
        currentTimeMillis: Long = System.now().toEpochMilliseconds()
    ): Boolean {
        val expires = expiresAt ?: return false
        return currentTimeMillis > expires
    }

    fun isRecentlyVerified(
        currentTimeMillis: Long = System.now().toEpochMilliseconds()
    ): Boolean {
        val oneHourMillis = 60 * 60 * 1000L
        return (currentTimeMillis - lastVerified) < oneHourMillis
    }

    fun hasFeature(feature: PremiumFeature): Boolean {
        val hasAccess = isSubscribed && !isExpired() && features.contains(feature)
        log.d { "hasFeature(${feature.name}): isSubscribed=$isSubscribed, isExpired=${isExpired()}, features=$features, hasAccess=$hasAccess" }
        return hasAccess
    }

    companion object {
        val DEFAULT = SubscriptionState()

        fun free() = SubscriptionState(
            isSubscribed = false,
            subscriptionType = SubscriptionType.FREE,
            features = PremiumFeature.getFreeFeatures()
        )

        fun error() = SubscriptionState(
            isSubscribed = false,
            subscriptionType = SubscriptionType.FREE,
            needsVerification = true,
            features = PremiumFeature.getFreeFeatures()
        )
    }
}
```

Keep `SubscriptionType`, `PremiumFeature`, `PlatformPurchase`, and `Platform` definitions in the file unchanged.

- [ ] **Step 3: Compile check**

```bash
./gradlew compileKotlinDesktop
```

Expected: `BUILD SUCCESSFUL`. Fix any remaining references to the removed fields reported by the compiler.

- [ ] **Step 4: Run tests**

```bash
./gradlew :composeApp:jvmTest
```

Expected: all tests pass.

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/SubscriptionState.kt
git commit -m "refactor(subscription): remove dead fields from SubscriptionState"
```

---

## Task 2: Add trial fields to `LocalSubscriptionData` and wire through

`LocalSubscriptionData` is the KStore-persisted model — the only layer that survives app restarts. `isInTrialPeriod` and `trialExpiresAt` exist in `PurchaseState` (from RevenueCat) and `SubscriptionState` (for UI) but are missing from the persistence layer. Trial state is silently lost on every app restart until RC responds.

**KStore deserialization safety:** Adding fields with default values to a `@Serializable` data class is backward compatible. Existing JSON files missing the new keys will deserialize with the field defaults (`false` / `null`).

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionStorage.kt`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/SubscriptionSyncer.kt`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/SimpleSubscriptionRepository.kt`
- Create: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionDataTest.kt`

- [ ] **Step 1: Write the failing test**

Create `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionDataTest.kt`:

```kotlin
package me.calebjones.spacelaunchnow.data.subscription

import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalSubscriptionDataTest {

    @Test
    fun `default LocalSubscriptionData has no trial`() {
        val data = LocalSubscriptionData()
        assertFalse(data.isInTrialPeriod)
        assertEquals(null, data.trialExpiresAt)
    }

    @Test
    fun `trial fields survive copy`() {
        val trialExpiry = 9_999_999_999L
        val data = LocalSubscriptionData(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            isInTrialPeriod = true,
            trialExpiresAt = trialExpiry
        )
        val copy = data.copy(isSubscribed = false)
        assertTrue(copy.isInTrialPeriod)
        assertEquals(trialExpiry, copy.trialExpiresAt)
    }

    @Test
    fun `FREE companion has no trial`() {
        assertFalse(LocalSubscriptionData.FREE.isInTrialPeriod)
        assertEquals(null, LocalSubscriptionData.FREE.trialExpiresAt)
    }
}
```

- [ ] **Step 2: Run to confirm it fails**

```bash
./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.data.subscription.LocalSubscriptionDataTest"
```

Expected: FAILED — `Unresolved reference: isInTrialPeriod`

- [ ] **Step 3: Add trial fields to `LocalSubscriptionData` in `LocalSubscriptionStorage.kt`**

Add the two new fields at the end of the data class parameter list. The full updated parameter list (existing fields unchanged, new fields appended):

```kotlin
@Serializable
data class LocalSubscriptionData(
    val isSubscribed: Boolean = false,
    val subscriptionType: SubscriptionType = SubscriptionType.FREE,
    val entitlements: Set<String> = emptySet(),
    val productIds: Set<String> = emptySet(),
    val lastSynced: Long = 0L,
    val needsSync: Boolean = true,
    val isDebugMode: Boolean = false,
    val subscriptionExpiryMs: Long? = null,
    val wasEverPremium: Boolean = false,
    val isInTrialPeriod: Boolean = false,
    val trialExpiresAt: Long? = null
) {
    // hasFeature(), availableFeatures, and companion object unchanged
}
```

- [ ] **Step 4: Wire trial fields in `SubscriptionSyncer.startSyncing()`**

In `SubscriptionSyncer.kt`, find the `LocalSubscriptionData(...)` construction block and add the two new fields:

```kotlin
val newData = LocalSubscriptionData(
    isSubscribed = purchaseState.isSubscribed,
    subscriptionType = purchaseState.subscriptionType,
    productIds = purchaseState.activeProductIds,
    entitlements = purchaseState.activeEntitlements,
    lastSynced = currentTime,
    needsSync = false,
    isDebugMode = false,
    subscriptionExpiryMs = purchaseState.subscriptionExpiryMs,
    wasEverPremium = currentData.wasEverPremium || purchaseState.isSubscribed,
    isInTrialPeriod = purchaseState.isInTrialPeriod,
    trialExpiresAt = purchaseState.trialExpiresAt
)
```

- [ ] **Step 5: Wire trial fields in `SimpleSubscriptionRepository.state` mapping**

In `SimpleSubscriptionRepository.kt`, the `.map { local -> SubscriptionState(...) }` block maps local storage to UI state. Add the trial fields to the `SubscriptionState(...)` constructor:

```kotlin
SubscriptionState(
    isSubscribed = local.isSubscribed,
    subscriptionType = local.subscriptionType,
    productId = local.productIds.firstOrNull(),
    features = local.availableFeatures,
    lastVerified = local.lastSynced,
    needsVerification = local.needsSync,
    isLoading = false,
    isInTrialPeriod = local.isInTrialPeriod,
    trialExpiresAt = local.trialExpiresAt
)
```

Also update the `initialValue = run { ... }` block inside `stateIn(...)` (the `runBlocking { localStorage.get() }` path) with the same two new fields.

- [ ] **Step 6: Run tests**

```bash
./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.data.subscription.LocalSubscriptionDataTest"
```

Expected: 3 tests PASS.

```bash
./gradlew :composeApp:jvmTest
```

Expected: all tests pass.

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionStorage.kt \
        composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/SubscriptionSyncer.kt \
        composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/SimpleSubscriptionRepository.kt \
        composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionDataTest.kt
git commit -m "fix(subscription): persist trial state across app restarts"
```

---

## Task 3: Remove `entitlements` from `LocalSubscriptionData`

`entitlements: Set<String>` (the raw RevenueCat entitlement ID strings) is synced to disk but never read back from disk. Feature gating is done via `subscriptionType` + `PremiumFeature.getFeaturesForType()`. Keeping entitlements persisted is misleading and wastes storage.

**KStore deserialization safety:** Removing a field from a `@Serializable` class causes kotlinx-serialization to silently ignore that key in existing JSON files — no migration needed.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionStorage.kt`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/SubscriptionSyncer.kt`

- [ ] **Step 1: Confirm no read sites exist**

```bash
grep -rn "\.entitlements\b" composeApp/src/commonMain composeApp/src/commonTest --include="*.kt"
```

Expected: hits only in `LocalSubscriptionStorage.kt` (field declaration and the `update()` log line) and `SubscriptionSyncer.kt` (the `LocalSubscriptionData(...)` construction). If any other read site appears, stop and investigate before proceeding.

- [ ] **Step 2: Remove `entitlements` from the `LocalSubscriptionData` data class**

Updated parameter list (remove the `val entitlements: Set<String> = emptySet()` line):

```kotlin
@Serializable
data class LocalSubscriptionData(
    val isSubscribed: Boolean = false,
    val subscriptionType: SubscriptionType = SubscriptionType.FREE,
    val productIds: Set<String> = emptySet(),
    val lastSynced: Long = 0L,
    val needsSync: Boolean = true,
    val isDebugMode: Boolean = false,
    val subscriptionExpiryMs: Long? = null,
    val wasEverPremium: Boolean = false,
    val isInTrialPeriod: Boolean = false,
    val trialExpiresAt: Long? = null
)
```

- [ ] **Step 3: Update the `update()` log line in `LocalSubscriptionStorage`**

Find the log line in `update()` that references `data.entitlements`:

```kotlin
// Old:
log.d { "Saving subscription data - Type: ${data.subscriptionType}, Subscribed: ${data.isSubscribed}, Entitlements: ${data.entitlements}" }

// New:
log.d { "Saving subscription data - type=${data.subscriptionType}, subscribed=${data.isSubscribed}, products=${data.productIds}" }
```

- [ ] **Step 4: Remove `entitlements` from the `SubscriptionSyncer` construction**

In `SubscriptionSyncer.kt`, remove the `entitlements = purchaseState.activeEntitlements,` line from the `LocalSubscriptionData(...)` block.

- [ ] **Step 5: Compile check**

```bash
./gradlew compileKotlinDesktop
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Run tests**

```bash
./gradlew :composeApp:jvmTest
```

Expected: all tests pass.

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionStorage.kt \
        composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/SubscriptionSyncer.kt
git commit -m "refactor(subscription): remove unused entitlements field from local storage"
```

---

## Task 4: Unify sync update path via `LocalSubscriptionData.fromPurchaseState()`

`SubscriptionSyncer` currently constructs `LocalSubscriptionData` manually with explicit field assignment. Any new field added to `LocalSubscriptionData` that needs populating from RC data requires a change in two places. A single `fromPurchaseState(purchase, existing)` factory function closes this gap and makes the transformation testable independently.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionStorage.kt`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/SubscriptionSyncer.kt`
- Modify: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionDataTest.kt`

- [ ] **Step 1: Add tests for `fromPurchaseState()` to `LocalSubscriptionDataTest.kt`**

Append to the existing test class:

```kotlin
import me.calebjones.spacelaunchnow.data.model.PurchaseState

@Test
fun `fromPurchaseState copies subscription fields from purchase`() {
    val existing = LocalSubscriptionData(wasEverPremium = false)
    val purchase = PurchaseState(
        isSubscribed = true,
        subscriptionType = SubscriptionType.PREMIUM,
        activeProductIds = setOf("sln_monthly"),
        subscriptionExpiryMs = 9_999_999_999L,
        isInTrialPeriod = false,
        trialExpiresAt = null,
        lastRefreshed = 1_000L
    )
    val result = LocalSubscriptionData.fromPurchaseState(purchase, existing)

    assertTrue(result.isSubscribed)
    assertEquals(SubscriptionType.PREMIUM, result.subscriptionType)
    assertEquals(setOf("sln_monthly"), result.productIds)
    assertEquals(9_999_999_999L, result.subscriptionExpiryMs)
    assertFalse(result.isDebugMode)
    assertFalse(result.needsSync)
}

@Test
fun `fromPurchaseState wasEverPremium sticky from existing`() {
    val existing = LocalSubscriptionData(wasEverPremium = true)
    val purchase = PurchaseState(isSubscribed = false, subscriptionType = SubscriptionType.FREE)
    val result = LocalSubscriptionData.fromPurchaseState(purchase, existing)
    assertTrue(result.wasEverPremium)
}

@Test
fun `fromPurchaseState wasEverPremium sticky from purchase`() {
    val existing = LocalSubscriptionData(wasEverPremium = false)
    val purchase = PurchaseState(isSubscribed = true, subscriptionType = SubscriptionType.PREMIUM)
    val result = LocalSubscriptionData.fromPurchaseState(purchase, existing)
    assertTrue(result.wasEverPremium)
}

@Test
fun `fromPurchaseState sets lastSynced to current time`() {
    val existing = LocalSubscriptionData(lastSynced = 0L)
    val purchase = PurchaseState(isSubscribed = false, subscriptionType = SubscriptionType.FREE)
    val before = kotlin.time.Clock.System.now().toEpochMilliseconds()
    val result = LocalSubscriptionData.fromPurchaseState(purchase, existing)
    val after = kotlin.time.Clock.System.now().toEpochMilliseconds()
    assertTrue(result.lastSynced in before..after)
}
```

- [ ] **Step 2: Run to confirm the new tests fail**

```bash
./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.data.subscription.LocalSubscriptionDataTest"
```

Expected: FAILED — `Unresolved reference: fromPurchaseState`

- [ ] **Step 3: Add the `fromPurchaseState()` factory to `LocalSubscriptionData.companion object`**

In `LocalSubscriptionStorage.kt`, add the import at the top of the file:

```kotlin
import me.calebjones.spacelaunchnow.data.model.PurchaseState
```

Then add `fromPurchaseState` inside the existing `companion object`:

```kotlin
companion object {
    val DEFAULT = LocalSubscriptionData()

    val FREE = LocalSubscriptionData(
        isSubscribed = false,
        subscriptionType = SubscriptionType.FREE,
        needsSync = false,
        isDebugMode = false
    )

    fun fromPurchaseState(
        purchase: PurchaseState,
        existing: LocalSubscriptionData
    ): LocalSubscriptionData = existing.copy(
        isSubscribed = purchase.isSubscribed,
        subscriptionType = purchase.subscriptionType,
        productIds = purchase.activeProductIds,
        lastSynced = System.now().toEpochMilliseconds(),
        needsSync = false,
        isDebugMode = false,
        subscriptionExpiryMs = purchase.subscriptionExpiryMs,
        wasEverPremium = existing.wasEverPremium || purchase.isSubscribed,
        isInTrialPeriod = purchase.isInTrialPeriod,
        trialExpiresAt = purchase.trialExpiresAt
    )
}
```

(`System` here refers to `kotlin.time.Clock.System` already imported in `LocalSubscriptionStorage.kt`.)

- [ ] **Step 4: Replace manual construction in `SubscriptionSyncer`**

In `SubscriptionSyncer.kt`, inside the `if (currentTime - lastSyncTime > syncCooldownMs)` block, replace the `val newData = LocalSubscriptionData(...)` block and the log line that follows it with:

```kotlin
val newData = LocalSubscriptionData.fromPurchaseState(purchaseState, currentData)
log.d { "Purchase state updated, syncing - isSubscribed=${purchaseState.isSubscribed}, type=${purchaseState.subscriptionType}, products=${purchaseState.activeProductIds}" }
lastSyncTime = currentTime

val success = localStorage.update(newData)
// ... success/failure handling unchanged
```

- [ ] **Step 5: Run tests**

```bash
./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.data.subscription.LocalSubscriptionDataTest"
```

Expected: 7 tests PASS (3 from Task 2 + 4 new).

```bash
./gradlew :composeApp:jvmTest
```

Expected: all tests pass.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionStorage.kt \
        composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/subscription/SubscriptionSyncer.kt \
        composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/subscription/LocalSubscriptionDataTest.kt
git commit -m "refactor(subscription): unify sync update path via LocalSubscriptionData.fromPurchaseState()"
```

---

## Task 5: Derive `features` from `subscriptionType` in `PurchaseState`, remove `userId`

**`features`:** `AndroidBillingManager.determineFeatures()` is 3 lines and delegates entirely to `PremiumFeature.getFeaturesForType(type)`. Storing the result in `PurchaseState` creates a drift risk if the feature mapping changes and the two copies get out of sync. Making `features` a computed property (`get() = PremiumFeature.getFeaturesForType(subscriptionType)`) eliminates the risk. All existing call sites that *read* `purchaseState.features` continue to work — the property name and return type are unchanged.

**`userId`:** The RevenueCat original app user ID is written into `PurchaseState` on every RC callback but is never read or forwarded to any downstream layer. It silently disappears on every app restart.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/PurchaseState.kt`
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/data/billing/AndroidBillingManager.kt`
- Modify: Any other `*BillingManager.kt` files that construct `PurchaseState(...)` (found via Step 1)
- Modify: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/model/PurchaseStateTest.kt`

- [ ] **Step 1: Find all `PurchaseState(` construction sites**

```bash
grep -rn "PurchaseState(" composeApp/src --include="*.kt"
```

Note every file returned. Each will need `features = ...` and `userId = ...` removed from its `PurchaseState(...)` constructor call.

- [ ] **Step 2: Update `PurchaseState.kt` — make `features` computed, remove `userId`**

Replace the full file content with:

```kotlin
package me.calebjones.spacelaunchnow.data.model

data class PurchaseState(
    val isSubscribed: Boolean = false,
    val subscriptionType: SubscriptionType = SubscriptionType.FREE,
    val activeEntitlements: Set<String> = emptySet(),
    val activeProductIds: Set<String> = emptySet(),
    val lastRefreshed: Long = 0L,
    val isInTrialPeriod: Boolean = false,
    val trialExpiresAt: Long? = null,
    val subscriptionExpiryMs: Long? = null
) {
    val hasLoaded: Boolean get() = lastRefreshed != 0L
    val features: Set<PremiumFeature> get() = PremiumFeature.getFeaturesForType(subscriptionType)

    fun toSubscriptionState(): SubscriptionState {
        return SubscriptionState(
            isSubscribed = isSubscribed,
            subscriptionType = subscriptionType,
            productId = activeProductIds.firstOrNull(),
            lastVerified = lastRefreshed,
            features = features,
            needsVerification = false,
            isInTrialPeriod = isInTrialPeriod,
            trialExpiresAt = trialExpiresAt
        )
    }
}
```

- [ ] **Step 3: Update `AndroidBillingManager.updatePurchaseState()`**

Three changes in `updatePurchaseState()` (around line 297):

**a) Delete the `features` local variable** (~line 315):
```kotlin
// DELETE:
val features = determineFeatures(subscriptionType)
```

**b) Update the log line** (~line 324) — remove the `features` reference:
```kotlin
log.i { "Purchase state updated - type: $subscriptionType, entitlements: $activeEntitlements, products: $productIds, inTrial: $isInTrial" }
```

**c) Remove `features` and `userId` from the `PurchaseState(...)` constructor call** (~lines 326–336):
```kotlin
_purchaseState.value = PurchaseState(
    isSubscribed = subscriptionType != SubscriptionType.FREE,
    subscriptionType = subscriptionType,
    activeEntitlements = activeEntitlements,
    activeProductIds = productIds,
    lastRefreshed = System.currentTimeMillis(),
    isInTrialPeriod = isInTrial,
    trialExpiresAt = trialExpires
)
```

- [ ] **Step 4: Apply the same constructor change to every other `*BillingManager.kt` from Step 1**

For each file: remove `features = ...` and `userId = ...` from its `PurchaseState(...)` constructor call. Reading `purchaseState.features` in any of these files will still compile — the property is now computed with the same name and type.

- [ ] **Step 5: Update `PurchaseStateTest.kt`**

Replace the full file with the updated test class (removes `features =` and `userId =` constructor args, removes `userId` assertions, adds a test for the computed `features` property):

```kotlin
package me.calebjones.spacelaunchnow.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PurchaseStateTest {

    @Test
    fun `default PurchaseState is free tier with no loaded flag`() {
        val state = PurchaseState()
        assertFalse(state.isSubscribed)
        assertEquals(SubscriptionType.FREE, state.subscriptionType)
        assertTrue(state.activeEntitlements.isEmpty())
        assertTrue(state.activeProductIds.isEmpty())
        assertTrue(state.features.isEmpty())
        assertEquals(0L, state.lastRefreshed)
        assertFalse(state.hasLoaded)
    }

    @Test
    fun `PurchaseState with premium subscription has correct features`() {
        val currentTime = System.currentTimeMillis()
        val state = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            activeEntitlements = setOf("premium"),
            activeProductIds = setOf("monthly_sub"),
            lastRefreshed = currentTime
        )
        assertTrue(state.isSubscribed)
        assertEquals(SubscriptionType.PREMIUM, state.subscriptionType)
        assertEquals(PremiumFeature.entries.toSet(), state.features)
        assertTrue(state.hasLoaded)
    }

    @Test
    fun `PurchaseState with lifetime subscription has all features`() {
        val state = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.LIFETIME,
            activeEntitlements = setOf("premium", "lifetime"),
            activeProductIds = setOf("lifetime_purchase")
        )
        assertEquals(PremiumFeature.entries.size, state.features.size)
    }

    @Test
    fun `features is derived from subscriptionType on copy`() {
        val original = PurchaseState(isSubscribed = true, subscriptionType = SubscriptionType.PREMIUM)
        val downgraded = original.copy(subscriptionType = SubscriptionType.FREE)
        assertEquals(PremiumFeature.entries.toSet(), original.features)
        assertTrue(downgraded.features.isEmpty())
    }

    @Test
    fun `toSubscriptionState converts correctly`() {
        val currentTime = System.currentTimeMillis()
        val state = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            activeEntitlements = setOf("premium"),
            activeProductIds = setOf("monthly_sub", "addon_1"),
            lastRefreshed = currentTime
        )
        val result = state.toSubscriptionState()
        assertTrue(result.isSubscribed)
        assertEquals(SubscriptionType.PREMIUM, result.subscriptionType)
        assertEquals("monthly_sub", result.productId)
        assertEquals(currentTime, result.lastVerified)
        assertEquals(PremiumFeature.entries.toSet(), result.features)
        assertFalse(result.needsVerification)
    }

    @Test
    fun `toSubscriptionState with no products returns null productId`() {
        val state = PurchaseState(isSubscribed = false, subscriptionType = SubscriptionType.FREE)
        val result = state.toSubscriptionState()
        assertEquals(null, result.productId)
    }

    @Test
    fun `PurchaseState tracks all entitlements`() {
        val state = PurchaseState(
            isSubscribed = true,
            subscriptionType = SubscriptionType.PREMIUM,
            activeEntitlements = setOf("premium", "ad_free", "widgets", "cal_sync"),
            activeProductIds = setOf("premium_yearly")
        )
        assertEquals(4, state.activeEntitlements.size)
        assertTrue(state.activeEntitlements.containsAll(listOf("premium", "ad_free", "widgets", "cal_sync")))
    }
}
```

- [ ] **Step 6: Run tests**

```bash
./gradlew :composeApp:jvmTest
```

Expected: all tests pass.

- [ ] **Step 7: Commit**

```bash
# Add PurchaseState.kt, AndroidBillingManager.kt, PurchaseStateTest.kt,
# and any other BillingManager files changed in Step 4
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/PurchaseState.kt \
        composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/data/billing/AndroidBillingManager.kt \
        composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/model/PurchaseStateTest.kt
git commit -m "refactor(subscription): derive PurchaseState.features from subscriptionType, remove unused userId"
```

---

## Task 6: Make `needsSync` trigger retry on cold start

`needsSync = true` is written to disk on sync failure and after file corruption recovery. `SubscriptionSyncer` never checks it — it reacts only to RC `purchaseState` flow updates. A failed sync is flagged on disk but silently ignored until the user taps "Restore Purchases" or RC emits another update. This task checks the flag in `initialize()` and calls `syncer.syncNow()` immediately if it is set.

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/SimpleSubscriptionRepository.kt`
- Create: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/SubscriptionInitializeRetryTest.kt`

- [ ] **Step 1: Look at existing test fakes for patterns before writing new ones**

Read these files to understand the fake/mock conventions used in this project:

- `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/billing/MockBillingManager.kt`
- `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/MockSubscriptionRepository.kt`

The test below needs a `FakeLocalSubscriptionStorage` and a `FakeSubscriptionSyncer`. Write them matching those patterns (in-memory state, simple callback lambdas, no real KStore or RC involved).

- [ ] **Step 2: Write the failing test**

Create `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/SubscriptionInitializeRetryTest.kt`:

```kotlin
package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.subscription.LocalSubscriptionData
import me.calebjones.spacelaunchnow.data.subscription.LocalSubscriptionStorage
import me.calebjones.spacelaunchnow.data.subscription.SubscriptionSyncer
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// Minimal fake — stores one LocalSubscriptionData in memory
class FakeLocalSubscriptionStorage(initial: LocalSubscriptionData) : LocalSubscriptionStorage() {
    private var stored = initial
    override suspend fun get(): LocalSubscriptionData = stored
    override suspend fun update(data: LocalSubscriptionData): Boolean { stored = data; return true }
}

// Minimal fake — records whether syncNow() was called
class FakeSubscriptionSyncer(
    storage: LocalSubscriptionStorage,
    billing: BillingManager,
    private val onSyncNow: suspend () -> Boolean = { true }
) : SubscriptionSyncer(storage, billing) {
    override fun startSyncing() { /* no-op */ }
    override suspend fun syncNow(): Boolean = onSyncNow()
}

class SubscriptionInitializeRetryTest {

    @Test
    fun `initialize calls syncNow when needsSync is true`() = runTest {
        var syncNowCalled = false
        val fakeStorage = FakeLocalSubscriptionStorage(LocalSubscriptionData(needsSync = true))
        val fakeBilling = object : BillingManager {
            override val purchaseState: StateFlow<PurchaseState> = MutableStateFlow(PurchaseState())
            override suspend fun initialize(appUserId: String?) {}
            override suspend fun refreshPurchaseState(): Boolean = false
            override fun getActiveEntitlements(): Set<String> = emptySet()
        }
        val fakeSyncer = FakeSubscriptionSyncer(fakeStorage, fakeBilling) {
            syncNowCalled = true; true
        }

        // Construct only the parts under test; pass nulls/no-ops for unrelated deps
        // following the pattern in SubscriptionViewModelTest.kt
        val repo = SimpleSubscriptionRepository(
            localStorage = fakeStorage,
            syncer = fakeSyncer,
            billingClient = fakeBilling as me.calebjones.spacelaunchnow.data.billing.BillingClient,
            widgetPreferences = FakeWidgetPreferences(),
            temporaryPremiumAccess = FakeTemporaryPremiumAccess()
        )
        repo.initialize()

        assertTrue(syncNowCalled, "syncNow must be called when needsSync=true on cold start")
    }

    @Test
    fun `initialize does not call syncNow when needsSync is false`() = runTest {
        var syncNowCalled = false
        val fakeStorage = FakeLocalSubscriptionStorage(LocalSubscriptionData(needsSync = false))
        val fakeBilling = object : BillingManager {
            override val purchaseState: StateFlow<PurchaseState> = MutableStateFlow(PurchaseState())
            override suspend fun initialize(appUserId: String?) {}
            override suspend fun refreshPurchaseState(): Boolean = false
            override fun getActiveEntitlements(): Set<String> = emptySet()
        }
        val fakeSyncer = FakeSubscriptionSyncer(fakeStorage, fakeBilling) {
            syncNowCalled = true; true
        }

        val repo = SimpleSubscriptionRepository(
            localStorage = fakeStorage,
            syncer = fakeSyncer,
            billingClient = fakeBilling as me.calebjones.spacelaunchnow.data.billing.BillingClient,
            widgetPreferences = FakeWidgetPreferences(),
            temporaryPremiumAccess = FakeTemporaryPremiumAccess()
        )
        repo.initialize()

        assertFalse(syncNowCalled, "syncNow must NOT be called when needsSync=false")
    }
}
```

**Note:** `FakeWidgetPreferences` and `FakeTemporaryPremiumAccess` — look at `ViewModelTestFixtures.kt` for how other tests stub these. If the constructor signatures shown above don't match `SimpleSubscriptionRepository`'s actual signature, adjust to match exactly (check the class header).

- [ ] **Step 3: Run to confirm the tests fail**

```bash
./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.data.repository.SubscriptionInitializeRetryTest"
```

Expected: FAILED — either compilation error (fakes need adjusting) or test assertion failure (`syncNowCalled` never set to `true`).

- [ ] **Step 4: Update `SimpleSubscriptionRepository.initialize()`**

In `SimpleSubscriptionRepository.kt`, find `override suspend fun initialize()` (~line 112). Add the `needsSync` check after `syncer.startSyncing()`:

```kotlin
override suspend fun initialize() {
    log.d { "SimpleSubscriptionRepository: Initializing..." }

    billingClient.initialize()
    syncer.startSyncing()

    // If a previous sync failed (write error or corruption recovery), the needsSync flag
    // was set on disk. Retry immediately rather than waiting for the next RC state update.
    if (localStorage.get().needsSync) {
        log.i { "needsSync=true on cold start — triggering immediate sync" }
        syncer.syncNow()
    }

    log.i { "SimpleSubscriptionRepository: ✅ Initialized" }
}
```

- [ ] **Step 5: Run tests**

```bash
./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.data.repository.SubscriptionInitializeRetryTest"
```

Expected: 2 tests PASS.

```bash
./gradlew :composeApp:jvmTest
```

Expected: all tests pass.

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/SimpleSubscriptionRepository.kt \
        composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/SubscriptionInitializeRetryTest.kt
git commit -m "fix(subscription): retry sync on cold start when needsSync=true"
```
