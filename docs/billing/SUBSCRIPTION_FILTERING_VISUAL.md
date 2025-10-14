# Subscription Package Filtering - Visual Guide

## Quick Reference: What Users See

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     SUBSCRIPTION PACKAGE MATRIX                         │
├─────────────────────┬────────────┬────────────┬────────────┬───────────┤
│   USER STATE        │  LIFETIME  │   ANNUAL   │  MONTHLY   │  BANNER   │
├─────────────────────┼────────────┼────────────┼────────────┼───────────┤
│ 1. NEW USER         │     ✅     │     ✅     │     ✅     │     ❌    │
│    (no SKUs)        │   Show     │   Show     │   Show     │    None   │
├─────────────────────┼────────────┼────────────┼────────────┼───────────┤
│ 2. LEGACY USER      │     ✅     │     ✅     │     ✅     │     ✅    │
│    (old purchase)   │   Show     │   Show     │   Show     │  Legacy   │
├─────────────────────┼────────────┼────────────┼────────────┼───────────┤
│ 3. MONTHLY SUB      │     ✅     │     ✅     │     ❌     │     ❌    │
│    (active monthly) │   Show     │   Show     │   Hide     │    None   │
├─────────────────────┼────────────┼────────────┼────────────┼───────────┤
│ 4. ANNUAL SUB       │     ✅     │     ❌     │     ❌     │     ❌    │
│    (active annual)  │   Show     │   Hide     │   Hide     │    None   │
├─────────────────────┼────────────┼────────────┼────────────┼───────────┤
│ 5. LIFETIME USER    │     ❌     │     ❌     │     ❌     │     ❌    │
│    (has lifetime)   │   Hide     │   Hide     │   Hide     │    None   │
└─────────────────────┴────────────┴────────────┴────────────┴───────────┘
```

## Screen Layout Examples

### 1. New User Screen
```
┌─────────────────────────────────────┐
│  🚀 Space Launch Now                │
│  Become a Member                    │
├─────────────────────────────────────┤
│  ✨ Premium Widget                  │
│  🎨 Premium Themes                  │
│  🚫 No Ads                          │
│  ❤️  Support Development            │
├─────────────────────────────────────┤
│  Choose Your Plan                   │
│                                     │
│  ╔═══════════════════════════════╗ │
│  ║  💎 PRO LIFETIME              ║ │
│  ║  $39.99 One-Time Payment      ║ │
│  ║  [Purchase Lifetime]          ║ │
│  ╚═══════════════════════════════╝ │
│                                     │
│  ───── Or subscribe ─────          │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🏆 YEARLY (Recommended)     │   │
│  │ $19.99/year • Save 30%      │   │
│  │ [Subscribe]                 │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ MONTHLY                     │   │
│  │ $2.99/month                 │   │
│  │ [Subscribe]                 │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 2. Legacy User Screen
```
┌─────────────────────────────────────┐
│  🚀 Thank You! 🎉                   │
│  You're a legacy member!            │
├─────────────────────────────────────┤
│  Current Plan: Legacy Member        │
├─────────────────────────────────────┤
│  ╔═══════════════════════════════╗ │
│  ║ ❤️  Thank You, Early Supporter║ │
│  ║                               ║ │
│  ║ Your legacy purchase helped   ║ │
│  ║ build this app! 🚀            ║ │
│  ║                               ║ │
│  ║ Continue Your Support:        ║ │
│  ║ ✨ Keep premium features      ║ │
│  ║ 🚀 Help add more features    ║ │
│  ║ ✅ Priority support           ║ │
│  ╚═══════════════════════════════╝ │
├─────────────────────────────────────┤
│  Upgrade to Premium                 │
│                                     │
│  💎 PRO LIFETIME     [Show]         │
│  🏆 YEARLY           [Show]         │
│  📅 MONTHLY          [Show]         │
└─────────────────────────────────────┘
```

### 3. Monthly Subscriber Screen
```
┌─────────────────────────────────────┐
│  🚀 Thank You! 🎉                   │
│  You're a premium member!           │
├─────────────────────────────────────┤
│  Current Plan: Premium (Monthly)    │
├─────────────────────────────────────┤
│  Upgrade to Better Value            │
│                                     │
│  ╔═══════════════════════════════╗ │
│  ║  💎 PRO LIFETIME              ║ │
│  ║  $39.99 One-Time Payment      ║ │
│  ║  [Purchase Lifetime]          ║ │
│  ╚═══════════════════════════════╝ │
│                                     │
│  ───── Or upgrade ─────            │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🏆 YEARLY (Save 30%!)       │   │
│  │ $19.99/year                 │   │
│  │ [Upgrade]                   │   │
│  └─────────────────────────────┘   │
│                                     │
│  ❌ MONTHLY (Hidden - you have it) │
└─────────────────────────────────────┘
```

### 4. Annual Subscriber Screen
```
┌─────────────────────────────────────┐
│  🚀 Thank You! 🎉                   │
│  You're a premium member!           │
├─────────────────────────────────────┤
│  Current Plan: Premium (Annual)     │
├─────────────────────────────────────┤
│  Upgrade to Lifetime                │
│                                     │
│  ╔═══════════════════════════════╗ │
│  ║  💎 PRO LIFETIME              ║ │
│  ║  $39.99 One-Time Payment      ║ │
│  ║  Never worry about renewals!  ║ │
│  ║  [Purchase Lifetime]          ║ │
│  ╚═══════════════════════════════╝ │
│                                     │
│  ❌ YEARLY (Hidden - you have it)   │
│  ❌ MONTHLY (Hidden - you have      │
│            better plan)             │
└─────────────────────────────────────┘
```

