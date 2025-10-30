# Feature Gating with RevenueCat - Implementation Guide

## Overview

This guide shows exactly how to gate premium features using RevenueCat entitlements.

---

## Architecture

```
User Interaction
       ↓
Check Cached State (instant feedback)
       ↓
Verify with RevenueCat (source of truth)
       ↓
Grant/Deny Access
```

---

## Step 1: Update PremiumFeature Enum

**File:** `composeApp/src/commonMain/kotlin/.../data/model/PremiumFeature.kt`

**Add RevenueCat entitlement mapping:**

```kotlin
package me.calebjones.spacelaunchnow.data.model

import me.calebjones.spacelaunchnow.data.billing.SubscriptionProducts

enum class PremiumFeature(
    val displayName: String,
    val description: String,
    val revenueCatEntitlementId: String  // NEW: Maps to RevenueCat
) {
    REMOVE_ADS(
        displayName = "Remove Ads",
        description = "Enjoy an ad-free experience",
        revenueCatEntitlementId = SubscriptionProducts.RC_ENTITLEMENT_PREMIUM  // All premium users get this
    ),
    PREMIUM_WIDGETS(
        displayName = "Premium Widgets",
        description = "Access advanced home screen widgets",
        revenueCatEntitlementId = SubscriptionProducts.RC_ENTITLEMENT_PREMIUM
    ),
    PREMIUM_THEMES(
        displayName = "Premium Themes",
        description = "Unlock all color themes and customization",
        revenueCatEntitlementId = SubscriptionProducts.RC_ENTITLEMENT_PREMIUM
    );

    companion object {
        /**
         * Get all free features (available to everyone)
         */
        fun getFreeFeatures(): Set<PremiumFeature> {
            // In your case, no features are free - all require premium
            return emptySet()
        }

        /**
         * Get all premium features (requires subscription)
         */
        fun getPremiumFeatures(): Set<PremiumFeature> {
            return values().toSet()
        }

        /**
         * Map RevenueCat entitlement to features
         */
        fun fromEntitlementId(entitlementId: String): Set<PremiumFeature> {
            return values().filter { 
                it.revenueCatEntitlementId == entitlementId 
            }.toSet()
        }
    }
}
```

---

## Step 2: Update SubscriptionRepository

**File:** `composeApp/src/commonMain/kotlin/.../data/repository/SubscriptionRepositoryImpl.kt`

**Add RevenueCat verification:**

```kotlin
class SubscriptionRepositoryImpl(
    private val billingClient: BillingClient,
    private val storage: SubscriptionStorage,
    private val debugPreferences: DebugPreferences,
    private val revenueCatManager: RevenueCatManager  // ADD THIS
) : SubscriptionRepository {

    override suspend fun hasFeature(
        feature: PremiumFeature,
        verify: Boolean
    ): Boolean {
        // For sensitive operations, always verify with RevenueCat
        if (verify) {
            revenueCatManager.refreshCustomerInfo()
            val hasEntitlement = revenueCatManager.hasEntitlement(feature.revenueCatEntitlementId)
            
            // Update cached state after verification
            if (hasEntitlement != state.value.hasFeature(feature)) {
                verifySubscription(forceRefresh = true)
            }
            
            return hasEntitlement
        }
        
        // For UI hints, use cached state (faster)
        return state.value.hasFeature(feature)
    }

    override suspend fun verifySubscription(forceRefresh: Boolean): Result<SubscriptionState> {
        return try {
            // Skip if recently verified (unless forced)
            if (!forceRefresh && state.value.isRecentlyVerified()) {
                return Result.success(state.value)
            }

            // Refresh from RevenueCat
            revenueCatManager.refreshCustomerInfo()

            // Get active entitlements
            val activeEntitlements = revenueCatManager.getActiveEntitlements()
            
            // Convert entitlements to features
            val features = activeEntitlements.flatMap { entitlementId ->
                PremiumFeature.fromEntitlementId(entitlementId)
            }.toSet()

            // Check if user has premium entitlement
            val hasPremium = revenueCatManager.hasEntitlement(SubscriptionProducts.RC_ENTITLEMENT_PREMIUM)

            val verifiedState = SubscriptionState(
                isSubscribed = hasPremium,
                subscriptionType = if (hasPremium) SubscriptionType.PREMIUM else SubscriptionType.FREE,
                features = features,
                lastVerified = Clock.System.now().toEpochMilliseconds(),
                needsVerification = false,
                isCached = false
            )

            // Update cache
            storage.saveState(verifiedState)
            _state.emit(verifiedState)

            Result.success(verifiedState)
        } catch (e: Exception) {
            // On error, return cached state but mark as needing verification
            val errorState = state.value.copy(
                needsVerification = true,
                verificationError = e.message
            )
            _state.emit(errorState)
            Result.failure(e)
        }
    }

    override suspend fun getAvailableFeatures(): Set<PremiumFeature> {
        return state.value.features
    }
}
```

