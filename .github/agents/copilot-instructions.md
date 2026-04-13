# SpaceLaunchNow-KMP-Main Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-01-21

## Active Technologies
- N/A (UI refactor only) (main)
- [e.g., Python 3.11, Swift 5.9, Rust 1.75 or NEEDS CLARIFICATION] + [e.g., FastAPI, UIKit, LLVM or NEEDS CLARIFICATION] (feature/fix_notifications)
- [if applicable, e.g., PostgreSQL, CoreData, files or N/A] (feature/fix_notifications)
- Kotlin 2.0.21, Swift 5.9, Java 21 + Firebase Messaging (Android), Firebase iOS SDK, Compose Multiplatform, Koin DI, WorkManager (dev)
- DataStore (Android), UserDefaults + App Groups (iOS), SQLite (notification history) (dev)
- Kotlin 2.1.0 (Kotlin Multiplatform) + Kotlinx Serialization, Firebase Cloud Messaging, Koin DI (fix_notif_filters)
- DataStore (key-value persistence for NotificationState) (fix_notif_filters)
- Swift 5.9+ (iOS widget extension)
 + `CryptoKit` (Apple system framework, iOS 13+), `WidgetKit`, `SwiftUI`
 (003-fix-widget-image-mismatch)
- Disk-based image cache in app group container (`group.me.calebjones.spacelaunchnow/widget_image_cache/`)
 (003-fix-widget-image-mismatch)
- Kotlin 2.0+ with Compose Multiplatform + Compose Material3, Compose Foundation (HorizontalPager), Koin DI (main)
- Kotlin 2.1.0 (KMP), Java 21 + Compose Multiplatform, RevenueCat SDK (via `SubscriptionViewModel`), Koin DI, DataStore Preferences (006-onboarding-paywall)
- DataStore Preferences (`AppPreferences`) — new `ONBOARDING_PAYWALL_SHOWN` boolean key (006-onboarding-paywall)
- Kotlin 2.0+ (KMP), Java 21 (JetBrains JDK) + Ktor (HTTP client), Koin (DI), Jetpack Compose Multiplatform, kotlinx.serialization (012-reduce-api-calls-caching)
- SQLDelight (persistent cache), in-memory `LaunchCache`, JSON blob serialization (012-reduce-api-calls-caching)

Please check the project [consitution](.specify/memory/consitution.md) for architectural principles and guidelines.

## Project Structure

```text
src/
tests/
```

## Commands

# Add commands for Kotlin 2.0+ with Compose Multiplatform

## Code Style

Kotlin 2.0+ with Compose Multiplatform: Follow standard conventions

## Recent Changes
- 012-reduce-api-calls-caching: Added Kotlin 2.0+ (KMP), Java 21 (JetBrains JDK) + Ktor (HTTP client), Koin (DI), Jetpack Compose Multiplatform, kotlinx.serialization
- 006-onboarding-paywall: Added Kotlin 2.1.0 (KMP), Java 21 + Compose Multiplatform, RevenueCat SDK (via `SubscriptionViewModel`), Koin DI, DataStore Preferences
- 005-fix-stale-filtered-data: Added Kotlin 2.1.x (KMP), Swift 5.x (iOS widgets) + Compose Multiplatform, Ktor, Koin, Glance (Android widgets), WidgetKit (iOS)
