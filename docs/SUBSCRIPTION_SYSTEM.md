## Subscription System Documentation

# Overview

This subscription system provides secure, platform-agnostic in-app purchase management for SpaceLaunchNow KMP.

## ⚠️ CRITICAL SECURITY PRINCIPLE

**DataStore is ONLY for UX caching - NEVER for access control!**

```kotlin
// ❌ WRONG - Never trust cached state for access control
if (cachedSubscriptionState.isSubscribed) {
    showPremiumFeature() // EASILY HACKABLE!
}

// ✅ CORRECT - Always verify with platform before granting access
suspend fun showPremiumFeature() {
    val verified = subscriptionRepo.verifySubscription(forceRefresh = true)
    if (verified.getOrNull()?.isSubscribed == true) {
        // NOW safe to show premium feature
    } else {
        showPaywall()
    }
}
```

---

## Architecture

### Components

1. **Models** (`SubscriptionState.kt`)
   - `SubscriptionState` - Subscription status and metadata
   - `SubscriptionType` - FREE, BASIC, PREMIUM
   - `PremiumFeature` - Enum of all premium features
   - `PlatformPurchase` - Platform-agnostic purchase data

2. **Billing Client** (`BillingClient.kt`)
   - **Android**: Google Play Billing Library
   - **iOS**: StoreKit 2 (TODO)
   - **Desktop**: No-op (server-side licensing recommended)

3. **Storage** (`SubscriptionStorage.kt`)
   - DataStore-based caching (UX only!)
   - Stores last verified state
   - Marks when verification needed

4. **Repository** (`SubscriptionRepository.kt`)
   - `SubscriptionRepositoryImpl` - Business logic
   - Verifies with platform billing
   - Caches for instant UI feedback
   - Manages purchase flows

5. **ViewModel** (`SubscriptionViewModel.kt`)
   - UI state management
   - Purchase initiation
   - Subscription verification

6. **UI** (`SubscriptionScreen.kt`)
   - Subscription management screen
   - Purchase flow UI
   - Feature list display

---

## Security Model

### Three-Layer Security

```
┌──────────────────────────────────────────────────┐
│ Layer 1: Platform Billing (SOURCE OF TRUTH)     │
│ - Google Play Billing / StoreKit                │
│ - Can NOT be manipulated by user                │
│ - Always verify here before granting access     │
└──────────────────────────────────────────────────┘
            ↓
┌──────────────────────────────────────────────────┐
│ Layer 2: Repository (VERIFICATION)               │
│ - Queries platform billing                      │
│ - Processes verified purchases                  │
│ - Updates cache after verification              │
└──────────────────────────────────────────────────┘
            ↓
┌──────────────────────────────────────────────────┐
│ Layer 3: DataStore (UX CACHE ONLY)              │
│ - Instant UI feedback                           │
│ - Offline hints                                  │
│ - NEVER used for access control                 │
└──────────────────────────────────────────────────┘
```

### Verification Flow

```kotlin
// 1. Load cached state (instant UI)
val cachedState = storage.getState()
_state.emit(cachedState)

// 2. Verify with platform (background)
val purchases = billingClient.queryPurchases() // ← SOURCE OF TRUTH

// 3. Process verified data
val verifiedState = processVerifiedPurchases(purchases)

// 4. Update cache and emit
storage.saveState(verifiedState)
_state.emit(verifiedState)
```

---

## Usage

### Basic Setup

#### 1. Initialize Repository

```kotlin
// In your Application class or main setup
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@MainApplication)
            modules(/* your modules */)
        }
        
        // Initialize subscription repository
        lifecycleScope.launch {
            val subscriptionRepo = get<SubscriptionRepository>()
            subscriptionRepo.initialize()
        }
    }
}
```

#### 2. Observe Subscription State

```kotlin
@Composable
fun MyFeature() {
    val subscriptionRepo = koinInject<SubscriptionRepository>()
    val subscriptionState by subscriptionRepo.state.collectAsState()
    
    // Show UI based on cached state (instant feedback)
    if (subscriptionState.isSubscribed) {
        PremiumUI()
    } else {
        FreeUI()
    }
}
```

#### 3. Verify Before Granting Access