---

## Step 3: Create Feature Gate Composable

**File:** `composeApp/src/commonMain/kotlin/.../ui/components/PremiumFeatureGate.kt`

```kotlin
package me.calebjones.spacelaunchnow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import org.koin.compose.koinInject

/**
 * Gate premium features behind subscription check
 * 
 * Usage:
 * ```
 * PremiumFeatureGate(feature = PremiumFeature.PREMIUM_WIDGETS) {
 *     // Premium content only shown if user has feature
 *     AdvancedWidgetSettings()
 * }
 * ```
 */
@Composable
fun PremiumFeatureGate(
    feature: PremiumFeature,
    modifier: Modifier = Modifier,
    onUpgradeClick: () -> Unit = {},
    showUpgradePrompt: Boolean = true,
    content: @Composable () -> Unit
) {
    val repository = koinInject<SubscriptionRepository>()
    val subscriptionState by repository.state.collectAsState()
    
    val hasFeature = subscriptionState.hasFeature(feature)
    
    if (hasFeature) {
        // User has access - show content
        content()
    } else if (showUpgradePrompt) {
        // User doesn't have access - show upgrade prompt
        UpgradePromptCard(
            feature = feature,
            onUpgradeClick = onUpgradeClick,
            modifier = modifier
        )
    } else {
        // Don't show anything
    }
}

@Composable
private fun UpgradePromptCard(
    feature: PremiumFeature,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Premium Feature",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = feature.displayName,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upgrade to Premium")
            }
        }
    }
}

/**
 * Helper composable to check feature access without showing upgrade prompt
 */
@Composable
fun rememberHasFeature(feature: PremiumFeature): State<Boolean> {
    val repository = koinInject<SubscriptionRepository>()
    return remember {
        repository.state.map { it.hasFeature(feature) }
    }.collectAsState(false)
}
```

---

## Step 4: Implement Feature Gates

### Example 1: Premium Widgets

**File:** `composeApp/src/androidMain/kotlin/.../widgets/WidgetConfigActivity.kt`

```kotlin
@Composable
fun WidgetConfigScreen(navController: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Widget Settings") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Basic widget settings (always available)
            item {
                Text("Basic Widget", style = MaterialTheme.typography.titleLarge)
                BasicWidgetOptions()
            }
            
            // Premium widgets (gated)
            item {
                PremiumFeatureGate(
                    feature = PremiumFeature.PREMIUM_WIDGETS,
                    onUpgradeClick = { navController.navigate(SupportUs) }
                ) {
                    Text("Advanced Widgets", style = MaterialTheme.typography.titleLarge)
                    AdvancedWidgetOptions()
                    CustomizableLayouts()
                    RealTimeUpdates()
                }
            }
        }
    }
}

@Composable
fun AdvancedWidgetOptions() {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Advanced Options", style = MaterialTheme.typography.titleMedium)
            
            SwitchPreference(
                title = "Live Updates",
                summary = "Update widget in real-time"
            )
            
            SwitchPreference(
                title = "Custom Layout",
                summary = "Choose from 10+ layouts"
            )
            
            SwitchPreference(
                title = "Background Sync",
                summary = "Keep widget data fresh"
            )
        }
    }
}
```

### Example 2: Remove Ads

**File:** `composeApp/src/commonMain/kotlin/.../ui/home/HomeScreen.kt`

```kotlin
@Composable
fun LaunchListScreen() {
    val repository = koinInject<SubscriptionRepository>()
    val hasAdFree by rememberHasFeature(PremiumFeature.REMOVE_ADS)
    
    LazyColumn {
        itemsIndexed(launches) { index, launch ->
            LaunchCard(launch)
            
            // Show ad every 5 items if user doesn't have ad-free
            if (!hasAdFree && index % 5 == 4) {
                item {
                    AdBanner(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val navController = LocalNavController.current
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Remove Ads", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Upgrade to Premium",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            TextButton(onClick = { navController.navigate(SupportUs) }) {
                Text("Upgrade")
            }
        }
    }
}
```

### Example 3: Premium Themes

**File:** `composeApp/src/commonMain/kotlin/.../ui/settings/ThemeSettings.kt`

