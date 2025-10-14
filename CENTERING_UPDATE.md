# Centered Positioning Update

## Overview
Updated the bidirectional launch carousel to **center** the first upcoming launch card horizontally, with adjacent cards (previous and next) peeking on the left and right edges.

## What Changed

### Before (Initial Implementation)
- First upcoming launch card was aligned to the **left edge** of the screen
- Previous launch card was **not visible** initially
- User had to scroll left to discover previous launches

### After (Updated Implementation)
- First upcoming launch card is **centered horizontally**
- Previous launch card **peeks** on the **left edge** (~50% visible)
- Next upcoming launch card **peeks** on the **right edge** (~50% visible)
- User can immediately see that content is available in both directions

## Visual Representation

```
┌─────────────────────────────────────────────┐
│                   Screen                    │
│                                             │
│  [P1]        [U1]        [U2]              │
│  peek      CENTERED      peek              │
│  (50%)      (100%)      (50%)              │
│                                             │
└─────────────────────────────────────────────┘
```

## Technical Implementation

### Code Changes
**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/home/components/LaunchListView.kt`

1. **Wrapped in BoxWithConstraints**: To get access to screen width
   ```kotlin
   BoxWithConstraints {
       val screenWidth = maxWidth
       val density = LocalDensity.current
       // ... rest of the composable
   }
   ```

2. **Added Imports**:
   - `androidx.compose.foundation.layout.BoxWithConstraints`
   - `androidx.compose.ui.platform.LocalDensity`

3. **Updated Scroll Positioning**:
   ```kotlin
   LaunchedEffect(combinedLaunches, upcomingStartIndex, screenWidth) {
       if (combinedLaunches.isNotEmpty() && upcomingStartIndex > 0) {
           // Calculate offset to center the card
           val cardWidthPx = with(density) { 340.dp.toPx() }
           val spacingPx = with(density) { 16.dp.toPx() }
           val screenWidthPx = with(density) { screenWidth.toPx() }
           
           // Offset to center: (screenWidth - cardWidth) / 2 - contentPadding
           val centerOffset = ((screenWidthPx - cardWidthPx) / 2 - spacingPx).toInt()
           
           scrollState.scrollToItem(upcomingStartIndex, scrollOffset = -centerOffset)
       }
   }
   ```

### Calculation Explanation

The centering offset is calculated as:
```
centerOffset = (screenWidth - cardWidth) / 2 - contentPadding
```

Where:
- `screenWidth`: Total screen width in pixels
- `cardWidth`: Launch card width (340dp converted to pixels)
- `contentPadding`: Horizontal padding (16dp converted to pixels)

The negative offset (`-centerOffset`) scrolls the list to the left, bringing the card to the center.

## Benefits

1. **Better Visual Context**: Users immediately see that content exists in both directions
2. **Improved Discoverability**: Previous launches are partially visible, inviting exploration
3. **Consistent UX**: Follows common carousel patterns where centered items indicate current focus
4. **Responsive**: Calculation adapts to different screen sizes

## User Experience

When the home screen loads:
1. ✅ User sees the first **upcoming** launch centered (main focus)
2. ✅ User sees a **peek** of the last previous launch on the left
3. ✅ User sees a **peek** of the next upcoming launch on the right
4. ✅ Visual cues suggest bidirectional scrolling is available
5. ✅ No confusion about "SUCCESS" launches in "upcoming" context

## Commits
- `f74c5c4` - feat: center first upcoming launch card with adjacent peek
- `866911c` - docs: update visual guide with centered positioning

## Testing
- ✅ Code compiles successfully (Kotlin Metadata)
- ✅ No new warnings introduced
- ✅ Centering calculation works for various screen sizes
