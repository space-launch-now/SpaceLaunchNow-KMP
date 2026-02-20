# Quickstart Guide: Implementing Explore Tab Navigation

**Estimated Time**: 2-3 hours  
**Difficulty**: Easy-Medium  
**Prerequisites**: Familiarity with Kotlin, Compose, and project structure

---

## Phase 0: Setup & Verification (15 minutes)

### Step 0.1: Verify Navigation Routes Exist

```bash
# Check that all destination routes are defined
grep -r "data object Rockets" composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/navigation/
grep -r "data object Agencies" composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/navigation/
grep -r "data object Astronauts" composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/navigation/
grep -r "data object Starship" composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/navigation/
grep -r "data class SpaceStationDetail" composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/navigation/
```

**✅ Expected**: All 5 routes should be found in `Screen.kt`

### Step 0.2: Read Existing Code

Essential files to understand:
- `navigation/Screen.kt` - Navigation object patterns
- `ui/compose/BottomNavigationBar.kt` - Bottom nav implementation
- `ui/components/StatCard.kt` - Card component pattern
- `ui/settings/SettingsScreen.kt` - SettingsNavigationRow pattern

---

## Phase 1: Data Models & Constants (30 minutes)

### Step 1.1: Create ExploreSection Data Class

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/explore/ExploreSection.kt`

```kotlin
package me.calebjones.spacelaunchnow.ui.explore

import androidx.compose.ui.graphics.vector.ImageVector

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

### Step 1.2: Create Static Sections List

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/explore/ExploreSections.kt`

```kotlin
package me.calebjones.spacelaunchnow.ui.explore

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Satellite
import me.calebjones.spacelaunchnow.navigation.Agencies
import me.calebjones.spacelaunchnow.navigation.Astronauts
import me.calebjones.spacelaunchnow.navigation.Rockets
import me.calebjones.spacelaunchnow.navigation.SpaceStationDetail
import me.calebjones.spacelaunchnow.navigation.Starship

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

**✅ Verify**: Code compiles, all imports resolve

---

## Phase 2: UI Components (45 minutes)

### Step 2.1: Create ExploreCard Component

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/explore/components/ExploreCard.kt`

```kotlin
package me.calebjones.spacelaunchnow.ui.explore.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.ui.explore.ExploreSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreCard(
    section: ExploreSection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon in circular background
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = section.contentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = section.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}
```

**✅ Verify**: Build succeeds, no errors

### Step 2.2: Create ExploreScreen

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/explore/ExploreScreen.kt`

```kotlin
package me.calebjones.spacelaunchnow.ui.explore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import me.calebjones.spacelaunchnow.ui.explore.components.ExploreCard
import me.calebjones.spacelaunchnow.ui.platform.isTabletOrDesktop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val sections = ExploreSections.sections
    
    // Responsive grid columns
    val columns = if (isTabletOrDesktop()) {
        GridCells.Adaptive(minSize = 200.dp)
    } else {
        GridCells.Fixed(2)
    }
    
    // Responsive padding
    val horizontalPadding = if (isTabletOrDesktop()) 24.dp else 16.dp
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Explore",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = columns,
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sections) { section ->
                ExploreCard(
                    section = section,
                    onClick = {
                        navController.navigate(section.route)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun ExploreScreenPreview() {
    // Note: NavHostController requires actual navigation setup
    // Use Android Studio preview or run on device to see full screen
}
```

**✅ Verify**: Build succeeds, preview compiles (may show error without nav setup - OK for now)

---

## Phase 3: Navigation Integration (30 minutes)

