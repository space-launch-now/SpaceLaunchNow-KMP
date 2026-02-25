# Feature Specification: Astronaut Views

**Feature Name**: Astronaut List and Detail Views
**Target Platform**: Android (primary), iOS, Desktop (KMP)  
**Priority**: Medium - Enhances "Explore" section functionality  
**Created**: 2026-01-29

## Overview

Add comprehensive astronaut browsing and viewing capabilities to SpaceLaunchNow. Users will be able to:
1. Browse a list of astronauts from Settings → Explore section
2. View detailed information about each astronaut
3. See astronaut profile cards on Launch Detail views (for crewed missions)

## User Stories

### US-1: Browse Astronauts
**As a** space enthusiast  
**I want to** browse a list of astronauts and cosmonauts  
**So that** I can discover and learn about space explorers

**Acceptance Criteria**:
- User can navigate to astronaut list from Settings → Explore (WIP) section
- List displays astronaut cards with profile image, name, agency, and status
- List supports pagination with infinite scrolling
- List shows loading, error, and empty states appropriately
- Tapping an astronaut navigates to detail view

### US-2: View Astronaut Details
**As a** space enthusiast  
**I want to** view detailed information about an astronaut  
**So that** I can learn about their career, missions, and achievements

**Acceptance Criteria**:
- Detail view shows comprehensive astronaut information including:
  - Profile image
  - Full name
  - Agency affiliation
  - Status (active, retired, etc.)
  - Biography
  - Nationality
  - Date of birth and age
  - Career statistics (flights, time in space, EVA time)
  - Mission history
- Detail view uses SharedDetailScaffold pattern for consistency
- Back navigation returns to previous screen

### US-3: See Crew on Launch Details
**As a** user viewing a launch  
**I want to** see astronaut profiles for crewed missions  
**So that** I can quickly identify who is flying

**Acceptance Criteria**:
- Launch Detail view displays astronaut profile cards for spacecraft with crew
- Astronaut cards are horizontal, compact format with avatar image
- Cards show astronaut name, role, and agency
- Tapping astronaut card navigates to astronaut detail view
- Cards appear in Spacecraft Details section or dedicated Crew section

## Functional Requirements

### FR-1: Astronaut List Screen
- Display paginated list of astronauts from Launch Library API
- Use `AstronautsApi.astronautsList()` endpoint (via extension function)
- Show 20 astronauts per page with lazy loading
- Filter capabilities: by agency, status, nationality
- Search functionality by name
- Card displays:
  - Circular avatar image (with fallback)
  - Name
  - Agency abbreviation
  - Status badge

### FR-2: Astronaut Detail Screen
- Fetch astronaut details using `AstronautsApi.astronautsRetrieve(id)` endpoint
- Display using `SharedDetailScaffold` component
- Sections:
  - Header: Name, agency, image
  - Quick Stats: Flights, time in space, EVA time
  - Biography section (expandable)
  - Career Information
  - Flight History (list of missions with dates)
  - Related launches (if applicable)

### FR-3: Astronaut Profile Card Component
- Reusable horizontal card component
- Displays on Launch Detail screens (Spacecraft tab)
- Compact layout:
  - Small circular avatar (48dp)
  - Name (primary text)
  - Role (secondary text)
  - Agency badge/logo
- Clickable → navigates to astronaut detail

### FR-4: Navigation Integration
- Add `Astronauts` route to `Screen.kt`
- Add `AstronautDetail(astronautId: Int)` route
- Add navigation link in Settings → Explore section
- Handle deep linking to astronaut details

## Technical Requirements

### TR-1: API Integration
- Use generated `AstronautsApi` from OpenAPI
- Create extension functions for clean parameter interfaces:
  - `AstronautsApi.getAstronautList(limit, offset, search, statusIds, agencyIds)`
  - `AstronautsApi.getAstronautDetail(id)`
- Use `AstronautEndpointNormal` for list items
- Use `AstronautEndpointDetailed` for detail view

### TR-2: Repository Pattern
```kotlin
interface AstronautRepository {
    suspend fun getAstronauts(limit: Int, offset: Int): Result<PaginatedAstronautEndpointNormalList>
    suspend fun getAstronautDetail(id: Int): Result<AstronautEndpointDetailed>
    suspend fun searchAstronauts(query: String): Result<PaginatedAstronautEndpointNormalList>
}
```

### TR-3: ViewModel Architecture
```kotlin
// AstronautListViewModel
data class AstronautListUiState(
    val astronauts: List<AstronautEndpointNormal>,
    val isLoading: Boolean,
    val error: String?,
    val hasMore: Boolean
)

// AstronautDetailViewModel
data class AstronautDetailUiState(
    val astronaut: AstronautEndpointDetailed?,
    val isLoading: Boolean,
    val error: String?
)
```

