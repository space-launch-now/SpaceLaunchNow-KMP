# Feature Spec: Live Composable Onboarding

**Branch**: `007-live-onboarding` | **Priority**: P1 (High)  
**Origin**: [Reddit r/androiddev concept](https://www.reddit.com/r/androiddev/comments/1rticy4/using_jetpack_compose_previews_as_live_onboarding/) — Live composable previews for onboarding

## Problem Statement

The existing `OnboardingScreen` (006-onboarding-paywall) functions as a paywall/subscription conversion surface. There is no dedicated onboarding flow that introduces users to the app's core features — launch cards, the schedule screen, and notification filters. Users arrive at the paywall without understanding what the app offers, reducing engagement and conversion.

Static screenshots for onboarding screens go stale whenever the UI changes. A more robust approach renders the actual production composables live within the onboarding flow, ensuring previews always reflect the current UI, respond to device size, and require zero maintenance.

## Goal

Create a **multi-page onboarding carousel** that uses **live composable previews** embedded inside platform-specific device frames. Each page showcases a real app feature by rendering the actual composable used in production (with mock data). The final page requests notification permission.

The existing `OnboardingScreen` should be renamed to `OnboardingPaywallScreen` to clarify its purpose. The new `LiveOnboardingScreen` becomes the first-run experience (before the paywall).

**Key Innovation**: Instead of static screenshots, each onboarding page renders the real composable (e.g., `LaunchCardHeaderOverlay`, `ScheduleContent`, notification filters) inside a composable device frame. When the UI changes in production, the onboarding previews update automatically.

## User Stories

### US-1: New User Sees Live Onboarding Carousel

**As a** new user installing SpaceLaunchNow for the first time,  
**I want to** see a multi-page onboarding carousel showcasing the app's key features with live UI previews,  
**So that** I understand what the app offers before I start using it.

**Acceptance Criteria:**
- Onboarding carousel has 4 pages: Launch Card, Schedule, Notification Filters, Enable Notifications
- Each feature page displays the actual composable component inside a device frame
- Device frame is platform-specific (Android phone frame on Android, iPhone frame on iOS)
- Device frame shows a live clock in the status bar area
- User can swipe between pages or tap full-width "Next" button at the bottom
- User can tap "Skip" text button (top-right corner) to bypass remaining pages
- Wavy-line progress bar shown between content and "Next" button — a straight track line with a sine-wave progress line drawn over it indicating how far through the onboarding the user is

**Visual Layout (per page, top-to-bottom):**
- Full-bleed space-themed background image behind everything
- "Skip" text button pinned top-right
- Device frame centered, occupying ~50-60% of screen height
  - Dark bezel with rounded corners (platform-specific shape)
  - Status bar inside frame with live clock
  - Live composable content rendered inside the screen area
- Bold title text below the device frame
- Lighter subtitle/description text below the title
- Wavy-line progress bar (straight track + animated sine-wave fill)
- Full-width "Next" button (accent/primary color) at bottom

### US-2: Launch Card Preview Page

**As a** new user,  
**I want to** see a live preview of a launch card on the first onboarding page,  
**So that** I understand the app shows detailed launch information.

**Acceptance Criteria:**
- Page displays a `LaunchCardHeaderOverlay` composable with mock launch data inside a device frame
- Title text: "Track Every Launch" (or similar)
- Subtitle describes the launch tracking feature
- The composable renders responsively within the device frame

### US-3: Schedule Screen Preview Page

**As a** new user,  
**I want to** see a live preview of the schedule screen on the second onboarding page,  
**So that** I understand I can browse upcoming and previous launches.

**Acceptance Criteria:**
- Page displays a scaled-down `ScheduleContent` composable with mock data inside a device frame
- Title text: "Your Launch Schedule" (or similar)
- Subtitle describes the schedule browsing feature

### US-4: Notification Filters Preview Page

**As a** new user,  
**I want to** see a live preview of the notification filter options on the third page,  
**So that** I understand I can customize which launches I'm notified about.

**Acceptance Criteria:**
- Page displays notification filter UI with mock filter data inside a device frame
- Title text: "Customize Notifications" (or similar)
- Subtitle describes the filtering feature

### US-5: Notification Permission Request Page

**As a** new user,  
**I want to** be prompted to enable notifications on the final onboarding page,  
**So that** I can receive launch alerts without navigating to settings later.

**Acceptance Criteria:**
- Page shows a notification-themed illustration or icon (no device frame needed)
- Title text: "Never Miss a Launch" (or similar)
- Prominent "Enable Notifications" button triggers platform notification permission request
- "Maybe Later" / "Skip" option allows proceeding without granting permission
- On Android 13+: triggers `POST_NOTIFICATIONS` runtime permission
- On iOS: triggers `UNUserNotificationCenter.requestAuthorization`
- On Desktop: no-op (skip or auto-advance)
- After permission response (granted or denied), proceeds to next screen (paywall or home)

### US-6: Onboarding Flow Sequencing

**As a** new user,  
**I want** the live onboarding to appear before the paywall,  
**So that** I understand the app's value before being asked to subscribe.

**Acceptance Criteria:**
- Flow order: Live Onboarding → Onboarding Paywall → Home
- New DataStore flag: `liveOnboardingCompleted`
- If `liveOnboardingCompleted == false` and user is new: show LiveOnboarding first
- After live onboarding completes, navigate to existing OnboardingPaywall (if not yet shown)
- Returning users skip both screens

## Non-Functional Requirements

- **Visual Design** (matching ClashMarket reference):
  - Full-bleed background image — space-themed (rocket launch, nebula, or starfield)
  - Device frame rendered as a dark bezel composable with platform-specific shape
  - Android: rounded rectangle bezel with pill-shaped camera cutout at top-center
  - iOS: rounded rectangle bezel with Dynamic Island notch at top-center
  - Desktop: generic rounded rectangle bezel
  - Status bar inside frame shows live clock (updates every minute)
  - Title text: large, bold, high contrast over background
  - Subtitle text: lighter weight, slightly lower contrast
  - "Skip" button: text-only, top-right corner, over the background image
  - "Next" button: full-width, accent/primary color, bottom of screen
  - Wavy-line progress bar: horizontal, between subtitle and "Next" button — straight baseline track with a sine-wave line drawn over the filled portion, animated on page change
- **Live Previews**: Composables rendered inside device frames must use the same components as production, fed with mock/preview data
- **Platform Device Frames**: Single `DeviceFrame` composable in `commonMain` with runtime platform detection (no expect/actual — purely visual)
- **Performance**: Device frame + embedded composable must not add >100ms to page render
- **Accessibility**: All pages must have content descriptions; support screen readers
- **Dual Previews**: All new composables must have light and dark `@Preview` annotations
- **Responsive**: Device frame scales appropriately on phones vs tablets
- **No Network Calls**: All preview data is mock/static — no API calls during onboarding

## Out of Scope

- Animated transitions between composable states inside the device frame
- Recording or screenshotting composable output
- A/B testing different onboarding page orders
- Paywall modifications (handled by 006-onboarding-paywall)

## Technical Approach

1. **Rename** existing `OnboardingScreen` → `OnboardingPaywallScreen` (and update all references)
2. **Add** `LiveOnboarding` route to `Screen.kt`
3. **Add** `LIVE_ONBOARDING_COMPLETED` preference flag to `AppPreferences`
4. **Create** `DeviceFrame` composable in `commonMain` with runtime platform detection (via `getPlatform()`):
   - `DeviceFrameStyle.Android`: rounded rectangle bezel, pill camera cutout, status bar with live clock
   - `DeviceFrameStyle.IPhone`: rounded rectangle bezel, Dynamic Island cutout, status bar with live clock
   - `DeviceFrameStyle.Generic`: simple rounded rectangle bezel, status bar with live clock
5. **Create** `LiveOnboardingScreen` composable — horizontal pager with 4 pages, full-bleed space-themed background image, "Skip" top-right, wavy-line progress bar, full-width "Next" button
6. **Create** `OnboardingPage` composable — centered device frame + bold title + subtitle layout
7. **Create** mock data providers using existing `PreviewData` for launch card, schedule, and notification filters
8. **Integrate** notification permission request using existing `requestPlatformNotificationPermission()` expect/actual
9. **Update** `App.kt` navigation to sequence: LiveOnboarding → OnboardingPaywall → Home
10. **Update** `startDestination` logic to check `liveOnboardingCompleted` flag
11. **Add** a space-themed background image to `commonMain/composeResources/drawable/` (or use a gradient fallback)