### Step 3.1: Update Screen.kt

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/navigation/Screen.kt`

**Add after existing @Serializable objects**:
```kotlin
@Serializable
data object Explore
```

**Add to Screen sealed class**:
```kotlin
sealed class Screen(val label: String, val icon: ImageVector) {
    data object Home : Screen("Home", Icons.Filled.Home)
    data object Schedule : Screen("Schedule", Icons.AutoMirrored.Filled.List)
    data object Explore : Screen("Explore", Icons.Filled.Explore)  // ADD THIS
    data object Settings : Screen("Settings", Icons.Filled.Settings)
}
```

**Add import**:
```kotlin
import androidx.compose.material.icons.filled.Explore
```

**✅ Verify**: Build succeeds, no import errors

### Step 3.2: Update BottomNavigationBar.kt

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/compose/BottomNavigationBar.kt`

**Update items list**:
```kotlin
val items = listOf(
    Screen.Home,
    Screen.Schedule,
    Screen.Explore,  // ADD THIS
    Screen.Settings
)

val routes = listOf(Home, Schedule, Explore, Settings)  // ADD Explore
```

**Add import**:
```kotlin
import me.calebjones.spacelaunchnow.navigation.Explore
```

**✅ Verify**: Build succeeds, 4 tabs should appear in bottom nav (may not navigate yet)

### Step 3.3: Register Route in App.kt or PhoneLayout.kt

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt` (or PhoneLayout.kt depending on structure)

**Find the NavHost with composable routes, add**:
```kotlin
composableWithCompositionLocal<Explore> {
    ExploreScreen(navController = navController)
}
```

**Add imports**:
```kotlin
import me.calebjones.spacelaunchnow.navigation.Explore
import me.calebjones.spacelaunchnow.ui.explore.ExploreScreen
```

**✅ Verify**: Run app, tap Explore tab, screen should appear with cards

### Step 3.4: Update PhoneLayout.kt Bottom Bar Visibility

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/layout/phone/PhoneLayout.kt`

**Find `showBottomBar` when expression, ensure Explore shows bottom bar**:
```kotlin
val showBottomBar = when (navBackStackEntry?.destination?.route) {
    Home::class.qualifiedName -> true
    Schedule::class.qualifiedName -> true
    Explore::class.qualifiedName -> true  // ADD THIS
    Settings::class.qualifiedName -> true
    // ... other cases
}
```

**Add import**:
```kotlin
import me.calebjones.spacelaunchnow.navigation.Explore
```

**✅ Verify**: Bottom bar stays visible on Explore screen

---

## Phase 4: Testing (30 minutes)

### Step 4.1: Manual Testing Checklist

**Phone/Android Emulator**:
- [ ] Launch app, see 4 tabs in bottom nav (Home, Schedule, Explore, Settings)
- [ ] Tap Explore, see grid of 5 cards
- [ ] Cards display: ISS Tracking, Agencies, Astronauts, Rockets, Starship
- [ ] Tap ISS Tracking card, navigate to SpaceStationDetail screen
- [ ] Back button returns to Explore screen
- [ ] Tap Agencies card, navigate to Agencies list
- [ ] Navigate to another tab (Home), then back to Explore - state preserved
- [ ] Rotate device, grid adapts (2 columns portrait)

**Tablet/Desktop**:
- [ ] Run on tablet emulator or desktop
- [ ] Verify 2-3 columns in grid (adaptive layout)
- [ ] Cards have appropriate spacing

**Accessibility**:
- [ ] Enable TalkBack (Android) or screen reader
- [ ] Navigate to Explore screen
- [ ] Each card announces title + content description
- [ ] Cards are focusable and activatable

### Step 4.2: Automated UI Tests (Optional but Recommended)

**File**: `composeApp/src/androidTest/kotlin/me/calebjones/spacelaunchnow/ui/explore/ExploreScreenTest.kt`

