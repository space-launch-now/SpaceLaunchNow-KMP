# Feature Specification: Wear OS Premium Experience

**Feature Branch**: `013-wear-os-premium`  
**Created**: 2026-04-15  
**Status**: Draft  
**Input**: Add a phased Wear OS experience (complications → tiles → companion app) as a new premium feature gated behind a `WEAR_OS` RevenueCat entitlement. Data via direct API calls with phone DataLayer sync fallback.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Watch Complications Showing Next Launch (Priority: P1)

A premium subscriber with a Wear OS watch adds a complication to their watch face that shows a live countdown to the next upcoming launch. The complication updates automatically and shows the launch vehicle name and time remaining.

**Why this priority**: Complications are the lowest-effort, highest-utility Wear OS feature. They live on the watch face — the most-seen surface — and require no app navigation. This is the minimum viable Wear OS experience that delivers value.

**Independent Test**: Can be fully tested by adding the complication to a watch face on a Wear OS emulator and verifying countdown accuracy against the LL API. Delivers standalone value without tiles or companion app.

**Acceptance Scenarios**:

1. **Given** a premium subscriber with the watch app installed, **When** they add the "Next Launch" complication to their watch face, **Then** it displays the vehicle name and a countdown (e.g., "Falcon 9 — T-2h 15m")
2. **Given** a premium subscriber's complication is active, **When** the countdown reaches T-0, **Then** the complication updates to show the next upcoming launch
3. **Given** a free-tier user with the watch app installed, **When** they add the complication, **Then** it shows "Subscribe" placeholder text instead of launch data
4. **Given** the watch has no internet and no phone connection, **When** the complication needs to update, **Then** it shows the last cached launch data with no crash or error state

---

### User Story 2 - Glanceable Launch Tile (Priority: P2)

A premium subscriber swipes through their Wear OS tile carousel and sees a tile showing the next upcoming launch with agency icon, mission name, vehicle, countdown timer, and launch location.

**Why this priority**: Tiles provide richer information than complications without requiring users to open a full app. They're the natural next step after complications — still glanceable, but with more detail.

**Independent Test**: Can be tested by adding the tile to the tile carousel on a Wear OS emulator and verifying it displays correct launch information. Works independently from complications and companion app.

**Acceptance Scenarios**:

1. **Given** a premium subscriber, **When** they add the "Next Launch" tile to their carousel, **Then** it shows agency icon, mission name, vehicle name, countdown timer, and location
2. **Given** the tile is active and launch data changes on the phone, **When** the DataLayer syncs, **Then** the tile auto-refreshes with updated data
3. **Given** a free-tier user, **When** they view the tile, **Then** it shows "Upgrade on phone" with a deep link to the phone's subscription screen
4. **Given** the tile is displayed on a round watch face, **When** rendered, **Then** all content fits correctly within the circular display

---

### User Story 3 - Companion App: Browse Upcoming Launches (Priority: P3)

A premium subscriber opens the Wear OS app and sees a scrollable list of upcoming launches. They can tap any launch to see details including mission info, NET datetime, status, agency, and location. They can also tap "Open on phone" to see the full detail on their phone.

**Why this priority**: The companion app is the most feature-rich experience but also the highest effort. It depends on the data infrastructure built for complications and tiles, making it a natural Phase 3.

**Independent Test**: Can be tested end-to-end on Wear OS emulator by launching the app, scrolling through launches, tapping into a detail view, and verifying "Open on phone" triggers the phone app. Delivers full browsing experience independently.

**Acceptance Scenarios**:

1. **Given** a premium subscriber opens the watch app, **When** the launch list loads, **Then** it shows upcoming launches in a `ScalingLazyColumn` with agency abbreviation, vehicle name, countdown, and status chip
2. **Given** the user taps a launch in the list, **When** the detail screen opens, **Then** it shows mission name, NET datetime (via DateTimeUtil), status, agency, location, and a live countdown
3. **Given** the user taps "Open on phone" on a launch detail, **When** the phone is connected, **Then** the phone app opens to that launch's detail screen
4. **Given** a free-tier user opens the watch app, **When** the app starts, **Then** it shows a single premium gate screen with a "Subscribe on your phone" prompt and deep link
5. **Given** the watch has no internet, **When** the user opens the app, **Then** it shows cached launches with a "Last updated X ago" indicator
6. **Given** the user rotates the crown/bezel, **When** on the launch list, **Then** the list scrolls smoothly via rotary input

---

### User Story 4 - Entitlement Sync Between Phone and Watch (Priority: P1)

When a user subscribes or unsubscribes on their phone, their Wear OS watch reflects the change within seconds. The watch caches the entitlement state locally so it works even when disconnected from the phone.

**Why this priority**: This is a foundational requirement — every other user story depends on correct entitlement checking. Without reliable sync, premium users could be blocked and free users could get unauthorized access.

**Independent Test**: Can be tested by toggling premium status on phone (via RevenueCat sandbox) and verifying the watch complication/tile/app updates within seconds. Also test by disconnecting phone and verifying cached state persists.

**Acceptance Scenarios**:

