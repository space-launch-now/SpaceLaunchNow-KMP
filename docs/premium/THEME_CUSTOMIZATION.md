# Theme Customization - Premium Feature Implementation

## Overview
Theme customization is now a premium feature that allows users to personalize their app experience with custom colors and palette styles. This applies to both the main app and widgets.

## Architecture

### 1. Theme Preferences Storage (`ThemePreferences.kt`)
- Stores custom theme preferences in DataStore
- **Fields**:
  - `customPrimaryColor`: Custom primary color (Long/ARGB)
  - `customSecondaryColor`: Custom secondary color (Long/ARGB) [Reserved for future use]
  - `paletteStyle`: Selected palette style (String)

- **Available Palette Styles**:
  1. **TonalSpot** - Balanced and harmonious, default style
  2. **Neutral** - Subtle and muted tones
  3. **Vibrant** - Bold and saturated colors
  4. **Expressive** - Creative and dynamic palette
  5. **Rainbow** - Playful and colorful
  6. **FruitSalad** - Fresh and varied hues
  7. **Monochrome** - Single hue variations
  8. **Fidelity** - True to selected color
  9. **Content** - Adaptive to content
  10. **SchemeContent** - Content-based color scheme

### 2. Theme Application (`Theme.kt`)
Updated `SpaceLaunchNowTheme` to:
- Inject `ThemePreferences` via Koin
- Collect custom color and palette style preferences
- Apply custom theme if premium user has set preferences
- Fall back to defaults if no customization

```kotlin
val seedColor = customPrimaryColor?.let { Color(it.toULong()) } ?: Primary
val paletteStyle = when (paletteStyleName) {
    "TonalSpot" -> PaletteStyle.TonalSpot
    // ... other styles
    else -> default style
}
```

### 3. Theme Customization UI (`ThemeCustomizationScreen.kt`)
Full-featured customization screen with:
- **Premium Gate**: Shows upgrade prompt for non-premium users
- **Color Palette Picker**: 16 pre-selected colors in a grid
- **Palette Style Selector**: List of all 10 palette styles with descriptions
- **Reset to Defaults**: Button to clear all customizations

### 4. Dependency Injection
Added to `AppModule.kt`:
```kotlin
single {
    val appDataStore = get<DataStore<Preferences>>(named("AppSettingsDataStore"))
    ThemePreferences(appDataStore)
}

single { 
    ThemeCustomizationViewModel(
        themePreferences = get(),
        billingClient = get()
    )
}
```

## Premium Integration

### Billing Check
- Uses `BillingClient.isPremium` flow to determine access
- Non-premium users see upgrade prompt
- Theme customization is locked behind premium status

### Widget Support
Since widgets use the same `SpaceLaunchNowTheme` composable:
- Custom colors automatically apply to widgets
- Palette style automatically applies to widgets
- Changes reflect in real-time when user updates preferences

## User Flow

### For Premium Users:
1. Navigate to Settings → Theme Customization
2. Select a primary color from the palette
3. Choose a palette style that fits their preference
4. Changes apply immediately to app and widgets
5. Can reset to defaults anytime

### For Free Users:
1. Navigate to Settings → Theme Customization
2. See premium feature prompt with:
   - Lock icon in toolbar
   - Description of feature
   - "Upgrade to Premium" button
3. Redirected to subscription screen on button click

## Color Palette
Pre-selected colors optimized for Material Design:
- Primary (Space Blue): #239DFF
- Pink: #E91E63
- Purple: #9C27B0
- Deep Purple: #673AB7
- Indigo: #3F51B5
- Blue: #2196F3
- Cyan: #00BCD4
- Teal: #009688
- Green: #4CAF50
- Light Green: #8BC34A
- Lime: #CDDC39
- Yellow: #FFEB3B
- Amber: #FFC107
- Orange: #FF9800
- Deep Orange: #FF5722
- Red: #F44336

## Technical Details

### Color Storage
- Colors stored as `Long` (ARGB format)
- Converted to `Color` using `Color(long.toULong())`
- Null values mean using default colors

### Palette Style Storage
- Stored as `String` matching enum names
- Validated against `AVAILABLE_PALETTE_STYLES` list
- Converted to `PaletteStyle` enum in theme

### State Management
- Uses Kotlin Flows for reactive updates
- ViewModel collects preferences and exposes StateFlows
- UI recomposes automatically on preference changes
- Changes persist across app restarts

## Future Enhancements
1. **Custom Secondary Color**: Currently reserved, can be enabled later
2. **Color History**: Remember recently used colors
3. **Theme Presets**: Pre-configured themes (e.g., "Ocean", "Sunset", "Forest")
4. **Custom Color Picker**: Full color wheel for precise color selection
5. **Theme Sharing**: Export/import theme configurations
6. **Per-Widget Themes**: Different themes for different widgets

## Testing Checklist
- [ ] Premium users can access theme customization
- [ ] Free users see premium prompt
- [ ] Color selection persists after app restart
- [ ] Palette style selection persists after app restart
- [ ] Theme applies to main app immediately
- [ ] Theme applies to widgets immediately
- [ ] Reset to defaults works correctly
- [ ] Invalid palette styles are handled gracefully
- [ ] Theme works in light mode
- [ ] Theme works in dark mode
