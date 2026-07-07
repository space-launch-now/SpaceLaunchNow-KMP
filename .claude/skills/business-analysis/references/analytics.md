# Analytics & Observability Reference

## Stack

| Tool | Purpose | Platforms |
|------|---------|-----------|
| Datadog RUM | Real User Monitoring, performance, crashes | Android, iOS |
| Datadog Logs | Structured logging with attributes | Android, iOS |
| Firebase Analytics | Event tracking, user properties | Android, iOS |
| Firebase Crashlytics | Crash reporting | Android, iOS |

Desktop platform has analytics **disabled** (stub implementation).

## Datadog Configuration

### Platform Files

| File | Platform |
|------|----------|
| `analytics/DatadogConfig.kt` | Common (expect/actual) |
| `analytics/DatadogConfig.android.kt` | Android implementation |
| `analytics/DatadogConfig.ios.kt` | iOS implementation |
| `analytics/DatadogConfig.desktop.kt` | Desktop (disabled stub) |

All paths relative to `composeApp/src/<platform>Main/kotlin/me/calebjones/spacelaunchnow/`.

### User Context Tracking

Datadog attaches the following user attributes to every session:

| Attribute | Source | Purpose |
|-----------|--------|---------|
| `originalAppUserId` | RevenueCat | Unique user identifier |
| `platform` | Platform detection | Android/iOS segmentation |
| `activeEntitlements` | RevenueCat | Subscription state (premium/free) |
| `firstSeenDate` | App install | Cohort analysis |
| `isLegacyPurchase` | Migration flag | Legacy user tracking |
| `subscriptionExpiry` | RevenueCat | Churn prediction |

### Secrets Required

| Secret | Purpose |
|--------|---------|
| `DATADOG_CLIENT_TOKEN` | API authentication |
| `DATADOG_APPLICATION_ID` | RUM app identifier |

Managed via `util/AppSecrets.kt` and CI/CD secrets.

## Firebase

### Services Used

| Service | Purpose |
|---------|---------|
| Firebase Cloud Messaging (FCM) | Push notifications for launch updates |
| Firebase Analytics | Event tracking |
| Firebase Distribution | Beta testing (Android releases) |

### Configuration

- `google-services.json` — Android config (gitignored, populated in CI/CD)
- Firebase BOM version: 34.3.0+

## Logging

### SpaceLogger

Custom logger (`util/logging/SpaceLogger.kt`) that bridges to Datadog:
- Structured log attributes
- Severity levels mapped to Datadog
- User context injection via `UserContext.kt`

### Key Log Events

| Event | Level | When |
|-------|-------|------|
| Purchase initiated | Info | User taps buy |
| Purchase completed | Info | Transaction confirmed |
| Purchase failed | Error | Transaction error |
| Restore purchases | Info | User restores |
| Entitlement changed | Info | Subscription state change |
| API call failed | Warning | Network/API errors |
| Widget update | Debug | Widget refresh cycle |

## Business Intelligence Opportunities

### Currently Tracked
- User session duration (Datadog RUM)
- Crash rates per version (Datadog/Crashlytics)
- Subscription state per user (RevenueCat + Datadog attributes)
- API response times (Datadog RUM)

### Potential Additions
- SupportUsScreen view → purchase conversion funnel
- Feature usage frequency per premium feature
- Widget engagement metrics
- Launch notification open rates
- Search/filter usage patterns
- Time spent on launch detail pages

## Key Documentation

| Doc | Path |
|-----|------|
| Datadog Restore Logging | `docs/billing/DATADOG_RESTORE_PURCHASES_LOGGING.md` |
| Datadog CI/CD Secrets | `docs/cicd/DATADOG_CICD_SECRETS.md` |
| Debug Tools | `docs/billing/REVENUECAT_DEBUG_TOOLS.md` |
