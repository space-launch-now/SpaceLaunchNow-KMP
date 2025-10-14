# RevenueCat Integration - Quick Start Guide

## ✅ What's Done

- [x] RevenueCat SDK installed (`purchases-kmp`)
- [x] `RevenueCatBillingClient` implemented
- [x] `RevenueCatManager` created
- [x] Offerings configured in RevenueCat dashboard

## 🎯 Immediate Next Steps (In Order)

### Step 1: Configure RevenueCat Entitlements (15 min)

**Go to RevenueCat Dashboard:**

1. Navigate to: **Entitlements** tab
2. Create entitlement: **`premium`**
   - Display Name: "Premium Features"
   - Description: "Access to all premium features"
3. Attach products to `premium` entitlement:
   - ✅ `spacelaunchnow_pro` (iOS + Android)
   - ✅ `base_plan` (iOS)
   - ✅ `sln_production_yearly:base-plan` (Android)
   - ✅ `yearly` (iOS)
   - ✅ `sln_production_yearly:yearly` (Android)
4. (Optional) Create `founder` entitlement for `2018_founder` SKU

**Result:** All purchases grant "premium" entitlement

---

### Step 2: Update Dependency Injection (10 min)

**File:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/di/AppModule.kt`

**Replace:**
```kotlin
factory { createBillingClient() }
```

**With:**
```kotlin
single<BillingClient> {
    BillingClient(
        revenueCatClient = RevenueCatBillingClient(
            purchases = Purchases.sharedInstance
        )
    )
}
```

**Also update SubscriptionRepositoryImpl:**
```kotlin
single<SubscriptionRepository> {
    SubscriptionRepositoryImpl(
        billingClient = get(),
        storage = get<SubscriptionStorage>(),
        debugPreferences = get<DebugPreferences>(),
        revenueCatManager = get()  // ADD THIS
    )
}
```

---

### Step 3: Make BillingClient a Wrapper (15 min)

**File:** `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/BillingClient.kt`

**Change from `expect class` to regular class:**

```kotlin
// Remove expect/actual pattern
// Change to:
class BillingClient(
    private val revenueCatClient: RevenueCatBillingClient
) {
    suspend fun initialize(): Result<Unit> = revenueCatClient.initialize()
    
    suspend fun queryPurchases(): Result<List<PlatformPurchase>> = 
        revenueCatClient.queryPurchases()
    
    suspend fun launchPurchaseFlow(productId: String, basePlanId: String? = null): Result<String> = 
        revenueCatClient.launchPurchaseFlow(productId, basePlanId)
    
    suspend fun acknowledgePurchase(purchaseToken: String): Result<Unit> = 
        revenueCatClient.acknowledgePurchase(purchaseToken)
    
    suspend fun getAvailableProducts(): Result<List<String>> = 
        revenueCatClient.getAvailableProducts()
    
    suspend fun getProductPricing(productId: String): Result<List<ProductPricing>> = 
        revenueCatClient.getProductPricing(productId)
    
    val purchaseUpdates: Flow<PlatformPurchase> = revenueCatClient.purchaseUpdates
    
    fun disconnect() = revenueCatClient.disconnect()
}

