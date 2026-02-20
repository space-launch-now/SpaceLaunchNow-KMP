# Research: Explore Tab Navigation

**Date**: February 8, 2026  
**Feature**: Add Explore tab to bottom navigation with access to discovery features

## 1. Navigation Architecture

### Decision: Type-Safe Navigation with @Serializable Routes
**Rationale**: Project uses modern Compose Navigation with Kotlin Serialization for type-safe routing.

**Current Bottom Nav Structure**:
- `Home` (@Serializable data object)
- `Schedule` (@Serializable data object)
- `Settings` (@Serializable data object)

**Required Navigation Objects**:
```kotlin
// Add to navigation/Screen.kt
@Serializable
data object Explore

// Existing routes to navigate to (already defined):
@Serializable data object Rockets
@Serializable data object Agencies
@Serializable data object Astronauts
@Serializable data object Starship
@Serializable data class SpaceStationDetail(val stationId: Int) // ISS uses ID=4
```

**Pattern Verification**: ✅
- Found in `navigation/Screen.kt` - dual system with `@Serializable` for routing and `Screen` sealed class for UI metadata
- Found in `ui/compose/BottomNavigationBar.kt` - pattern for adding tabs with icon/label
- All five destination routes already exist; navigation infrastructure is ready

**Alternatives Considered**:
- ❌ String-based routes → Type-unsafe, deprecated pattern
- ❌ Legacy Screen only → Cannot handle parameterized routes
- ✅ Serializable + Screen sealed class → Type-safe, follows existing pattern

---

## 2. Icon Selection for Explore Tab

### Decision: Use Icons.Filled.Explore
**Rationale**: Material Icons Explore icon is semantically perfect for discovery/exploration features and is already imported in the project.

**Evidence**: Found in `ui/home/components/NextUpView.kt`:
```kotlin
import androidx.compose.material.icons.filled.Explore
// Used at line 263 for mission patch placeholder
```

**Icons for Explore Screen Sections**:
Based on existing usage in codebase:
- **ISS Tracking**: `Icons.Filled.Satellite` or `Icons.Filled.SatelliteAlt`
  - Found in: `LaunchVehicleDetailedStatistics.kt`, widely used for space station/satellite content
- **Agencies**: `Icons.Filled.Business`
  - Found in: `QuickStatsGrid.kt`, `OwnerAgenciesCard.kt` - standard for organizations
- **Astronauts**: `Icons.Filled.Person` or `Icons.Filled.Group`
  - Found in: `LatestUpdatesView.kt`, `SpacecraftDetailsCard.kt` - used for people/crew
- **Rockets**: `Icons.Filled.Rocket`
  - Found in: `RocketListScreen.kt`, `RocketListItem.kt` - established pattern
- **Starship**: `Icons.Filled.RocketLaunch`
  - Found in: `navigation/Screen.kt` line 84 - already defined in Screen sealed class

**Alternatives Considered**:
- ❌ Icons.Filled.Dashboard → Too generic
- ❌ Icons.Filled.Compass → Less intuitive than Explore
- ✅ Icons.Filled.Explore → Perfect semantic match, already in codebase

---

## 3. Card Layout Pattern

### Decision: Use LazyVerticalGrid with Adaptive Columns
**Rationale**: Provides responsive grid that adapts from phone (1-2 cols) to tablet/desktop (2-3 cols).

**Pattern Found**: `ui/starship/components/VehicleGrids.kt` and `StarshipVehiclesTab.kt`:
```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(configs) { config ->
        // Card content
    }
}
```

**Adaptive Strategy for Explore Screen**:
```kotlin
// Phone: GridCells.Fixed(1) or Fixed(2) - depends on card size
// Tablet/Desktop: GridCells.Adaptive(minSize = 180.dp)
```

**Component Reuse Options**:
1. **Option A**: Adapt existing `SettingsNavigationRow` component
   - Found in: `ui/settings/SettingsScreen.kt` line 585
   - Current: Horizontal layout with title/subtitle/arrow
   - Adaptation: Make it work in grid with icon on top, center-aligned
   
2. **Option B**: Create new `ExploreCard` component inspired by `StatCard`
   - Found in: `ui/components/StatCard.kt`
   - Pattern: Icon in circle, value, label - very similar to what we need
   - Modification: Replace value/label with title/subtitle, make clickable

**Recommended**: Option B - Create new ExploreCard component
- Better visual hierarchy for grid layout
- Reuses StatCard's icon-in-circle pattern
- Cleaner separation of concerns (Settings rows vs Explore cards)

**Alternatives Considered**:
- ❌ LazyColumn with full-width cards → Wastes space on tablet/desktop
- ❌ Custom FlowRow → More complex, LazyVerticalGrid handles efficiently
- ✅ LazyVerticalGrid with adaptive columns → Responsive, efficient, proven pattern

---

## 4. Compose Material Design 3 Components

### Decision: Use Material 3 Card, Icon, Text Components
**Rationale**: Project uses Material Design 3 throughout; maintain consistency.

**Components to Use**:
```kotlin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
```

**Pattern from StatCard.kt**:
```kotlin
Card(
    modifier = modifier.clickable(onClick = onClick),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
) {
    // Content with icon in circular surface + title/subtitle
}
```