### TR-4: UI Components Structure
```
ui/
└── astronaut/
    ├── AstronautListScreen.kt        # Main list view
    ├── AstronautDetailView.kt        # Detail view
    └── components/
        ├── AstronautCard.kt          # List item card
        ├── AstronautProfileCard.kt   # Horizontal profile for launch detail
        ├── AstronautInfoCard.kt      # Biography/info section
        └── AstronautStatsCard.kt     # Career statistics
```

## Non-Functional Requirements

### NFR-1: Performance
- List should load initial 20 items in < 2 seconds
- Smooth scrolling with pagination
- Image loading with Coil caching
- Offline support via caching (future enhancement)

### NFR-2: Accessibility
- All images have content descriptions
- Text is readable at all zoom levels
- Touch targets minimum 48dp
- Semantic labels for screen readers

### NFR-3: Design Consistency
- Follow existing Material Design 3 patterns
- Use SharedDetailScaffold for detail views
- Match color scheme and typography of other detail screens
- Reuse existing components (InfoTile, StatusChip, etc.)

### NFR-4: Testing
- Unit tests for ViewModels
- Integration tests for repository
- UI tests for critical flows (navigation, loading states)

## UI/UX Design

### List Screen Layout
```
┌─────────────────────────────┐
│ ← Astronauts        🔍     │ TopAppBar
├─────────────────────────────┤
│ ┌─────────────────────────┐ │
│ │ 👤 Neil Armstrong       │ │ Astronaut Card
│ │    NASA • Retired       │ │
│ └─────────────────────────┘ │
│ ┌─────────────────────────┐ │
│ │ 👤 Yuri Gagarin         │ │
│ │    Roscosmos • Retired  │ │
│ └─────────────────────────┘ │
│ ...                         │
│ [Loading More...]           │
└─────────────────────────────┘
```

### Detail Screen Layout
```
┌─────────────────────────────┐
│ ←                           │ Header with image background
│                             │
│   Neil Armstrong            │ Name overlay
│   NASA                      │ Agency
└─────────────────────────────┘
│ ┌───────────────────────┐   │
│ │ 🚀 Flights: 2         │   │ Quick Stats Grid
│ │ ⏱️ Time: 8d 14h       │   │
│ └───────────────────────┘   │
│ Biography                   │ Expandable section
│ [Biography text...]         │
│ Career Information          │
│ [Career details...]         │
│ Flight History              │
│ [Mission cards...]          │
└─────────────────────────────┘
```

### Astronaut Profile Card (for Launch Detail)
```
┌─────────────────────────────────┐
│ 👤 Neil Armstrong               │
│    Commander • NASA             │
└─────────────────────────────────┘
```

## Dependencies

### Existing Components to Reuse
- `SharedDetailScaffold` - For detail view structure
- `InfoTile` - For statistics display
- `StatusChip` - For astronaut status badges
- `AgencyChip` - For agency badges
- Coil `AsyncImage` - For image loading

### New Dependencies
- None required (all API models already generated)

## Implementation Order

### Phase 1: Foundation (Priority 1)
1. Create navigation routes in `Screen.kt`
2. Create repository interface and implementation
3. Create extension functions for AstronautsApi
4. Create ViewModels with basic state management

### Phase 2: List View (Priority 1)
5. Implement AstronautListScreen with basic list
6. Create AstronautCard component
7. Add pagination logic
8. Integrate with navigation

### Phase 3: Detail View (Priority 2)
9. Implement AstronautDetailView using SharedDetailScaffold
10. Create AstronautInfoCard and AstronautStatsCard components
11. Add mission history display
12. Connect to navigation

### Phase 4: Integration (Priority 2)
13. Create AstronautProfileCard component (horizontal)
14. Integrate into Launch Detail view (Spacecraft tab)
15. Add click handling to navigate to detail view
16. Update Settings screen with astronaut list link

### Phase 5: Polish (Priority 3)
17. Add loading states and error handling
18. Implement image fallbacks
19. Add search functionality (future)
20. Write tests

## Success Metrics

- User can navigate to astronaut list from Settings
- List displays astronauts with proper pagination
- Detail view shows comprehensive information
- Astronaut cards appear on crewed launches
- No crashes or errors in astronaut flows
- All screens follow existing design patterns

## Future Enhancements

- Search and filter astronauts by various criteria
- Favorite astronauts for quick access
- Notifications for astronaut-related news
- Astronaut comparison feature
- Integration with event system (spacewalks, etc.)
- Offline caching and sync

## References

- Launch Library API 2.4.0: `/astronauts/` endpoints
- Existing Detail Screens: RocketDetailView, AgencyDetailView, EventDetailView
- Existing List Patterns: RocketsListScreen, AgenciesListScreen
- Crew Display: EventDetailView.kt AstronautsCard, SpacecraftDetailsCard crew sections