// Keep SubscriptionProducts as-is
object SubscriptionProducts { ... }
```

---

### Step 4: Initialize RevenueCat on App Start (20 min)

**Android - File:** `composeApp/src/androidMain/kotlin/.../MainApplication.kt`

**Add after Koin setup:**
```kotlin
override fun onCreate() {
    super.onCreate()
    instance = this
    
    initializeBuildConfig()
    
    // Start Koin
    val koin = startKoin {
        androidLogger(Level.DEBUG)
        androidContext(this@MainApplication)
        includes(koinConfig)
    }
    
    // Initialize RevenueCat
    lifecycleScope.launch {
        try {
            val revenueCatManager = koin.koin.get<RevenueCatManager>()
            revenueCatManager.initialize()
            Log.d("MainApplication", "RevenueCat initialized successfully")
        } catch (e: Exception) {
            Log.e("MainApplication", "RevenueCat initialization failed", e)
        }
    }
    
    // ... rest of onCreate
}
```

**iOS - File:** `iosApp/iosApp/iOSApp.swift`

**Add initialization:**
```swift
import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    init() {
        KoinKt.doInitKoin()
        
        // Initialize RevenueCat
        let revenueCatManager = KoinKt.getRevenueCatManager()
        Task {
            await revenueCatManager.initialize(appUserId: nil)
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

---

### Step 5: Update SubscriptionViewModel (30 min)

**File:** `composeApp/src/commonMain/kotlin/.../ui/viewmodel/SubscriptionViewModel.kt`

**Add offering state:**
```kotlin
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Package

class SubscriptionViewModel(
    private val repository: SubscriptionRepository,
    private val revenueCatManager: RevenueCatManager  // ADD THIS via Koin
) : ViewModel(), KoinComponent {
    
    // Add offering state
    val currentOffering: StateFlow<Offering?> = revenueCatManager.currentOffering
    
    init {
        // ... existing init code ...
        
        // Load offerings
        viewModelScope.launch {
            revenueCatManager.isInitialized
                .filter { it }
                .first()
            revenueCatManager.refreshOfferings()
        }
    }
    
    // Add purchase method
    fun purchasePackage(pkg: Package) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = repository.launchPurchaseFlow(
                productId = pkg.product.id,
                basePlanId = pkg.identifier
            )
            
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Thank you for supporting Space Launch Now! 🚀"
                        )
                    }
                    repository.verifySubscription(forceRefresh = true)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Purchase failed: ${error.message}"
                        )
                    }
                }
            )
        }
    }
}
```

---

### Step 6: Update SupportUsScreen (1 hour)

**File:** `composeApp/src/commonMain/kotlin/.../ui/subscription/SupportUsScreen.kt`

**Get RevenueCat packages:**
```kotlin
@Composable
fun SupportUsScreen(
    viewModel: SubscriptionViewModel = koinInject(),
    onNavigateBack: () -> Unit = {}
) {
    val currentOffering by viewModel.currentOffering.collectAsState()
    
    // Get packages - these map to your offering
    val lifetimePackage = currentOffering?.lifetime  // $rc_lifetime
    val monthlyPackage = currentOffering?.monthly    // $rc_monthly
    val annualPackage = currentOffering?.annual      // $rc_annual
    
    LazyColumn {
        // Hero section (unchanged)
        item { HeroSection() }
        
        // Golden Lifetime Card
        item {
            LifetimeProCard(
                packageInfo = lifetimePackage,
                onPurchaseClick = {
                    lifetimePackage?.let { viewModel.purchasePackage(it) }
                }
            )
        }
        
        item { HorizontalDivider() }
        
        // Subscription options
        item {
            PricingCard(
                title = "Monthly",
                subtitle = "Base Plan",
                packageInfo = monthlyPackage,
                onPurchaseClick = {
                    monthlyPackage?.let { viewModel.purchasePackage(it) }
                }
            )
        }
        
        item {
            PricingCard(
                title = "Yearly",
                subtitle = "Best Value",
                packageInfo = annualPackage,
                onPurchaseClick = {
                    annualPackage?.let { viewModel.purchasePackage(it) }
                }
            )
        }
    }
}

@Composable
fun LifetimeProCard(
    packageInfo: Package?,
    onPurchaseClick: () -> Unit
) {
    val price = packageInfo?.product?.price?.formatted ?: "$99.99"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Space Launch Now - Pro Lifetime",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                price,
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onPurchaseClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Purchase Lifetime Access")
            }
        }
    }
}

@Composable
fun PricingCard(
    title: String,
    subtitle: String,
    packageInfo: Package?,
    onPurchaseClick: () -> Unit
) {
    val price = packageInfo?.product?.price?.formatted ?: "Loading..."
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(price, style = MaterialTheme.typography.headlineMedium)
            Button(
                onClick = onPurchaseClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Subscribe")
            }
        }
    }
}
```

---

## 🧪 Testing Checklist

After completing steps 1-6:

- [ ] Run app in debug mode
- [ ] Check logs for "RevenueCat initialized successfully"
- [ ] Navigate to Support Us screen
- [ ] Verify pricing loads from RevenueCat (not hardcoded)
- [ ] Test purchase flow (sandbox mode)
- [ ] Verify entitlement appears in RevenueCat dashboard
- [ ] Test restore purchases
- [ ] Test with legacy SKU (if available)

---

## 🚨 Common Issues

### Issue: "Purchases not initialized"
**Solution:** Ensure `revenueCatManager.initialize()` is called before accessing offerings

### Issue: Prices show as "$0.00"
**Solution:** Check that products are configured correctly in Google Play/App Store Console

### Issue: "Product not found"
**Solution:** Verify offering ID matches: `ofrng74226a750e`

### Issue: Legacy purchases not detected
**Solution:** Create entitlement in RevenueCat dashboard and attach legacy SKUs

---

## 📊 Verification

**After each step, verify:**
1. ✅ App compiles without errors
2. ✅ No Koin injection errors
3. ✅ RevenueCat logs show initialization
4. ✅ Offerings load successfully
5. ✅ Purchase flow works in sandbox

---

## 🎉 Success Criteria

You'll know it's working when:
- ✅ Support Us screen shows real prices from RevenueCat
- ✅ Purchase flow completes successfully
- ✅ Entitlements appear immediately in app
- ✅ RevenueCat dashboard shows purchase events
- ✅ Legacy purchases are detected and honored

---

## 📚 Next Phase

Once Steps 1-6 are working:
- Implement feature gating (see main plan)
- Add restore purchases button
- Test with beta users
- Deploy to production

**See full plan:** `docs/REVENUECAT_TRANSITION_PLAN.md`
