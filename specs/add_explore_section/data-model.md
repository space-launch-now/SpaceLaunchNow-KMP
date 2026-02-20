# Data Model: Explore Tab Navigation

**Feature**: Explore tab with navigation to discovery sections  
**Date**: February 8, 2026

## Overview

This feature is primarily UI/navigation-focused with no backend data persistence. The "data model" consists of navigation state, UI component structures, and static section definitions.

---

## 1. Navigation Data Models

### Explore Navigation Object

```kotlin
// File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/navigation/Screen.kt

// Add to existing @Serializable routes
@Serializable
data object Explore

// Add to existing Screen sealed class for UI metadata
sealed class Screen(val label: String, val icon: ImageVector) {
    data object Home : Screen("Home", Icons.Filled.Home)
    data object Schedule : Screen("Schedule", Icons.AutoMirrored.Filled.List)
    data object Explore : Screen("Explore", Icons.Filled.Explore)  // NEW
    data object Settings : Screen("Settings", Icons.Filled.Settings)
}
```

**Properties**:
- `label`: String - Display name in bottom navigation ("Explore")
- `icon`: ImageVector - Material icon for tab (Icons.Filled.Explore)

**Relationships**:
- Navigates to: ExploreScreen (composable)
- Part of: Bottom navigation tab list
- Reuses: Existing navigation infrastructure (NavHostController, type-safe routes)

**Validation Rules**:
- MUST follow `@Serializable data object` pattern for routing
- MUST add corresponding entry to `Screen` sealed class for UI metadata
- Icon MUST be from Material Icons library

---

## 2. Explore Section Data Model

### ExploreSection Data Class

```kotlin
// File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/explore/ExploreSection.kt

/**
 * Represents a discoverable section in the Explore screen
 */
data class ExploreSection(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: Any, // @Serializable navigation object
    val contentDescription: String = "Navigate to $title"
)
```

**Properties**:
- `id`: String - Unique identifier for section (e.g., "iss_tracking", "agencies")
- `title`: String - Display title (e.g., "ISS Tracking", "Agencies")
- `description`: String - Brief description shown on card
- `icon`: ImageVector - Material icon representing the section
- `route`: Any - Type-safe @Serializable navigation object (Rockets, Agencies, etc.)
- `contentDescription`: String - Accessibility description for screen readers

**Validation Rules**:
- `id` MUST be unique across all sections
- `title` MUST be non-empty, max 20 characters for UI constraints
- `description` MUST be non-empty, max 50 characters (recommended for card layout)
- `icon` MUST be from Material Icons library
- `route` MUST be a valid @Serializable navigation object defined in Screen.kt
- `contentDescription` MUST be descriptive for accessibility

**Static Data**:
```kotlin
// File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/explore/ExploreSections.kt

object ExploreSections {
    val sections = listOf(
        ExploreSection(
            id = "iss_tracking",
            title = "ISS Tracking",
            description = "Live tracking and crew info",
            icon = Icons.Filled.Satellite,
            route = SpaceStationDetail(4), // ISS has ID 4
            contentDescription = "Navigate to ISS Tracking with live position and crew information"
        ),
        ExploreSection(
            id = "agencies",
            title = "Agencies",
            description = "Space agencies and missions",
            icon = Icons.Filled.Business,
            route = Agencies,
            contentDescription = "Navigate to Agencies list to explore space organizations"
        ),
        ExploreSection(
            id = "astronauts",
            title = "Astronauts",
            description = "Browse astronaut profiles",
            icon = Icons.Filled.Person,
            route = Astronauts,
            contentDescription = "Navigate to Astronauts list to view career stats and missions"
        ),
        ExploreSection(
            id = "rockets",
            title = "Rockets",
            description = "Launcher configurations",
            icon = Icons.Filled.Rocket,
            route = Rockets,
            contentDescription = "Navigate to Rockets list to explore launch vehicles"
        ),
        ExploreSection(
            id = "starship",
            title = "Starship",
            description = "SpaceX Starship development",
            icon = Icons.Filled.RocketLaunch,
            route = Starship,
            contentDescription = "Navigate to Starship dashboard for updates and launches"
        )
    )
}
```

---

## 3. UI Component State Models

### ExploreCard State

```kotlin
// File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/explore/components/ExploreCard.kt

// No explicit state class needed - ExploreSection serves as the data model
// Card is stateless and receives:
// - section: ExploreSection (data to display)
// - onClick: () -> Unit (navigation action)
// - modifier: Modifier (styling)
```