```kotlin
@Composable
fun PremiumFeatureButton() {
    val subscriptionRepo = koinInject<SubscriptionRepository>()
    val scope = rememberCoroutineScope()
    
    Button(
        onClick = {
            scope.launch {
                // CRITICAL: Verify before showing premium content
                val result = subscriptionRepo.verifySubscription(forceRefresh = true)
                
                if (result.isSuccess && result.getOrNull()?.isSubscribed == true) {
                    // Safe to show premium feature
                    showPremiumContent()
                } else {
                    // Show paywall
                    showPaywall()
                }
            }
        }
    ) {
        Text("Access Premium Feature")
    }
}
```

### Feature Gating

#### Check Specific Features

```kotlin
@Composable
fun ConditionalFeature() {
    val subscriptionRepo = koinInject<SubscriptionRepository>()
    val hasAdvancedFilters by remember {
        derivedStateOf {
            subscriptionRepo.state.value.hasFeature(PremiumFeature.ADVANCED_FILTERS)
        }
    }
    
    if (hasAdvancedFilters) {
        AdvancedFiltersUI()
    } else {
        Button(onClick = { /* Show paywall */ }) {
            Text("Unlock Advanced Filters")
        }
    }
}
```

#### Verified Feature Check (Secure)

```kotlin
suspend fun accessSensitiveFeature() {
    val subscriptionRepo = get<SubscriptionRepository>()
    
    // verify=true forces platform verification
    val hasAccess = subscriptionRepo.hasFeature(
        feature = PremiumFeature.OFFLINE_MODE,
        verify = true // ← Verifies with platform first
    )
    
    if (hasAccess) {
        // Verified - safe to proceed
        enableOfflineMode()
    } else {
        showPaywall()
    }
}
```

### Purchase Flow

#### Launch Purchase

```kotlin
@Composable
fun SubscriptionButton() {
    val viewModel = koinInject<SubscriptionViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    
    Button(
        onClick = {
            viewModel.purchaseSubscription(
                SubscriptionProducts.PREMIUM_MONTHLY
            )
        },
        enabled = !uiState.isProcessing
    ) {
        if (uiState.isProcessing) {
            CircularProgressIndicator()
        } else {
            Text("Subscribe to Premium")
        }
    }
}
```

#### Restore Purchases

```kotlin
Button(
    onClick = { viewModel.restorePurchases() }
) {
    Text("Restore Purchases")
}
```

---

## Product Configuration

### Available Products

Defined in `SubscriptionProducts`:

```kotlin
object SubscriptionProducts {
    // Monthly
    const val BASIC_MONTHLY = "spacelaunchnow_basic_monthly"
    const val PREMIUM_MONTHLY = "spacelaunchnow_premium_monthly"
    
    // Yearly (better value)
    const val BASIC_YEARLY = "spacelaunchnow_basic_yearly"
    const val PREMIUM_YEARLY = "spacelaunchnow_premium_yearly"
}
```

### Platform Setup Required

#### Android (Google Play)

1. **Create products in Google Play Console**:
   - Navigate to: Monetization → Products → Subscriptions
   - Create each product ID listed above
   - Set pricing and billing periods

2. **Add dependency** (already done):
   ```kotlin
   // build.gradle.kts (androidMain)
   implementation("com.android.billingclient:billing-ktx:6.1.0")
   ```

3. **Test with license testers**:
   - Add test accounts in Play Console
   - Use test subscription products

#### iOS (App Store) - TODO

1. **Configure in App Store Connect**:
   - Create subscription group
   - Add products matching IDs above
   - Set pricing for each region

2. **Implement StoreKit 2 bridge**:
   - Create Swift bridge (see `IosBillingClient.kt` TODOs)
   - Use `Transaction.currentEntitlements`
   - Handle purchase verification

3. **Add StoreKit configuration file**:
   - Add to iOS project for local testing

---

## Premium Features

### Defining Features

Add to `PremiumFeature` enum:

```kotlin
enum class PremiumFeature {
    ADVANCED_FILTERS,
    UNLIMITED_NOTIFICATIONS,
    AD_FREE,
    CUSTOM_THEMES,
    // Add new features here
}
```

### Feature Sets by Tier

```kotlin
companion object {
    fun getBasicFeatures() = setOf(
        AD_FREE,
        ADVANCED_FILTERS,
        UNLIMITED_NOTIFICATIONS
    )
    
    fun getPremiumFeatures() = entries.toSet() // All features
}
```

