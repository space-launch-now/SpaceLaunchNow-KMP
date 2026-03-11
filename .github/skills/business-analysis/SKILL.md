---
name: business-analysis
description: "Analyze SpaceLaunchNow business environment, monetization, costs, and performance. Use when: reviewing subscription revenue model, CI/CD spend, ad monetization, app store strategy, analytics tracking, premium feature gating, release cadence, or RevenueCat integration health."
---

# Business Analysis for SpaceLaunchNow

## When to Use

- Evaluate or optimize the monetization strategy (subscriptions, ads, premium features)
- Analyze CI/CD pipeline costs and identify savings opportunities
- Review RevenueCat subscription integration health and entitlement mapping
- Assess analytics/observability coverage (Datadog RUM, Firebase)
- Plan app store release strategy or review release cadence
- Audit premium feature gating and conversion funnel
- Compare revenue tiers (monthly vs annual vs lifetime) or plan pricing changes
- Investigate ad network configuration (AdMob, SKAdNetwork)

## Business Model Overview

SpaceLaunchNow uses a **freemium model** with three revenue streams:

1. **Subscriptions** (RevenueCat) — Monthly, Annual, Lifetime plans granting `premium` entitlement
2. **Advertising** (AdMob) — Banner, interstitial, and rewarded ads for free-tier users
3. **Premium Features** — Ad removal, premium widgets, premium themes gated behind subscription

## Procedure

### 1. Identify the Analysis Domain

Determine which business area the question covers:

| Domain | Reference | Data Source |
|--------|-----------|-------------|
| Monetization & Subscriptions | [monetization.md](./references/monetization.md) | RevenueCat MCP (live) + `docs/billing/` |
| Analytics & Observability | [analytics.md](./references/analytics.md) | `analytics/DatadogConfig.kt` |
| App Store & Distribution | [app-store.md](./references/app-store.md) | `docs/release-notes/`, fastlane |

### 2. Gather Live Data (RevenueCat MCP)

The `revenuecat-mvp` MCP server provides live access to subscription data. Use these tools:

| Tool | Purpose | When to Use |
|------|---------|-------------|
| `RC_get_overview_metrics` | MRR, revenue, active subs, trials, active users, new customers | Start here for any revenue question |
| `RC_get_project` | Project ID (`projbe17841f`) and metadata | Needed as input to other calls |
| `RC_list_apps` | List apps (iOS: `app70aaf33046`, Android: `appb9bf4f1820`) | Platform-specific analysis |
| `RC_list_entitlements` | Active entitlements (Pro, Lifetime, Legacy) | Feature gating audit |
| `RC_list_offerings` | Current + legacy offerings | Offering structure review |
| `RC_list_products` | All products across both stores | Product catalog audit |
| `RC_list_packages` | Packages within an offering | Package structure review |
| `RC_get_chart_data` | Historical chart data for trends | Trend/churn analysis |
| `RC_get_chart_options` | Available chart types | Discover what trends are available |

**Project ID:** `projbe17841f` (required for most calls)

**Quick start for any revenue question:**
```
1. Call RC_get_overview_metrics with project_id=projbe17841f
2. Parse: MRR, revenue (28d), active_subscriptions, active_trials, active_users, new_customers
3. Calculate conversion rate: active_subscriptions / active_users
```

### 3. Gather Static Data

- Read the relevant reference doc for architecture and implementation details
- Review `docs/billing/` for subscription architecture decisions
- Review `docs/cicd/` for pipeline cost data and optimization history
- Review `docs/premium/` for feature gating and premium feature definitions
- Check the source files listed in each reference for code-level details

### 4. Analyze

When analyzing, consider:

- **Revenue**: Which subscription tier drives the most value? Is lifetime cannibalizing recurring?
- **Conversion**: Are premium feature gates compelling enough? Is the SupportUsScreen effective?
- **Retention**: Does analytics coverage capture enough user journey data for retention analysis?
- **Compliance**: Are ad networks (SKAdNetwork) properly configured? Privacy implications?

### 5. Recommend

Provide actionable recommendations grounded in the codebase and live data. Reference specific files, configurations, or docs when suggesting changes. Quantify impact using real metrics from RevenueCat MCP where possible (e.g., "$X/month savings", "Y% of 27K active users affected").

## Key Business Metrics to Track

- **MRR/ARR**: Monthly/Annual recurring revenue from subscriptions
- **Conversion Rate**: Free → Paid conversion from SupportUsScreen
- **Release Cadence**: Tracked via `version.properties` and `CHANGELOG.md`
- **Ad Revenue**: AdMob impressions/clicks for non-premium users
- **Churn**: Subscription cancellations tracked via RevenueCat dashboard

## Live Data Access

**RevenueCat MCP** (`revenuecat-mvp`) is configured in `.vscode/mcp.json` and provides real-time access to:
- Revenue metrics (MRR, 28-day revenue, transaction counts)
- Subscriber counts (active subscriptions, trials)
- User metrics (active users, new customers)
- Product catalog, entitlements, offerings, packages
- Chart data for historical trends

**GitHub MCP** (`mcp_github`) provides access to:
- Repository issues and pull requests
- Workflow runs (for CI/CD cost estimation)
- Release history and tags

## Important Context

- **RevenueCat Project ID:** `projbe17841f`
- **iOS App ID:** `app70aaf33046` | **Android App ID:** `appb9bf4f1820`
- **Entitlements:** Pro (`entl9fe2b6018c`), Lifetime (`entleb14c06f19`), Legacy (`entl3ad15261a3`)
- RevenueCat integration is **100% complete** (6 phases) — see `docs/billing/REVENUECAT_COMPLETE_PROGRESS_SUMMARY.md`
- Datadog RUM tracks user sessions with subscription state attributes
- Version and build numbers are in `version.properties` (monotonically increasing for Play Store)