1. **Given** a user subscribes on phone, **When** the DataLayer syncs, **Then** the watch enables all Wear OS premium features within 10 seconds
2. **Given** a user's subscription expires, **When** the DataLayer syncs, **Then** the watch disables all Wear OS premium features and shows free-tier states
3. **Given** the watch is disconnected from the phone, **When** the user checks a premium feature, **Then** the cached entitlement state is used (last known state)
4. **Given** the watch has never been connected to the phone, **When** the user installs the watch app, **Then** it defaults to free-tier until first sync

---

### User Story 5 - Data Sync with Fallback (Priority: P1)

The watch fetches launch data via direct API calls when on WiFi/LTE, falls back to phone DataLayer sync when disconnected from the internet, and uses local cache when fully offline.

**Why this priority**: This is infrastructure — complications, tiles, and the companion app all need reliable data. The three-tier fallback ensures the watch always has something to show.

**Independent Test**: Can be tested by: (1) enabling watch WiFi and verifying direct API data, (2) disabling watch WiFi with phone nearby and verifying DataLayer sync, (3) disconnecting both and verifying cached data is shown.

**Acceptance Scenarios**:

1. **Given** the watch has WiFi/LTE, **When** data is needed, **Then** it fetches directly from the Launch Library API using the generated client
2. **Given** the watch has no internet but phone is nearby, **When** data is needed, **Then** it reads from the phone's DataLayer sync
3. **Given** the watch has no internet and no phone connection, **When** data is needed, **Then** it shows the last cached data from local DataStore
4. **Given** a background WorkManager job runs every 30 minutes, **When** the watch has connectivity, **Then** it refreshes the cache silently

---

### Edge Cases

- What happens when the phone app is not installed but the watch app is? → Watch works standalone via direct API, shows "Install phone app for best experience" prompt
- What happens when the user has a Legacy entitlement (not PREMIUM)? → Legacy entitlements do NOT grant WEAR_OS — only PREMIUM and LIFETIME tiers include watch access
- What happens when the LL API returns an error? → Show cached data if available, show "Unable to load" with retry button if no cache
- What happens when multiple launches are happening simultaneously? → Show the one with the earliest NET that hasn't completed yet
- What happens when the watch battery is critically low? → WorkManager respects battery constraints, background sync skipped

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST add a `WEAR_OS` entry to the `PremiumFeature` enum, granted by PREMIUM and LIFETIME subscription tiers only
- **FR-002**: System MUST create a `wearApp` Gradle module with `com.android.application` plugin targeting Wear OS 3+ (API 30+)
- **FR-003**: System MUST implement `NextLaunchComplicationService` providing short text (countdown), long text (vehicle + countdown), and ranged value (progress) complication types
- **FR-004**: System MUST implement a `NextLaunchTile` using Horologist TileRenderer with agency icon, mission name, vehicle, countdown, and location
- **FR-005**: System MUST implement a companion app with launch list (`ScalingLazyColumn`), launch detail, and settings screens using Wear Navigation (SwipeDismissableNavHost)
- **FR-006**: System MUST sync entitlement state from phone to watch via Wearable DataLayer API, with local DataStore caching for offline access
- **FR-007**: System MUST implement three-tier data fetching: direct API → DataLayer phone sync → local cache fallback
- **FR-008**: System MUST gate all Wear OS features behind `PremiumFeature.WEAR_OS` — free users see subscribe/upgrade prompts
- **FR-009**: System MUST use consistent title formatting via `LaunchFormatUtil.formatLaunchTitle()` on all watch surfaces
- **FR-010**: System MUST use `DateTimeUtil` for all datetime display on watch to support UTC toggle
- **FR-011**: System MUST schedule background data refresh via WorkManager (every 30 minutes) respecting battery constraints
- **FR-012**: System MUST support rotary scroll input on the companion app launch list
- **FR-013**: System MUST implement "Open on phone" functionality via `RemoteActivityHelper` for launch detail deep linking
- **FR-014**: System MUST bundle the Wear OS APK with the phone app in a single Play Store listing (app bundle)

### Key Entities

- **WearEntitlementState**: Cached premium state on watch (hasWearOs: Boolean, lastSyncTimestamp: Instant, source: PHONE_SYNC | LOCAL_CACHE)
- **WatchLaunchCache**: Local DataStore on watch storing last N launches (LaunchBasic list, lastUpdated: Instant)
- **DataLayerSyncPayload**: Data synced from phone → watch (launches: List<LaunchBasic>, entitlementActive: Boolean, syncTimestamp: Instant)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Premium subscribers can add a working launch countdown complication within 30 seconds of watch app install
- **SC-002**: Complication countdown accuracy is within 1 second of actual NET time
- **SC-003**: Entitlement state syncs from phone to watch within 10 seconds of subscription change
- **SC-004**: Watch shows cached data within 500ms when offline (no loading spinner for cached content)
- **SC-005**: Free-tier users are clearly prompted to subscribe with a working deep link to phone paywall
- **SC-006**: Companion app launch list renders at 60fps with rotary scroll on Galaxy Watch 5+
- **SC-007**: Background WorkManager refresh does not cause noticeable battery drain (< 1% per day)
- **SC-008**: Watch features contribute to measurable increase in premium conversion rate (target: +0.5% over baseline 0.14%)
