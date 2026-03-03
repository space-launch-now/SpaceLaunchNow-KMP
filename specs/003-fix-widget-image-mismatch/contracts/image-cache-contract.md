# Contract: Widget Image Cache

**Branch**: `003-fix-widget-image-mismatch` | **Date**: 2026-03-02

## Contract Definition

This document defines the expected behavior of the image cache system in `iosApp/LaunchWidget/LaunchData.swift`.

### Cache Key Function Contract

**Function**: `cacheFile(for urlString: String) -> URL`

| Property | Requirement |
|----------|-------------|
| **Input** | Any valid URL string |
| **Output** | File URL in `{app_group}/widget_image_cache/{hash}.jpg` |
| **Hash algorithm** | SHA-256 (via `CryptoKit`) |
| **Hash output** | 64-character lowercase hexadecimal string |
| **Collision resistance** | Distinct inputs MUST produce distinct outputs (2^-256 collision probability) |
| **Deterministic** | Same input MUST always produce the same output |

### Before (Broken)

```swift
private static func cacheFile(for urlString: String) -> URL {
    let hash = urlString.data(using: .utf8)!.base64EncodedString()
        .replacingOccurrences(of: "/", with: "_")
        .prefix(64)
    return imageCacheDir.appendingPathComponent(String(hash) + ".jpg")
}
```

**Problem**: `prefix(64)` on base64 encoding only covers first ~48 bytes of URL. All CDN URLs share this prefix → all images map to the same cache file.

### After (Fixed)

```swift
import CryptoKit

// internal (not private) to allow unit testing from LaunchWidgetTests target
static func cacheFile(for urlString: String) -> URL {
    let data = Data(urlString.utf8)
    let hash = SHA256.hash(data: data)
    let hashString = hash.compactMap { String(format: "%02x", $0) }.joined()
    return imageCacheDir.appendingPathComponent(hashString + ".jpg")
}
```

**Guarantee**: SHA-256 processes the entire URL string. Different URLs produce different 64-char hex digests.

**Visibility**: `internal` (default Swift access) to enable `@testable import` from `LaunchWidgetTests`.

### Behavioral Contract

| Scenario | Expected Behavior |
|----------|------------------|
| Two different URLs | Two different cache files |
| Same URL called twice | Same cache file (deterministic) |
| Cache file exists and < 1 hour old | Return cached image, no download |
| Cache file exists and >= 1 hour old | Re-download and overwrite |
| Cache file does not exist | Download, resize, cache, return |
| Download fails | Return nil, no cache file created |
| Invalid URL string | Return nil |

### Verification

```bash
# After fix: Count unique cache files after widget refresh with N distinct image URLs
# Should see N unique .jpg files (not 1 shared file)
ls ~/Library/Group\ Containers/group.me.calebjones.spacelaunchnow/widget_image_cache/
```
