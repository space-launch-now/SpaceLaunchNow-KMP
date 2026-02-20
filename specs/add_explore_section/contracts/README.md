# API Contracts: Explore Tab Navigation

**Feature**: Explore tab with navigation to discovery sections  
**Date**: February 8, 2026

## Overview

**No API contracts are required for this feature.**

This is a pure frontend UI/navigation feature that does not:
- Make new API requests
- Modify existing API endpoints
- Change API request/response formats
- Introduce new backend dependencies

---

## Rationale

### Feature Scope

The Explore tab feature:
1. Adds a new tab to bottom navigation UI
2. Creates a screen displaying cards for 5 existing sections
3. Navigates to existing screens using existing navigation routes

### Existing API Integration

All destination screens already have working API integrations:
- **ISS Tracking** → Uses `SpaceStationDetail(4)` route with existing `IssTrackingRepository`
- **Agencies** → Uses `Agencies` route with existing `AgencyRepository`
- **Astronauts** → Uses `Astronauts` route with existing `AstronautRepository`
- **Rockets** → Uses `Rockets` route with existing `RocketRepository`
- **Starship** → Uses `Starship` route with existing `StarshipRepository`

### No New API Calls

The ExploreScreen itself does not fetch any data:
- Section list is static (hardcoded in `ExploreSections.sections`)
- Icons are from Material Icons (local assets)
- Text content is hardcoded strings
- Navigation is handled by Compose Navigation (local state)

---

## Navigation Contract (Internal)

While not an API contract, here's the internal navigation contract:

### NavHostController Integration

```kotlin
// Required navigation route registration in App.kt
composableWithCompositionLocal<Explore> {
    ExploreScreen(navController = navController)
}

// Navigation action from ExploreScreen to sections
navController.navigate(section.route) // where section.route is @Serializable object
```

**Contract Properties**:
- `navController`: NavHostController - Provided by parent layout
- `section.route`: @Serializable object - Must be registered in NavHost
- Navigation behavior: Preserves state, allows back navigation

**Validation**:
- All destination routes pre-exist and are registered
- ExploreScreen does not create new navigation patterns
- Follows existing bottom nav navigation contract

---

## Summary

**API Contracts**: None - No backend interaction  
**Internal Contracts**: Standard Compose Navigation patterns  
**Testing Focus**: UI navigation tests, not API integration tests  

For API contracts of destination screens (Rockets, Agencies, etc.), refer to their respective specification documents.
