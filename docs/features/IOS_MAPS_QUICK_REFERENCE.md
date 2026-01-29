# iOS Google Maps - Quick Reference

## 🎯 Quick Start

1. **Add SDK** (in Xcode):
   ```
   File → Add Package Dependencies
   URL: https://github.com/googlemaps/ios-maps-sdk
   Version: 8.4.0+
   Target: iosApp
   ```

2. **Enable API** (Google Cloud Console):
   - Enable "Maps SDK for iOS"
   - Add bundle ID: `me.spacelaunchnow.spacelaunchnow`

3. **Build**:
   ```bash
   ./gradlew :composeApp:iosSimulatorArm64Test
   ```

## 📁 Key Files

| File | Purpose |
|------|---------|
| `iosApp/iosApp/IssMapViewController.swift` | Native map view controller |
| `composeApp/.../IssMapView.ios.kt` | Kotlin bridge to Swift |
| `iosApp/iosApp/AppDelegate.swift` | API key initialization |

## 🔧 Architecture

```
Kotlin → NSNotification → Swift → GMSMapView
```

**Notification**: `"SpaceLaunchNow.UpdateIssMapView"`

**Data Structure**:
```kotlin
userInfo = [
    "currentPosition": ["latitude": Double, "longitude": Double],
    "orbitPath": [["latitude": Double, "longitude": Double], ...],
    "isInteractive": Bool
]
```

## 🐛 Common Issues

| Problem | Solution |
|---------|----------|
| "Class not found" | Add `.swift` to iosApp target in Xcode |
| "No module GoogleMaps" | Add package via SPM, clean build |
| Blank map | Check API key in Google Cloud Console |
| Polyline wraps | Antimeridian logic issue, check segments |

## 🧪 Testing

**Navigate to**: Space Station detail (e.g., ISS)

**Expected**:
- Satellite map view
- Blue ISS marker
- Cyan orbit polyline
- 3-5 polyline segments (for typical ISS orbit)

**Console logs**:
```
🗺️ IssMapViewController: Created 4 segments
🗺️ IssMapViewController: Camera initialized to ISS position
```

## 📊 Features

- ✅ Satellite view
- ✅ ISS marker with coordinates
- ✅ Orbit ground track (90 points, 180 min)
- ✅ Antimeridian crossing detection
- ✅ Interactive/non-interactive modes
- ✅ Real-time updates

## 📚 Documentation

- Full setup: `docs/features/IOS_GOOGLE_MAPS_SETUP.md`
- Implementation: `docs/features/IOS_MAPS_IMPLEMENTATION_SUMMARY.md`
- Verify setup: `bash scripts/verify-ios-maps-setup.sh`

## 💡 Tips

- **First time setup**: May take 5-10 min for SPM to download SDK
- **Simulator**: Works with or without valid API key (watermark shown)
- **Production**: Requires valid API key from BuildConfig
- **Cost**: Free tier covers typical usage (<100k loads/month)