**Accessibility Requirements** (from Constitution III):
- Content descriptions on all icons: `Icon(contentDescription = "Navigate to $sectionName")`
- Semantic properties on cards: Cards are already clickable with native accessibility
- Keyboard navigation: Compose handles this automatically for clickable elements

**Alternatives Considered**:
- ❌ Material 2 components → Project migrated to M3
- ❌ Custom drawn cards → Reinventing the wheel, loses accessibility
- ✅ Material 3 Card + components → Consistent, accessible, maintainable

---

## 5. Responsive Layout Strategy

### Decision: Use Platform Detection in commonMain
**Rationale**: Project already uses `isTabletOrDesktop()` pattern for responsive layouts.

**Pattern Found**: Throughout the app (implied from architecture docs)
- Phone: Single column or 2-column grid
- Tablet/Desktop: 2-3 column grid with more spacing

**Implementation**:
```kotlin
// In ExploreScreen.kt
val columns = if (isTabletOrDesktop()) {
    GridCells.Adaptive(minSize = 200.dp) // 2-3 cols depending on width
} else {
    GridCells.Fixed(2) // Always 2 columns on phone
}

LazyVerticalGrid(columns = columns) { ... }
```

**Spacing Strategy**:
- Phone: 12.dp between cards, 16.dp padding
- Tablet/Desktop: 16.dp between cards, 24.dp padding

**Alternatives Considered**:
- ❌ Hardcoded 2 columns everywhere → Poor tablet/desktop UX
- ❌ Custom breakpoint logic → Already solved by project patterns
- ✅ Adaptive with platform detection → Proven, responsive, maintainable

---

## 6. Navigation Stack Behavior

### Decision: Standard Navigation with State Preservation
**Rationale**: Follow existing bottom nav pattern - each tab maintains its own stack.

**Pattern from BottomNavigationBar.kt**:
```kotlin
navController.navigate(routes[index]) {
    popUpTo(navController.graph.startDestinationId) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

**Behavior**:
1. User taps Explore tab → Navigate to ExploreScreen
2. User taps a card (e.g., Astronauts) → Navigate to AstronautListScreen
3. User taps bottom nav (e.g., Home) → Save Explore stack state
4. User taps Explore again → Restore to last position (AstronautListScreen or ExploreScreen)

**Back Button**:
- From ExploreScreen: Exit app or pop to previous tab (platform default)
- From sub-section (e.g., Astronauts): Pop back to ExploreScreen

**Alternatives Considered**:
- ❌ Clear stack on tab change → Loses user context
- ❌ Shared stack across tabs → Confusing navigation
- ✅ Per-tab stacks with state preservation → Standard Android/iOS pattern

---

## 7. Testing Strategy

### Decision: UI Tests for Navigation + Manual Testing for Layouts
**Rationale**: Constitution VII requires tests; navigation is critical path.

**Required Tests** (from Constitution Check):
```kotlin
// composeApp/src/commonTest/kotlin/ui/explore/ExploreScreenTest.kt
class ExploreScreenTest {
    @Test
    fun exploreTabAppearsInBottomNav() { ... }
    
    @Test
    fun tappingExploreNavigatesToExploreScreen() { ... }
    
    @Test
    fun tappingISSCardNavigatesToSpaceStationDetail() { ... }
    
    @Test
    fun tappingAgenciesCardNavigatesToAgenciesList() { ... }
    
    @Test
    fun tappingAstronautsCardNavigatesToAstronautsList() { ... }
    
    @Test
    fun tappingRocketsCardNavigatesToRocketsList() { ... }
    
    @Test
    fun tappingStarshipCardNavigatesToStarship() { ... }
}
```

**Manual Testing** (responsive layouts, accessibility):
- Verify 1-2 column grid on phone (portrait/landscape)
- Verify 2-3 column grid on tablet/desktop
- Test with screen reader (Android TalkBack, iOS VoiceOver)
- Test keyboard navigation on desktop

**Alternatives Considered**:
- ❌ Skip tests → Violates Constitution VII
- ❌ Only manual tests → Not repeatable, blocks CI
- ✅ Automated UI + manual accessibility → Comprehensive coverage

---

## Summary of Research Decisions

| Area | Decision | Rationale |
|------|----------|-----------|
| **Navigation** | @Serializable + Screen sealed class | Type-safe, follows existing pattern |
| **Icon (Explore)** | Icons.Filled.Explore | Semantic match, already in codebase |
| **Section Icons** | Satellite, Business, Person, Rocket, RocketLaunch | Established usage patterns |
| **Layout** | LazyVerticalGrid with adaptive columns | Responsive, efficient, proven |
| **Cards** | New ExploreCard based on StatCard pattern | Better for grid, reuses icon pattern |
| **Material Design** | Material 3 Card + components | Consistent with app, accessible |
| **Responsive** | Platform detection + GridCells.Adaptive | Proven app pattern |
| **Nav Stack** | Per-tab stacks with state preservation | Standard mobile UX |
| **Testing** | Automated UI tests + manual accessibility | Constitution compliance |

**No NEEDS CLARIFICATION remaining** - All technical decisions are based on existing project patterns and verified through code analysis.