---

## Testing

### Test Subscription Flow

```kotlin
@Test
fun `subscription verification updates state`() = runTest {
    val mockBillingClient = MockBillingClient()
    val storage = SubscriptionStorage(testDataStore)
    val repository = SubscriptionRepositoryImpl(mockBillingClient, storage)
    
    repository.initialize()
    
    // Simulate purchase
    mockBillingClient.simulatePurchase(
        productId = "premium_monthly",
        expiryTime = Clock.System.now().plus(30.days)
    )
    
    // Verify
    val result = repository.verifySubscription(forceRefresh = true)
    
    assertTrue(result.isSuccess)
    assertTrue(repository.state.value.isSubscribed)
}
```

### Manual Testing Checklist

- [ ] Free state shows paywall
- [ ] Purchase flow launches correctly
- [ ] Successful purchase updates UI immediately
- [ ] Premium features unlock after purchase
- [ ] Restore purchases works after reinstall
- [ ] Expired subscriptions revert to free
- [ ] Offline mode uses cached state (with warning)
- [ ] Re-verification updates stale cache

---

## Common Patterns

### Show Paywall for Premium Feature

```kotlin
@Composable
fun GatedFeature() {
    val subscriptionRepo = koinInject<SubscriptionRepository>()
    val hasFeature = subscriptionRepo.state.collectAsState().value
        .hasFeature(PremiumFeature.ADVANCED_FILTERS)
    
    if (hasFeature) {
        // Show feature
        AdvancedFiltersContent()
    } else {
        // Show paywall
        PaywallCard(
            feature = PremiumFeature.ADVANCED_FILTERS,
            onSubscribe = { /* Navigate to subscription screen */ }
        )
    }
}
```

### Periodic Re-verification

```kotlin
LaunchedEffect(Unit) {
    while (true) {
        delay(1.hours)
        subscriptionRepo.verifySubscription(forceRefresh = false)
    }
}
```

### On App Resume

```kotlin
@Composable
fun AppLifecycleObserver() {
    val subscriptionRepo = koinInject<SubscriptionRepository>()
    
    // When app returns from background, re-verify
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch {
            subscriptionRepo.verifySubscription(forceRefresh = true)
        }
    }
}
```

---

## Troubleshooting

### "Billing client not ready"

**Cause**: BillingClient not initialized
**Fix**: Call `repository.initialize()` on app start

### Purchases not updating

**Cause**: Not listening to purchase updates
**Fix**: Repository automatically listens - ensure initialized

### Cache shows subscribed but verification fails

**Cause**: Subscription expired or refunded
**Fix**: This is expected - cache marked with `needsVerification`

### Test purchases not working

**Cause**: Not using test account or wrong environment
**Fix**: Add account to license testers in Play Console

---

## Migration Notes

### Adding Subscription to Existing App

1. Add all files from this PR
2. Update DI modules (AppModule.kt)
3. Add DataStore configuration
4. Create products in Play Console / App Store Connect
5. Test with test accounts
6. Gate existing features behind premium
7. Deploy with conventional commit: `feat: add subscription support`

### Data Migration

Subscriptions start fresh - no existing user data to migrate.
All users start as FREE tier.

---

## Security Best Practices

### ✅ DO

- Always verify with platform before granting access to premium features
- Use cached state only for instant UI feedback
- Re-verify periodically (every hour)
- Re-verify on app resume
- Mark stale cache with `needsVerification`
- Handle network errors gracefully (fall back to cached state with warning)

### ❌ DON'T

- Never trust DataStore for access control
- Never skip verification for "performance"
- Never expose premium features based only on cache
- Never store purchase tokens in DataStore
- Never implement client-side license validation

---

## Future Enhancements

- [ ] Server-side receipt validation
- [ ] Cross-platform subscription sync (via backend)
- [ ] Promotional codes support
- [ ] Subscription analytics
- [ ] A/B testing for pricing
- [ ] Grace period handling
- [ ] Account recovery flow

---

## References

- [Google Play Billing](https://developer.android.com/google/play/billing)
- [StoreKit 2](https://developer.apple.com/documentation/storekit)
- [In-App Purchase Security](https://developer.apple.com/documentation/storekit/in-app_purchase/validating_receipts_with_the_app_store)