### 5. Lifetime User Screen
```
┌─────────────────────────────────────┐
│  🚀 Thank You! 🎉                   │
│  You're a pro member!               │
├─────────────────────────────────────┤
│  Current Plan: Pro (Lifetime)       │
├─────────────────────────────────────┤
│  ╔═══════════════════════════════╗ │
│  ║  🎉 You Have Lifetime Access  ║ │
│  ║                               ║ │
│  ║  Thank you for your support!  ║ │
│  ║  You're helping us continue   ║ │
│  ║  building amazing features.   ║ │
│  ║                               ║ │
│  ║  ✅ All premium features      ║ │
│  ║  ✅ No renewals ever          ║ │
│  ║  ✅ Priority support          ║ │
│  ╚═══════════════════════════════╝ │
│                                     │
│  ❌ No upgrade options (you have    │
│     the best plan!)                 │
└─────────────────────────────────────┘
```

## Upgrade Funnel Logic

```
NEW USER
   │
   ├─→ Monthly ──┐
   │             │
   ├─→ Annual ───┼─→ Lifetime
   │             │
   └─→ Lifetime ─┘

LEGACY USER
   │
   ├─→ Monthly ──┐
   │             │
   ├─→ Annual ───┼─→ Lifetime
   │             │
   └─→ Lifetime ─┘

MONTHLY SUBSCRIBER
   │
   ├─→ Annual ───┬─→ Lifetime
   │             │
   └─→ Lifetime ─┘
   (Monthly hidden)

ANNUAL SUBSCRIBER
   │
   └─→ Lifetime
   (Annual & Monthly hidden)

LIFETIME USER
   │
   └─→ No upgrades
   (All packages hidden)
```

## Implementation Pseudo-Code

```kotlin
fun SupportUsScreen() {
    // 1. Determine user state
    val state = when {
        hasLifetime -> LIFETIME_USER
        isLegacy -> LEGACY_USER
        isMonthlySubscriber -> MONTHLY_SUBSCRIBER
        isAnnualSubscriber -> ANNUAL_SUBSCRIBER
        else -> NEW_USER
    }
    
    // 2. Get packages to show
    val packages = when (state) {
        NEW_USER -> all
        LEGACY_USER -> all
        MONTHLY_SUBSCRIBER -> [lifetime, annual]
        ANNUAL_SUBSCRIBER -> [lifetime]
        LIFETIME_USER -> []
    }
    
    // 3. Render UI
    if (packages.showLifetime) {
        ProLifetimeCard()
    }
    
    if (packages.showAnnual) {
        AnnualPricingCard()
    }
    
    if (packages.showMonthly) {
        MonthlyPricingCard()
    }
}
```

## Decision Tree

```
Is user subscribed?
│
├─ NO → NEW_USER
│        └─ Show: All packages
│
└─ YES → Has lifetime?
         │
         ├─ YES → LIFETIME_USER
         │         └─ Show: Nothing
         │
         └─ NO → Is legacy?
                 │
                 ├─ YES → LEGACY_USER
                 │         └─ Show: All packages
                 │
                 └─ NO → Is monthly?
                         │
                         ├─ YES → MONTHLY_SUBSCRIBER
                         │         └─ Show: Annual + Lifetime
                         │
                         └─ NO → ANNUAL_SUBSCRIBER
                                  └─ Show: Lifetime only
```

## Testing Matrix

| Test Case | Simulation Command | Expected Packages |
|-----------|-------------------|-------------------|
| New User | `clearDebugSimulation()` | L + A + M |
| Legacy | `simulateSubscriptionState(LEGACY)` | L + A + M |
| Monthly | `simulateSubscriptionState(PREMIUM, "base-plan")` | L + A |
| Annual | `simulateSubscriptionState(PREMIUM, "yearly")` | L only |
| Lifetime | `simulateSubscriptionState(PREMIUM, "pro")` | None |

**Legend:**
- L = Lifetime
- A = Annual
- M = Monthly

## Business Impact

### Conversion Funnel
```
NEW USER (100 users)
├─→ 40% choose Monthly → MONTHLY_SUBSCRIBER (40 users)
│   └─→ 30% upgrade to Annual (12 users)
│       └─→ 20% upgrade to Lifetime (2-3 users)
│
├─→ 35% choose Annual → ANNUAL_SUBSCRIBER (35 users)
│   └─→ 15% upgrade to Lifetime (5-6 users)
│
└─→ 25% choose Lifetime → LIFETIME_USER (25 users)

Total Lifetime Conversions: ~30-34 users
```

### Before vs After

**Before (Show All):**
- Confusing: "Why am I seeing monthly when I already pay monthly?"
- Decision paralysis: Too many options
- Lower conversion: Unclear upgrade path

**After (Smart Filtering):**
- Clear: Only see relevant upgrades
- Focused: Fewer, better choices
- Higher conversion: Obvious next step

---

**Last Updated:** October 2024
**Related Docs:**
- [SUBSCRIPTION_SMART_FILTERING.md](./SUBSCRIPTION_SMART_FILTERING.md) - Full documentation
- [LEGACY_UPGRADE_ENHANCEMENT.md](./LEGACY_UPGRADE_ENHANCEMENT.md) - Legacy banner
- [TESTING_GUIDE.md](./TESTING_GUIDE.md) - Testing scenarios