```kotlin
@Composable
fun ThemeSettingsScreen() {
    val repository = koinInject<SubscriptionRepository>()
    val hasPremiumThemes by rememberHasFeature(PremiumFeature.PREMIUM_THEMES)
    
    LazyColumn {
        // Free themes (always available)
        item {
            SectionHeader("Basic Themes")
        }
        
        items(basicThemes) { theme ->
            ThemeOption(
                theme = theme,
                isSelected = currentTheme == theme,
                onSelect = { setTheme(theme) }
            )
        }
        
        // Premium themes (gated)
        item {
            SectionHeader("Premium Themes")
        }
        
        if (hasPremiumThemes) {
            items(premiumThemes) { theme ->
                ThemeOption(
                    theme = theme,
                    isSelected = currentTheme == theme,
                    onSelect = { setTheme(theme) }
                )
            }
        } else {
            item {
                PremiumThemesLockedCard(
                    onUpgradeClick = { navController.navigate(SupportUs) }
                )
            }
        }
    }
}

@Composable
fun PremiumThemesLockedCard(onUpgradeClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preview of locked themes (blurred)
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                premiumThemes.take(3).forEach { theme ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.linearGradient(theme.colors),
                                shape = CircleShape
                            )
                            .alpha(0.5f)
                    )
                }
                Text("+${premiumThemes.size - 3} more")
            }
            
            Icon(Icons.Default.Lock, contentDescription = null)
        }
        
        Button(
            onClick = onUpgradeClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Unlock Premium Themes")
        }
    }
}
```

---

## Step 5: Verify Access for Sensitive Operations

### Example: Export Data (Requires Verification)

```kotlin
@Composable
fun ExportDataButton() {
    val repository = koinInject<SubscriptionRepository>()
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    Button(
        onClick = {
            scope.launch {
                isExporting = true
                error = null
                
                // CRITICAL: Verify with RevenueCat before allowing export
                val hasFeature = repository.hasFeature(
                    feature = PremiumFeature.PREMIUM_WIDGETS,
                    verify = true  // ← Forces verification with RevenueCat
                )
                
                if (hasFeature) {
                    // User verified - allow export
                    exportData()
                } else {
                    error = "Premium feature required"
                }
                
                isExporting = false
            }
        },
        enabled = !isExporting
    ) {
        if (isExporting) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp))
        } else {
            Text("Export Data")
        }
    }
    
    error?.let { errorMessage ->
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
```

---

## Testing Feature Gates

### Test Checklist

- [ ] **Without Subscription:**
  - [ ] Premium widgets show upgrade prompt
  - [ ] Ads are visible
  - [ ] Premium themes show locked state
  - [ ] Sensitive operations are blocked

- [ ] **With Active Subscription:**
  - [ ] Premium widgets are accessible
  - [ ] No ads shown
  - [ ] All themes unlocked
  - [ ] Sensitive operations work

- [ ] **With Legacy Purchase (`2018_founder`):**
  - [ ] All features accessible (if configured in RevenueCat)
  - [ ] No upgrade prompts shown

- [ ] **Offline Mode:**
  - [ ] Cached entitlements work
  - [ ] Sensitive operations still require online verification

---

## Security Best Practices

### ✅ DO:
- Always verify with `verify=true` for sensitive operations
- Use cached state for UI hints (faster UX)
- Re-verify periodically (hourly)
- Handle verification failures gracefully

### ❌ DON'T:
- Trust cached state for access control
- Allow operations without verification
- Assume offline state is accurate
- Skip verification to "improve performance"

---

## Monitoring & Analytics

### RevenueCat Dashboard

Monitor these metrics:
- Active subscriptions
- Churn rate
- Failed purchases
- Entitlement access patterns

### Custom Events

```kotlin
fun trackFeatureAccess(feature: PremiumFeature, hasAccess: Boolean) {
    // Log to your analytics
    analytics.logEvent("feature_gate_check") {
        param("feature", feature.name)
        param("has_access", hasAccess)
        param("subscription_type", subscriptionState.subscriptionType.name)
    }
}
```

---

## Summary

**3 Ways to Check Features:**

1. **UI Hints (Cached):** `rememberHasFeature(PremiumFeature.REMOVE_ADS)`
2. **Feature Gates:** `PremiumFeatureGate(feature = ...) { content }`
3. **Verified Check:** `repository.hasFeature(feature, verify = true)`

**Choose based on use case:**
- UI hints: Show/hide elements
- Feature gates: Block entire sections
- Verified checks: Sensitive operations

**Remember:** Always verify before granting access to premium features!
