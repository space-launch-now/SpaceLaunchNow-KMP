# Quickstart: Fix iOS Widget Image/Launch Mismatch

**Branch**: `003-fix-widget-image-mismatch` | **Date**: 2026-03-02

## Prerequisites

- [x] Xcode 16.0+ ✅
- [x] iOS deployment target 13.0+ (CryptoKit available) ✅
- [x] Access to `iosApp/LaunchWidget/LaunchData.swift` ✅

## What Needs to Change

**One file, two changes:**

### Step 1: Add CryptoKit Import

At the top of `iosApp/LaunchWidget/LaunchData.swift`, add:

```swift
import CryptoKit
```

alongside the existing imports (`Foundation`, `WidgetKit`, `SwiftUI`, `UIKit`).

### Step 2: Replace `cacheFile(for:)` Function

Replace the existing `cacheFile(for:)` function:

```swift
// BEFORE (broken — base64 prefix collides for same-domain URLs)
private static func cacheFile(for urlString: String) -> URL {
    let hash = urlString.data(using: .utf8)!.base64EncodedString()
        .replacingOccurrences(of: "/", with: "_")
        .prefix(64)
    return imageCacheDir.appendingPathComponent(String(hash) + ".jpg")
}
```

With:

```swift
// AFTER (fixed — SHA-256 produces unique key for each URL)
private static func cacheFile(for urlString: String) -> URL {
    let data = Data(urlString.utf8)
    let hash = SHA256.hash(data: data)
    let hashString = hash.compactMap { String(format: "%02x", $0) }.joined()
    return imageCacheDir.appendingPathComponent(hashString + ".jpg")
}
```

### Step 3: Verify

1. **Build**: Open `iosApp.xcodeproj` in Xcode, build the `LaunchWidgetExtension` target.
2. **Run widget**: Add NextUpWidget and LaunchListWidget to home screen on simulator.
3. **Check images**: Verify each launch displays its own correct image, not another launch's image.
4. **Check cache**: Inspect the cache directory to confirm multiple unique `.jpg` files exist (not just one).

### Step 4: Commit

```bash
git add iosApp/LaunchWidget/LaunchData.swift
git commit -m "fix(ios): use SHA-256 hash for widget image cache keys to prevent collisions"
```

## Why This Fix Works

| Aspect | Before (Broken) | After (Fixed) |
|--------|-----------------|---------------|
| Algorithm | Base64 encoding | SHA-256 cryptographic hash |
| Output length | 64 chars (truncated) | 64 chars (full hex digest) |
| Input considered | First ~48 bytes of URL | Entire URL string |
| Collision risk | **100%** for same-domain URLs | **~0%** (2^-256 probability) |
| Dependencies | None | `CryptoKit` (system framework, iOS 13+) |

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| One-time cache miss after deploy | Certain | Minimal (few small images re-downloaded) | Acceptable — transparent to user |
| CryptoKit not available | None (iOS 13+) | N/A | Already at minimum deployment target |
| Old cache files orphaned | Certain | Negligible | iOS purges as needed; all small files |
