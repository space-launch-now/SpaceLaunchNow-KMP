# Bidirectional Launch Carousel - Visual Guide

## Before (Original Behavior)

```
[Upcoming Launches Carousel]
┌────────────────────────────────────────────────────────────┐
│                                                            │
│  [Launch 1] → [Launch 2] → [Launch 3] → [Launch 4] → ...  │
│   SUCCESS      TBD          GO           TBD              │
│  (Completed)  (Upcoming)  (Upcoming)   (Upcoming)         │
│                                                            │
└────────────────────────────────────────────────────────────┘
     ↑
     Issue: "SUCCESS" launch appears in "upcoming" context
```

## After (New Behavior)

```
[Combined Launches Carousel - Bidirectional Scrolling with Centered Positioning]
┌────────────────────────────────────────────────────────────────────────┐
│                                                                        │
│  ← Previous          |          Upcoming →                            │
│                      |                                                │
│     [P2] [P1]      [U1]      [U2] [U3] ...                           │
│     peek  peek   CENTERED     peek  peek                              │
│                     ↑                                                 │
│              Initial Position                                         │
│         (First Upcoming Launch)                                       │
│              ** CENTERED **                                           │
│        with adjacent cards peeking                                    │
│                                                                        │
└────────────────────────────────────────────────────────────────────────┘

Legend:
P1-P5: Previous launches (reversed, oldest to newest)
U1-U5: Upcoming launches
CENTERED: Card is horizontally centered on screen
peek: Partial views of adjacent cards visible on edges
```

## User Interaction Flow

### 1. Initial Load
```
User opens Home Screen
         ↓
App loads 5 previous + 10 upcoming launches in parallel
         ↓
Carousel automatically centers first upcoming launch (U1)
         ↓
User sees upcoming launch CENTERED with adjacent cards peeking
         ↓
Previous launch (P1) visible on LEFT edge (peek)
Next launch (U2) visible on RIGHT edge (peek)
```

### 2. Scroll Left (View Previous)
```
User swipes/scrolls left
         ↓
Carousel reveals previous launches
         ↓
User can see recent completed launches
         ↓
No more confusion about "SUCCESS" in upcoming context
```

### 3. Scroll Right (View More Upcoming)
```
User swipes/scrolls right
         ↓
Carousel shows more upcoming launches
         ↓
User can see future launches
```

## Data Structure

### Combined Launches Array
```kotlin
combinedLaunches = [
    // Previous launches (reversed chronologically)
    LaunchNormal(id="p5", status="SUCCESS", net="2024-01-01"),  // oldest
    LaunchNormal(id="p4", status="SUCCESS", net="2024-01-02"),
    LaunchNormal(id="p3", status="SUCCESS", net="2024-01-03"),
    LaunchNormal(id="p2", status="SUCCESS", net="2024-01-04"),
    LaunchNormal(id="p1", status="SUCCESS", net="2024-01-05"),  // most recent previous
    
    // Upcoming launches (chronologically)
    LaunchNormal(id="u1", status="TBD", net="2024-01-06"),      // ← upcomingStartIndex (5)
    LaunchNormal(id="u2", status="GO", net="2024-01-07"),
    LaunchNormal(id="u3", status="TBD", net="2024-01-08"),
    // ... more upcoming
]

upcomingStartIndex = 5  // Position where upcoming starts
```

## State Management

```
HomeViewModel States:
├── previousLaunches: List<LaunchNormal>         [5 items]
├── upcomingLaunches: List<LaunchNormal>         [10 items]
├── combinedLaunches: List<LaunchNormal>         [15 items total]
├── upcomingStartIndex: Int                      [value: 5]
├── isPreviousLaunchesLoading: Boolean
├── isUpcomingLaunchesLoading: Boolean
├── previousLaunchesError: String?
└── upcomingLaunchesError: String?
```

## Benefits Visualization

### Problem Solved
```
BEFORE:
┌─────────────────┐
│ ✗ Confusing     │
│ ✗ SUCCESS in    │
│   "upcoming"    │
│ ✗ No context    │
│   for recent    │
│   launches      │
└─────────────────┘

AFTER:
┌─────────────────┐
│ ✓ Clear context │
│ ✓ Previous &    │
│   upcoming      │
│   separated     │
│ ✓ Seamless      │
│   navigation    │
│ ✓ Better UX     │
└─────────────────┘
```

## Performance

```
Parallel Loading:
┌────────────────────────────┐
│ API Call 1: Previous (5)   │ ─┐
│ API Call 2: Upcoming (10)  │ ─┤ Async
└────────────────────────────┘ ─┘
         ↓
  Combine Results
         ↓
    Update UI

Total Time ≈ max(call1, call2) instead of call1 + call2
```

## Configuration

To adjust the number of launches loaded, modify in `HomeViewModel.kt`:

```kotlin
// Line ~179-180
val previousDeferred = async { 
    launchRepository.getPreviousLaunchesNormal(limit = 5)  // Change this
}
val upcomingDeferred = async { 
    launchRepository.getUpcomingLaunchesNormal(limit = 10) // Or this
}
```

## Example Scenario

```
Timeline:
├── Jan 1  [Falcon 9] ────────────── SUCCESS (Previous)
├── Jan 2  [Atlas V] ─────────────── SUCCESS (Previous)
├── Jan 3  [Soyuz] ───────────────── SUCCESS (Previous)
├── Jan 4  [Ariane 6] ────────────── SUCCESS (Previous)
├── Jan 5  [Electron] ────────────── SUCCESS (Previous)
│
├── Jan 6  [Falcon 9] ────────────── TBD (Upcoming) ← Initial view
├── Jan 7  [Vulcan] ──────────────── GO (Upcoming)
├── Jan 8  [New Glenn] ───────────── TBD (Upcoming)
└── Jan 9  [Starship] ────────────── TBD (Upcoming)

User Experience:
1. Opens app → Sees "Falcon 9 TBD" first (Jan 6)
2. Scrolls left → Sees recent "Electron SUCCESS" (Jan 5)
3. Scrolls right → Sees future "Starship TBD" (Jan 9)
```
