# Dynamic Pricing from Google Play

## ✅ Implementation Complete

The app now automatically fetches and displays real pricing from Google Play Console instead of using hardcoded values.

## How It Works

### 1. Pricing Data Model (`ProductPricing.kt`)

New data class that holds pricing information from the platform:

```kotlin
data class ProductPricing(
    val productId: String,
    val basePlanId: String,
    val formattedPrice: String,        // e.g., "$4.99"
    val priceCurrencyCode: String,     // e.g., "USD"
    val priceAmountMicros: Long,       // Price in micros
    val billingPeriod: String,         // e.g., "P1M", "P1Y"
    val title: String,
    val description: String
)
```

**Features**:
- `getBillingPeriodText()` - Converts "P1M" to "month", "P1Y" to "year"
- `calculateSavingsPercent(comparedTo)` - Calculates savings percentage between plans

### 2. Billing Client Updates

Added new method to fetch pricing:

```kotlin
suspend fun getProductPricing(productId: String): Result<List<ProductPricing>>
```

**Android Implementation** (`AndroidBillingClient.kt`):
- Queries Google Play Billing for product details
- Extracts pricing from each base plan's pricing phases
- Returns formatted pricing information (currency-aware)
- Logs pricing as it's discovered

**Example log output**:
```
AndroidBillingClient: Querying pricing for sln_production_yearly...
AndroidBillingClient: Found pricing - base-plan: $4.99
AndroidBillingClient: Found pricing - yearly: $24.99
```

### 3. Repository Integration

Added pricing fetch to `SubscriptionRepository`:

```kotlin
suspend fun getProductPricing(productId: String): Result<List<ProductPricing>>
```

Simply delegates to the billing client.

### 4. ViewModel Updates

**Auto-loading on initialization**:
```kotlin
init {
    repository.initialize()
    loadPricing()  // Fetches pricing automatically
}
```

**Updated `SubscriptionUiState`**:
```kotlin
data class SubscriptionUiState(
    val monthlyPricing: ProductPricing? = null,
    val yearlyPricing: ProductPricing? = null,
    // ... other fields
) {
    fun getMonthlyPrice(): String = monthlyPricing?.formattedPrice ?: "$4.99"
    fun getYearlyPrice(): String = yearlyPricing?.formattedPrice ?: "$24.99"
    fun getSavingsPercent(): String {
        // Calculates real savings or returns fallback
    }
}
```

**Fallback pricing**: If pricing query fails, shows default values ($4.99/$24.99).

### 5. UI Updates

**SupportUsScreen.kt**:
```kotlin
PricingCard(
    title = "Yearly",
    price = uiState.getYearlyPrice(),    // From Google Play
    savings = uiState.getSavingsPercent(), // Calculated savings
    // ...
)
```

**SubscriptionScreen.kt**:
```kotlin
SubscriptionTierCard(
    title = "Premium Yearly",
    price = "${uiState.getYearlyPrice()}/year",  // From Google Play
    subtitle = uiState.getSavingsPercent(),       // Calculated
    // ...
)
```

## Benefits

### ✅ Automatic Currency Conversion
Google Play automatically shows prices in the user's local currency:
- US: $4.99
- EU: €4.99
- UK: £4.99
- JP: ¥500

### ✅ Accurate Pricing
No need to manually update hardcoded prices when you change pricing in Google Play Console.

### ✅ Dynamic Savings Calculation
Automatically calculates the correct savings percentage based on actual prices.

### ✅ Graceful Fallback
If pricing query fails (offline, etc.), shows sensible defaults.

## Pricing Query Flow

```
App Initialization
    ↓
SubscriptionViewModel.init()
    ↓
loadPricing()
    ↓
repository.getProductPricing(PRODUCT_ID)
    ↓
billingClient.getProductPricing(PRODUCT_ID)
    ↓
Google Play Billing Library
    ↓
QueryProductDetailsAsync()
    ↓
Extract pricing from SubscriptionOfferDetails
    ↓
Return List<ProductPricing>
    ↓
Update UI State
    ↓
UI displays real prices
```

## Testing

### See Real Pricing

1. **Ensure billing client is initialized**:
   ```
   AndroidBillingClient: Connected successfully
   ```

2. **Check pricing logs**:
   ```
   SubscriptionViewModel: Loading pricing...
   AndroidBillingClient: Querying pricing for sln_production_yearly...
   AndroidBillingClient: Found pricing - base-plan: $4.99
   AndroidBillingClient: Found pricing - yearly: $24.99
   ```

3. **UI updates automatically** with real prices from Google Play

### Pricing Not Showing?

**Problem**: UI still shows fallback prices

**Debug steps**:
1. Check logs for pricing query errors
2. Verify product exists in Google Play Console: `sln_production_yearly`
3. Verify base plans are active: `base-plan`, `yearly`
4. Ensure app is installed from Play Store (license tester)
5. Check Google Play Services is updated

**Common issue**: App installed from APK instead of Play Store
- Solution: Install from internal testing track

## Currency Handling

Google Play automatically handles currency conversion based on:
- User's country
- Play Store account settings
- Product price configuration in Play Console

### Example: Multi-Currency

If you configure pricing in Play Console:
- US: $4.99 / $24.99
- EU: €4.49 / €22.99
- UK: £3.99 / £19.99

The app will automatically show:
- US users: "$4.99" / "$24.99"
- EU users: "€4.49" / "€22.99"
- UK users: "£3.99" / "£19.99"

No code changes needed! Google Play handles everything.

## Savings Calculation

The `calculateSavingsPercent()` method normalizes prices to yearly cost and calculates savings:

```kotlin
Monthly: $4.99/month → $59.88/year
Yearly: $24.99/year

Savings = ($59.88 - $24.99) / $59.88 * 100 = 58%
```

This works regardless of currency because it uses `priceAmountMicros` (not formatted strings).

## Configuration

### Change Pricing in Google Play Console

1. Go to Google Play Console → Your App → Subscriptions
2. Select `sln_production_yearly`
3. Edit base plans:
   - `base-plan`: Change monthly price
   - `yearly`: Change yearly price
4. Save changes
5. **App automatically shows new prices** (no code changes needed!)

Wait 2-4 hours for changes to propagate to all Google Play servers.

## Error Handling

### Pricing Query Failure

If `getProductPricing()` fails:
- **UI**: Shows fallback prices ($4.99 / $24.99)
- **Log**: "Failed to load pricing - [error message]"
- **User experience**: Can still purchase subscriptions

Reasons for failure:
- No internet connection
- Google Play Services not installed/outdated
- Product not found (wrong product ID)
- App not installed from Play Store

### Graceful Degradation

The app always works, even if pricing query fails:
1. Try to fetch real pricing
2. If fails, use fallback prices
3. User can still complete purchases
4. Next app restart will retry pricing fetch

## Future Enhancements

### Potential improvements:

- [ ] Cache pricing to reduce API calls
- [ ] Retry pricing fetch on network reconnect
- [ ] Show currency code in UI (e.g., "USD $4.99")
- [ ] Support promotional pricing (intro offers)
- [ ] Show localized product descriptions
- [ ] Display billing period from API (monthly/yearly text)

## Reference

- [Google Play Billing Library - Query Products](https://developer.android.com/google/play/billing/integrate#query-products)
- [ProductDetails API](https://developer.android.com/reference/com/android/billingclient/api/ProductDetails)
- [SubscriptionOfferDetails](https://developer.android.com/reference/com/android/billingclient/api/ProductDetails.SubscriptionOfferDetails)
