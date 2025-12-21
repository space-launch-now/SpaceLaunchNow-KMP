# ISS Tracking Feature

## Overview

A comprehensive ISS (International Space Station) tracking page with live data, orbit visualization, and crew information.

## Features

### For ISS (Station ID 4):
- **Live Position Tracking**: Real-time ISS location updated every 10 seconds
- **Orbit Visualization**: Full orbit ground track calculated using Where is the ISS positioning API
- **Interactive Map**: Google Maps on Android showing current position and orbit path
- **Live Video Stream**: Embedded NASA ISS live feed
- **Current Crew**: Display active expedition crew members with photos
- **Expedition Info**: Active expedition details and timeline
- **News Reports**: Related articles from SNAPI filtered by ISS keywords

### For Other Space Stations:
- Station details and status
- Active expedition information
- Crew roster
- Related news articles

## Architecture

### Data Sources

1. **wheretheiss.at API** (`https://api.wheretheiss.at/v1`)
   - Current ISS position (`/satellites/25544`)
   - TLE data for orbit propagation (`/satellites/25544/tles`)
   - No authentication required

2. **Launch Library API** (`https://spacelaunchnow.app`)
   - Space station details (`/api/ll/2.4.0/space_stations/{id}/`)
   - Active expedition and crew data
   - Requires API key authentication

3. **SNAPI** (`https://api.spaceflightnewsapi.net`)
   - Space-related news reports (`/v4/reports/`)
   - No authentication required

### Key Components

- **OrbitPropagator**: Pure Kotlin SGP4 implementation for orbit calculations
- **IssTrackingRepository**: Handles wheretheiss.at API calls
- **SpaceStationViewModel**: Coordinates data from multiple sources with polling
- **IssMapView**: Platform-specific map rendering (expect/actual pattern)

### Platform Support

| Platform | Map | Live Video | Notes |
|----------|-----|------------|-------|
| Android | ✅ Google Maps Compose | ✅ YouTube embed | Full functionality |
| Desktop | ⚠️ Placeholder | ✅ YouTube embed | Shows lat/lon text |
| iOS | ⚠️ Placeholder | ✅ YouTube embed | Map planned for future |

## Setup

### Required Dependencies

```toml
# gradle/libs.versions.toml
maps-compose = "6.1.0"
```

### Google Maps API Key (Android)

1. Get API key from [Google Cloud Console](https://console.cloud.google.com/)
2. Enable Maps SDK for Android
3. Add to `local.properties`:
   ```properties
   MAPS_API_KEY=your_actual_api_key_here
   ```
4. Key is injected into `AndroidManifest.xml` via `manifestPlaceholders`


## References

- [wheretheiss.at API Documentation](https://wheretheiss.at/w/developer)
- [SGP4 Algorithm (Spacetrack Report #3)](https://celestrak.org/NORAD/documentation/spacetrk.pdf)
- [Google Maps Compose Documentation](https://developers.google.com/maps/documentation/android-sdk/maps-compose)
- [NASA ISS Live Stream](https://www.youtube.com/nasa/live)
