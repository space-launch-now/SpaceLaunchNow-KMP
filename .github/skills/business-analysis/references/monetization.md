# Monetization & Subscription Reference

## Live Data Access

Query live RevenueCat data via the `revenuecat-mvp` MCP server (configured in `.vscode/mcp.json`).

### Quick Revenue Snapshot

Call `RC_get_overview_metrics` with `project_id=projbe17841f` to get:
- **MRR**: Monthly recurring revenue
- **Revenue (28d)**: Total revenue last 28 days (includes one-time purchases)
- **Active Subscriptions**: Current paying subscribers
- **Active Trials**: Users in trial period
- **Active Users (28d)**: Total active users
- **New Customers (28d)**: New installs/signups
- **Transactions (28d)**: Purchase events

### RevenueCat IDs Reference

| Entity | ID | Name |
|--------|-----|------|
| Project | `projbe17841f` | Space Launch Now |
| iOS App | `app70aaf33046` | Space Launch Now (App Store) |
| Android App | `appb9bf4f1820` | Space Launch Now (Play Store) |
| Pro Entitlement | `entl9fe2b6018c` | Space Launch Now - Pro |
| Lifetime Entitlement | `entleb14c06f19` | Space Launch Now - Lifetime |
| Legacy Entitlement | `entl3ad15261a3` | Legacy - Previous IAPs |
| Current Offering | `ofrng74226a750e` | default |

### Available MCP Tools

| Tool | Use For |
|------|---------|
| `RC_get_overview_metrics` | Revenue snapshot (MRR, subs, users) |
| `RC_list_apps` | Platform breakdown |
| `RC_list_entitlements` | Entitlement audit |
| `RC_list_offerings` | Offering structure (current + 8 legacy) |
| `RC_list_products` | Full product catalog (20+ products) |
| `RC_list_packages` | Packages within a specific offering |
| `RC_get_chart_data` | Historical trends |
| `RC_get_chart_options` | Available chart types |
| `RC_get_entitlement` | Single entitlement detail |
| `RC_get_app` | Single app detail |

## Revenue Model

SpaceLaunchNow uses a **freemium** model with three revenue streams:

### 1. Subscriptions (RevenueCat)

**Platform:** RevenueCat KMP SDK (Kotlin Multiplatform)  
**Status:** 100% integrated (6 phases complete, Jan 2025)

#### Product Tiers

| Tier | RevenueCat Package | Android Product ID | iOS Product ID |
|------|-------------------|-------------------|----------------|
| Monthly | `$rc_monthly` | `spacelaunchnow_pro:base-plan` | `spacelaunchnow_pro` |
| Annual | `$rc_annual` | `spacelaunchnow_pro:yearly` | `yearly` |
| Lifetime | `$rc_lifetime` | `spacelaunchnow_pro_lifetime` | `spacelaunchnow_pro_lifetime` |

**Entitlement:** All tiers grant the single `premium` entitlement.

#### Key Code Files

| File | Purpose |
|------|---------|
| `data/billing/RevenueCatManager.kt` | Core manager (215 lines) |
| `data/billing/RevenueCatBillingClient.kt` | BillingClient implementation (228 lines) |
| `data/billing/BillingClient.kt` | Interface definition |
| `data/repository/SubscriptionRepository.kt` | Repository interface |
| `data/repository/SubscriptionProcessor.kt` | Subscription logic |
| `data/subscription/LocalSubscriptionStorage.kt` | Local cache |
| `data/subscription/SubscriptionSyncer.kt` | RevenueCat sync |
| `ui/subscription/SupportUsScreen.kt` | Purchase UI with dynamic pricing |
| `ui/viewmodel/SubscriptionViewModel.kt` | Subscription UI state |

All paths relative to `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/`.

#### Architecture

```
SupportUsScreen (UI)
    ↓
SubscriptionViewModel
    ↓
SubscriptionRepository → SubscriptionProcessor
    ↓
RevenueCatManager → RevenueCatBillingClient
    ↓
RevenueCat SDK (KMP)
    ↓
Google Play / App Store
```

### 2. Advertising (AdMob)

**Free-tier users** see ads. Premium subscribers have ads removed.

| Ad Format | Usage |
|-----------|-------|
| Banner | In-feed, persistent |
| Interstitial | Between screens |
| Rewarded | Optional engagement |

**Android:** App ID via `ADMOB_APP_ID` build config, test ID in debug builds.  
**iOS:** App ID via `generate-ios-secrets.sh` → `GADApplicationIdentifier` in Info.plist.  
**SKAdNetwork:** Configured in iOS Info.plist for conversion tracking compliance.

### 3. Premium Features

| Feature | Enum Value | Description |
|---------|------------|-------------|
| Remove Ads | `REMOVE_ADS` | Ad-free experience |
| Premium Widgets | `PREMIUM_WIDGETS` | Advanced home screen widgets |
| Premium Themes | `PREMIUM_THEMES` | 10 palette styles + custom color picker |

**Gating:** `PremiumFeatureGate.kt` composable, `PremiumFeature` enum mapped to `premium` entitlement.

## Key Documentation

| Doc | Path | Content |
|-----|------|---------|
| Integration Summary | `docs/billing/REVENUECAT_COMPLETE_PROGRESS_SUMMARY.md` | 6-phase completion status |
| Feature Gating | `docs/premium/REVENUECAT_FEATURE_GATING.md` | How premium features are gated |
| Theme Customization | `docs/premium/THEME_CUSTOMIZATION.md` | 10 palette styles detail |
| iOS Widget Gating | `docs/premium/IOS_WIDGET_PREMIUM_GATING.md` | Swift/Kotlin bridge for widgets |
| Troubleshooting | `docs/billing/REVENUECAT_TROUBLESHOOTING.md` | Common billing issues |
| Legacy Migration | `docs/billing/LEGACY_PURCHASE_RESTORATION.md` | Legacy → RevenueCat migration |
| Trial Conversion | `docs/billing/TRIAL_CONVERSION_FIX.md` | Trial → paid conversion fix |

## Business Questions This Enables

- What is the subscription tier distribution?
- Is lifetime cannibalizing recurring revenue?
- What is the free → paid conversion rate from SupportUsScreen?
- Are legacy purchases properly migrated?
- Is the feature gate compelling enough to drive upgrades?
- What ad revenue is generated from free-tier users?
- Should pricing or tier structure change?