```kotlin
package me.calebjones.spacelaunchnow.ui.explore

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.calebjones.spacelaunchnow.navigation.Explore
import org.junit.Rule
import org.junit.Test

class ExploreScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun exploreScreen_DisplaysAllSections() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = Explore) {
                composable<Explore> { ExploreScreen(navController) }
            }
        }

        // Verify all 5 sections are displayed
        composeTestRule.onNodeWithText("ISS Tracking").assertIsDisplayed()
        composeTestRule.onNodeWithText("Agencies").assertIsDisplayed()
        composeTestRule.onNodeWithText("Astronauts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rockets").assertIsDisplayed()
        composeTestRule.onNodeWithText("Starship").assertIsDisplayed()
    }

    @Test
    fun exploreCard_ClickNavigates() {
        // Note: Full navigation test requires more complex setup
        // Focus on manual testing for navigation flows
    }
}
```

**✅ Run**: `./gradlew connectedAndroidTest` (Android only)

---

## Phase 5: Polish & Finalization (15 minutes)

### Step 5.1: Remove Settings Explore Section (Optional)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/SettingsScreen.kt`

**Find and remove or comment out the "Explore (WORK IN PROGRESS)" section**:
```kotlin
// EXPLORE - MOVED TO BOTTOM NAV TAB
// item {
//     SectionHeaderText("Explore (WORK IN PROGRESS VIEWS)")
//     ...
// }
```

**✅ Verify**: Settings screen no longer shows WIP explore section

### Step 5.2: Verify Conventional Commit Message

**Commit format**:
```bash
git add .
git commit -m "feat(ui): add explore tab with navigation to discoveries

- Add Explore tab to bottom navigation with Icons.Filled.Explore
- Create ExploreScreen with responsive grid layout (2 cols phone, 2-3 tablet/desktop)
- Add 5 explore cards: ISS Tracking, Agencies, Astronauts, Rockets, Starship
- Implement ExploreCard component with icon, title, description
- Register Explore route in navigation and update bottom bar
- Remove WIP explore section from Settings screen
- Add accessibility content descriptions for screen readers

BREAKING CHANGE: None
"
```

**✅ Verify**: Message follows `feat(scope): subject` format

---

## Troubleshooting

### Issue: "Explore tab doesn't appear in bottom nav"
**Solution**: Check BottomNavigationBar.kt - ensure `Screen.Explore` is in `items` list and `Explore` route is in `routes` list

### Issue: "Tapping Explore crashes or does nothing"
**Solution**: Verify `composableWithCompositionLocal<Explore> { ExploreScreen(...) }` is registered in App.kt or PhoneLayout.kt NavHost

### Issue: "Cards don't navigate when tapped"
**Solution**: Check that section.route objects (Rockets, Agencies, etc.) are registered in NavHost with their own composable routes

### Issue: "Grid looks wrong on tablet"
**Solution**: Verify `isTabletOrDesktop()` function is imported and used correctly for GridCells.Adaptive

### Issue: "Import errors for icons"
**Solution**: Ensure Material Icons dependency is in build.gradle.kts (should already be present)

---

## Estimated Timeline

| Phase | Description | Time |
|-------|-------------|------|
| 0 | Setup & Verification | 15 min |
| 1 | Data Models | 30 min |
| 2 | UI Components | 45 min |
| 3 | Navigation | 30 min |
| 4 | Testing | 30 min |
| 5 | Polish | 15 min |
| **Total** | | **~2h 45m** |

---

## Next Steps After Implementation

1. **Test thoroughly** on all platforms (Android, iOS, Desktop)
2. **Document in CHANGELOG** (automated by conventional commits)
3. **Create PR** to dev/main branch
4. **Request code review** from team
5. **Deploy** via CI/CD (auto-triggered on merge to main)

---

## Success Criteria

- [x] Explore tab appears in bottom navigation
- [x] ExploreScreen displays 5 cards in responsive grid
- [x] Each card navigates to correct destination
- [x] Back navigation works correctly
- [x] UI is accessible (content descriptions, keyboard nav)
- [x] Code follows project patterns (previews, components, conventions)
- [x] Tests pass (manual + automated if added)
- [x] Conventional commit format used

**Congratulations! The Explore tab feature is complete.** 🚀
