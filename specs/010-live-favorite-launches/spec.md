# Feature Specification: Live Favorite Launches Card

**Feature Branch**: `010-live-favorite-launches`  
**Created**: 2026-04-01  
**Status**: Draft  
**Input**: User description: "If there is a launch in flight that matches a user's favorites I want to show that on the home page in a special colored card with a Live glowing button or something peeking down from the top. Currently as soon as NET passes the only way to find a launch is to go to Previous."

## Problem Statement

When a rocket launches (NET time passes), it immediately disappears from the "Next Up" / featured launch card on the home page. Users who are interested in a launch that is currently **in flight** (status ID = 6) have no easy way to find it without navigating to the "Previous" launches section. This is a poor UX, especially for users who have set up filters for their favorite agencies/locations and want to watch an active launch.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Live Launch Card (Priority: P1)

As a user who has favorited a launch provider (e.g., SpaceX), I want to see a prominent "LIVE" card on my home page when one of their launches is currently in flight, so I can quickly access the launch details and webcast.

**Why this priority**: This is the core feature request. Users need to see in-flight launches prominently displayed rather than having them disappear after NET.

**Independent Test**: Can be tested by simulating or waiting for an in-flight launch matching user's filter settings. Card should appear at top of home screen with distinctive visual treatment.

**Acceptance Scenarios**:

1. **Given** user has SpaceX (ID: 121) in their subscribed agencies and a Falcon 9 launch is in flight,  
   **When** the home page loads,  
   **Then** a "LIVE" card appears prominently showing the in-flight launch with distinctive visual styling

2. **Given** user has no filters enabled (followAllLaunches = true) and any launch is in flight,  
   **When** the home page loads,  
   **Then** the LIVE card shows the in-flight launch

3. **Given** a launch is in flight but does NOT match user's configured filters,  
   **When** the home page loads,  
   **Then** no LIVE card is shown (respects user preferences)

---

### User Story 2 - LIVE Card Visual Distinction (Priority: P1)

As a user, I want the LIVE launch card to have a visually distinct appearance (special color, glowing/pulsing indicator) so I can immediately recognize that a launch is happening right now.

**Why this priority**: Visual distinction is essential to make the LIVE card noticeable and communicate urgency.

**Independent Test**: Visual inspection of LIVE card vs regular "Next Up" card - should be immediately distinguishable.

**Acceptance Scenarios**:

1. **Given** a LIVE launch card is displayed,  
   **When** I view the home page,  
   **Then** the card has a distinct border color (Blue 500 / In Flight color) or accent

2. **Given** a LIVE launch card is displayed,  
   **When** I view the home page,  
   **Then** a "LIVE" badge/pill is visible with animation or glow effect

3. **Given** a LIVE launch card is displayed,  
   **When** the card is rendered,  
   **Then** the visual treatment is accessible (sufficient contrast, not relying solely on color)

---

### User Story 3 - Navigate to Live Launch Details (Priority: P1)

As a user viewing a LIVE launch card, I want to tap it and navigate to the launch detail page so I can see more information and access the webcast.

**Why this priority**: Users need to take action on the LIVE card by viewing details or watching the stream.

**Independent Test**: Tap LIVE card and verify navigation to launch detail page with correct launch ID.

**Acceptance Scenarios**:

1. **Given** a LIVE launch card is displayed,  
   **When** I tap the card,  
   **Then** I navigate to the launch detail page for that launch

---

### User Story 4 - LIVE Card Position on Home Page (Priority: P2)

As a user, I want the LIVE card to appear at the very top of the home page (above the regular "Next Up" card) so I don't miss it.

**Why this priority**: Position determines visibility. Top placement ensures users see active launches immediately.

**Independent Test**: With a LIVE launch active, scroll position should default to top and LIVE card should be first visible element.

**Acceptance Scenarios**:

1. **Given** a launch is in flight matching my filters,  
   **When** I view the home page,  
   **Then** the LIVE card appears at the top, before the regular "Next Up" featured launch card

2. **Given** no launches are in flight,  
   **When** I view the home page,  
   **Then** the regular "Next Up" featured launch card is displayed normally without a LIVE section

