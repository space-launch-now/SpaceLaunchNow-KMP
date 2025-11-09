# Agency Detail Screen

## Overview
The Agency Detail Screen displays comprehensive information about a space agency, including statistics, launches, landings, and spacecraft.

## Features
- **Agency Overview**: Logo, name, type, founding year, administrator
- **Launch Statistics**: Total launches, success rate, consecutive successes
- **Landing Statistics**: Attempted/successful/failed landings for boosters and spacecraft
- **Vehicles**: List of launch vehicles and spacecraft operated by the agency
- **Links**: Website and Wikipedia links
- **Responsive Design**: Adapts to phone, tablet, and desktop layouts
- **Ad Integration**: Content placement ads and interstitial ads

## Architecture

### Components
1. **AgencyViewModel** - Manages state and data fetching
2. **AgencyDetailScreen** - Root composable with state management
3. **AgencyDetailView** - Main UI implementation
4. **Navigation Integration** - Routes in PhoneLayout and TabletDesktopLayout

### Navigation
```kotlin
// Navigate to agency detail
navController.navigate(AgencyDetail(agencyId = 44))
```

### Data Flow
```
User navigates → AgencyDetailScreen → AgencyViewModel.fetchAgencyDetails()
                                    → LaunchRepository.getAgencyDetails()
                                    → API Call → AgencyDetailView renders
```

## Usage Examples

### From a List
```kotlin
LazyColumn {
    items(agencies) { agency ->
        AgencyRow(
            agency = agency,
            onClick = {
                navController.navigate(AgencyDetail(agencyId = agency.id))
            }
        )
    }
}
```

### From Launch Detail
```kotlin
// In LaunchDetailView
AgencyDetailsCard(
    agency = launch.launchServiceProvider,
    onClick = {
        navController.navigate(AgencyDetail(agencyId = launch.launchServiceProvider.id))
    }
)
```

## UI Components

### AgencyOverviewCard
Displays:
- Agency logo
- Name and abbreviation
- Type (Government, Commercial, etc.)
- Founded year
- Administrator
- Countries
- Description
- Website/Wikipedia links

### Statistics Cards
Shows metrics in clean stat cards:
- Launch success rate
- Total launches
- Successful/failed launches
- Landing statistics
- Spacecraft landing statistics

### Color Theme
Uses tertiary color scheme to distinguish from:
- Launch Detail (primary colors)
- Event Detail (secondary colors)

## Error Handling

### Loading State
Shows SharedDetailScaffold with loading indicator

### Error State
Displays error card with:
- Error message
- Retry button
- Back navigation

### Network Errors
Handled by repository with Result<T> pattern

## Testing

### Manual Testing
1. Navigate from home screen or launch detail
2. Verify agency information displays correctly
3. Test loading and error states
4. Verify back navigation works
5. Check responsive behavior on different screen sizes

### Integration Points
- Koin dependency injection
- Navigation routes
- API repository calls
- Shared components (InfoTile, CountryChip, etc.)

## Future Enhancements
- [ ] Add related launches section
- [ ] Add agency timeline/history
- [ ] Add social media links display
- [ ] Add launcher/spacecraft detail navigation
- [ ] Add comparison with other agencies
- [ ] Add agency news feed
