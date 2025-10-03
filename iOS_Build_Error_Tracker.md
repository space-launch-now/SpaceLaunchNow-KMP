# iOS Build Error Tracker

This file tracks unresolved build errors for the iOS target and their resolution status.

## Error List

| Error Message                                                                 | File/Location                                                                 | Status   | Notes/Resolution Steps |
|------------------------------------------------------------------------------|-------------------------------------------------------------------------------|----------|-----------------------|
| Unresolved reference 'JvmStatic'                                             | composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/test/ApiTest.kt | ✅ Fixed | Removed JVM-specific annotation and usage |
| Unresolved reference 'javaClass'                                             | composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/test/SimpleApiTest.kt | ✅ Fixed | Replaced with platform-agnostic code |
| Unresolved reference 'App'                                                   | composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/MainViewController.kt | ✅ Fixed | Entry point now uses SpaceLaunchNowApp() |
| actual fun initPlatformSpecific(...) has no corresponding expected declaration| composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/ui/theme/PlatformSpecificInit.kt | ✅ Fixed | Added expect/actual declarations |
| Unresolved reference 'ImageColorExtractor'                                   | composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/ui/theme/PlatformSpecificInit.kt | ✅ Fixed | Stubbed actual class for iOS |
| Expected getScreenWidth has no actual declaration in module <commonMain> for Native | composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/Platform.kt | ✅ Fixed | Added actual implementation for iOS |
| Mismatched @Composable annotation between expect and actual declaration       | composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/Platform.kt, PlatformSpecificInit.kt | ✅ Fixed | Annotation now matches expect declaration |
| Unresolved reference 'dp'                                                    | composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/Platform.kt        | ✅ Fixed | Imported dp extension |

## Resolution Steps
- [x] Fix JVM-specific code in test files
- [x] Add missing expect/actual declarations
- [x] Implement or stub ImageColorExtractor for iOS
- [x] Fix Compose entry point for iOS
- [x] Add actual getScreenWidth for iOS
- [x] Match @Composable annotation
- [x] Import dp extension

---

All tracked iOS build errors have been resolved. Build is successful.