**Component Properties**:
- `section`: ExploreSection - Data to display (title, description, icon)
- `onClick`: () -> Unit - Callback when card is tapped
- `modifier`: Modifier - Compose modifier for styling/layout

**Visual States**:
- Normal: Default card appearance
- Pressed: Material ripple effect (handled by Card component)
- Focused: Keyboard focus highlight (handled by Compose)

**No State Management Needed**: Cards are stateless presentational components. All data flows from parent ExploreScreen.

---

## 4. Screen State Models

### ExploreScreen State

```kotlin
// File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/explore/ExploreScreen.kt

// No ViewModel needed - screen is stateless
// Data is static (ExploreSections.sections)
// Navigation is handled by NavHostController passed from parent

// Screen parameters:
@Composable
fun ExploreScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Static list of sections
    val sections = ExploreSections.sections
    
    // No loading, error, or dynamic state
    // Pure presentation + navigation
}
```

**No ViewModel Required**: This is a pure navigation/presentation screen with no:
- Network requests
- Database queries
- User input validation
- Complex state management

**State Transitions**:
None - screen has no dynamic state. Only navigation events.

---

## 5. Bottom Navigation State Extension

### Updated Bottom Navigation Items

```kotlin
// File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/compose/BottomNavigationBar.kt

// Current items list
val items = listOf(
    Screen.Home,
    Screen.Schedule,
    Screen.Settings
)

// Updated items list (AFTER implementation)
val items = listOf(
    Screen.Home,
    Screen.Schedule,
    Screen.Explore,  // NEW
    Screen.Settings
)

// Corresponding routes
val routes = listOf(Home, Schedule, Explore, Settings)  // Add Explore
```

**Navigation State**:
- Managed by Jetpack Navigation Compose `NavHostController`
- Each tab maintains its own back stack with state preservation
- Bottom nav selection follows current destination in back stack

**No Additional State**: Navigation state is fully managed by Compose Navigation.

---

## Entity Relationships Diagram

```
┌─────────────────────┐
│  Bottom Navigation  │
│   (NavHostController│
└──────────┬──────────┘
           │
           ├─────────────────────┐
           │                     │
    ┌──────▼──────┐      ┌──────▼─────────┐
    │ Screen.kt   │      │ ExploreScreen  │
    │ (Metadata)  │      │   (Composable) │
    └─────────────┘      └───────┬────────┘
           │                     │
           │              ┌──────▼──────────────┐
           │              │ ExploreSections     │
           │              │ (Static Data)       │
           │              └──────┬──────────────┘
           │                     │
           │              ┌──────▼──────────────┐
           │              │ List<ExploreSection>│
           │              └──────┬──────────────┘
           │                     │
           │              ┌──────▼──────────┐
           │              │ ExploreCard     │
           │              │ (Stateless UI)  │
           │              └──────┬──────────┘
           │                     │
           │                  onClick
           │                     │
           └─────────────────────▼─────────────┐
                          Navigate to:          │
                    ┌──────────────────────┐   │
                    │ Rockets              │◄──┤
                    │ Agencies             │   │
                    │ Astronauts           │   │
                    │ Starship             │   │
                    │ SpaceStationDetail(4)│   │
                    └──────────────────────┘   │
```

---

## Data Flow

```
1. User taps "Explore" tab in bottom nav
   ↓
2. NavHostController navigates to Explore route
   ↓
3. ExploreScreen composable renders
   ↓
4. Reads static sections from ExploreSections.sections
   ↓
5. Renders LazyVerticalGrid of ExploreCard components
   ↓
6. User taps a card (e.g., "Astronauts")
   ↓
7. Card onClick triggers navController.navigate(section.route)
   ↓
8. NavHostController navigates to destination (Astronauts)
   ↓
9. Explore screen remains in back stack with saved state
```

---

## Summary

**Entities**:
1. **Screen.Explore** - Navigation metadata (icon, label)
2. **Explore** (@Serializable) - Type-safe route object
3. **ExploreSection** - Display data for each discoverable section
4. **ExploreSections** - Static list of 5 sections (ISS, Agencies, Astronauts, Rockets, Starship)

**Relationships**:
- Screen.Explore → ExploreScreen (1:1)
- ExploreScreen → List<ExploreSection> (1:5)
- ExploreSection → Navigation Route (1:1)

**State Management**:
- **None required** - Static data, stateless UI, navigation handled by framework

**Validation**:
- All navigation routes pre-validated (already exist in codebase)
- All icons pre-validated (Material Icons library)
- All text content hardcoded and reviewed for accessibility

**No Backend/Database**: This is a pure frontend navigation feature with no persistence layer.