---

### User Story 5 - Multiple In-Flight Launches (Priority: P3)

As a user, when multiple launches are in flight simultaneously, I want to see the most relevant one (matching my filters) or all of them if practical.

**Why this priority**: Edge case - multiple simultaneous in-flight launches are rare but possible.

**Independent Test**: Simulate two in-flight launches, verify at least one is shown.

**Acceptance Scenarios**:

1. **Given** two launches are in flight and both match my filters,  
   **When** I view the home page,  
   **Then** at least one LIVE card is shown (implementation may show first match or allow scrolling through multiple)

---

### Edge Cases

- What happens when the launch status changes from "In Flight" (6) to "Success" (3) or "Failure" (4)? → LIVE card should disappear or transition gracefully
- What happens if the API returns no in-flight launches? → No LIVE section shown, regular home page displayed
- What happens with slow network / offline mode? → Show cached in-flight launch if available, or show empty state
- How long should a launch be considered "live"? → Use API status; as long as status=6, show LIVE card

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST query API for launches with status_ids=6 (In Flight) on home page load
- **FR-002**: System MUST filter in-flight launches by user's subscribed agencies/locations if filtering is enabled
- **FR-003**: System MUST display a distinct "LIVE" card when an in-flight launch matches user filters
- **FR-004**: System MUST position the LIVE card at the top of the home page, above the featured launch
- **FR-005**: LIVE card MUST navigate to launch detail page when tapped
- **FR-006**: LIVE card MUST display "LIVE" indicator with visual animation/glow effect
- **FR-007**: LIVE card MUST use the In Flight status color (Blue 500 / #1976D2) for visual distinction
- **FR-008**: System MUST respect user filter settings (followAllLaunches, subscribedAgencies, subscribedLocations)
- **FR-009**: System MUST handle transition gracefully when launch status changes from In Flight to another status
- **FR-010**: System MUST implement Stale-While-Revalidate caching pattern for in-flight launch data

### Non-Functional Requirements

- **NFR-001**: LIVE card animation MUST not impact battery life significantly (use efficient animation)
- **NFR-002**: API call for in-flight launches MUST not block initial home page render (parallel loading)
- **NFR-003**: LIVE indicator MUST be accessible (sufficient contrast, alternative text)

### Key Entities

- **LaunchNormal**: Existing launch entity with `status.id` field (status 6 = In Flight)
- **InFlightLaunchState**: New ViewState for tracking in-flight launch loading/data/error
- **LiveIndicator**: New UI component for the animated LIVE badge

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can see in-flight launches on home page within 2 seconds of page load
- **SC-002**: LIVE card is visually distinguishable from regular launch cards (user testing confirms)
- **SC-003**: Tapping LIVE card successfully navigates to launch details 100% of the time
- **SC-004**: LIVE card respects user filter settings (verified through automated tests)
- **SC-005**: No performance regression on home page load time (< 500ms additional delay)

## Technical Notes

### API Status Values Reference

| Status ID | Name | Color |
|-----------|------|-------|
| 1 | GO | Green 600 |
| 2 | TBD | Red 500 |
| 3 | Success | Green 800 |
| 4 | Failure | Red 700 |
| 5 | Hold | Orange 500 |
| **6** | **In Flight** | **Blue 500** |
| 7 | Partial Failure | Blue Grey 500 |
| 8 | Partial Failure | Blue Grey 800 |

### API Query Strategy

Use the existing LaunchesApi with `status__ids=6` filter parameter via extension functions:

```kotlin
launchesApi.getLaunchList(
    statusIds = listOf(6),  // In Flight only
    lspIds = filterParams.agencyIds,
    locationIds = filterParams.locationIds,
    limit = 5
)
```

### Related Files (Current Codebase)

- `NextUpView.kt` - Current featured launch card component
- `HomeViewModel.kt` - Home screen state management
- `LaunchRepository.kt` / `LaunchRepositoryImpl.kt` - Data layer
- `StatusColorUtil.kt` - Status color mappings
- `LaunchFilterService.kt` - Filter parameter generation
- `NotificationState.kt` - User filter preferences
