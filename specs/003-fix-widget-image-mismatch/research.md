# Research: Fix iOS Widget Image/Launch Mismatch on Refresh

**Branch**: `003-fix-widget-image-mismatch` | **Date**: 2026-03-02

## Research Task 1: Root Cause — Cache Key Collision

**Question**: Why does the widget show the wrong image for a launch after refresh?

**Finding**: The image cache key function in `iosApp/LaunchWidget/LaunchData.swift` (line ~258) uses base64 encoding truncated to 64 characters:

```swift
private static func cacheFile(for urlString: String) -> URL {
    let hash = urlString.data(using: .utf8)!.base64EncodedString()
        .replacingOccurrences(of: "/", with: "_")
        .prefix(64)
    return imageCacheDir.appendingPathComponent(String(hash) + ".jpg")
}
```

**The problem**: Base64 encoding expands input by ~33%, so 64 characters of base64 represent only the first ~48 bytes of the original URL. Since all launch images come from the same CDN (`https://spacelaunchnow-prod-east.nyc3.cdn.digitaloceanspaces.com/media/...`), the first 48 bytes are identical for every URL. The truncated base64 is the same for all of them.

**Verified**: Two different launch image URLs produce identical cache keys:
```
URL1: https://spacelaunchnow-prod-east.nyc3.cdn.digitaloceanspaces.com/media/launch_images/falcon925/image1.jpg
URL2: https://spacelaunchnow-prod-east.nyc3.cdn.digitaloceanspaces.com/media/launch_images/falcon926/image2.jpg
Hash1: aHR0cHM6Ly9zcGFjZWxhdW5jaG5vdy1wcm9kLWVhc3QubnljMy5jZG4uZGlnaXRh
Hash2: aHR0cHM6Ly9zcGFjZWxhdW5jaG5vdy1wcm9kLWVhc3QubnljMy5jZG4uZGlnaXRh
Match: TRUE — cache collision confirmed!
```

**Consequence**: Whichever image downloads first gets cached under the shared key. All subsequent launches with different URLs resolve to the same cache file and get the wrong image.

**Decision**: Replace base64 + truncation with SHA-256 hash from `CryptoKit`.
**Rationale**: SHA-256 produces a fixed 64-character hex string that is collision-resistant (probability ~1/2^256). It considers the full URL, not just the prefix.
**Alternatives considered**:
- Full base64 without truncation: Too long for filenames (URLs can be 200+ chars → 270+ base64 chars)
- MD5: Deprecated and has known collision vulnerabilities; no reason to use when SHA-256 is equally available
- Hashing only the path component: Fragile if CDN changes; SHA-256 of the full URL is simpler and more robust

## Research Task 2: Stale Cache Behavior

**Question**: Does the 1-hour cache TTL contribute to the mismatch bug?

**Finding**: The 1-hour TTL (line ~270 of LaunchData.swift) means once an image is cached, it won't be re-downloaded for 60 minutes. This is **not the primary cause** of the mismatch — the cache collision is. However, the stale cache exacerbates the issue: once the wrong image is cached under a collided key, it persists for an hour even if new data is fetched.

**Decision**: Keep the 1-hour TTL unchanged. Fixing the cache key collision eliminates the mismatch. The TTL is a reasonable tradeoff for widget battery/network efficiency.
**Rationale**: Widget extensions have strict memory and network budgets. Re-downloading images every 15 minutes (the timeline refresh interval) would be wasteful.
**Alternatives considered**: Reducing TTL to 15 minutes (matching timeline refresh) — rejected because it increases network usage without meaningful benefit once the collision bug is fixed.

## Research Task 3: CryptoKit Availability

**Question**: Is `CryptoKit` available on the minimum deployment target?

**Finding**: `CryptoKit` is available on iOS 13.0+. The app targets iOS 13.0+. The widget extension inherits this deployment target. No compatibility issues.

**Decision**: Use `import CryptoKit` and `SHA256.hash(data:)` directly.
**Rationale**: No need for a fallback — the minimum deployment target already supports CryptoKit.
**Alternatives considered**: Using `CommonCrypto` (C-based API) — rejected because CryptoKit is the modern Swift-native solution and is already available.

## Research Task 4: Data Flow Correctness

**Question**: Is the image download → launch assignment loop correct?

**Finding**: The loop in `fetchLaunches()` iterates by index:

```swift
for i in updatedLaunches.indices {
    if let urlString = updatedLaunches[i].imageUrl {
        updatedLaunches[i].image = await Self.downloadImage(from: urlString, maxSize: 200)
    }
}
```

This is sequential and safe — each launch gets its own image downloaded from its own URL. The assignment is correctly paired by index. **The bug is not in the loop logic** but in `downloadImage → cacheFile` returning the wrong cached image due to the collision.

**Decision**: No changes needed to the download loop.
**Rationale**: The loop is correct. Once `cacheFile()` returns unique keys per URL, the loop will correctly assign distinct images.

## Research Task 5: Migration Impact

**Question**: What happens to existing cached images when the cache key algorithm changes?

**Finding**: Old cache files (keyed with truncated base64) will become orphaned — they won't be found by the new SHA-256 lookup. On the next refresh:
1. New cache misses for all images (no matching SHA-256-keyed files)
2. All images re-downloaded from network
3. New cache files created with SHA-256 keys
4. Old base64-keyed files remain on disk until manually cleaned or device storage pressure triggers cleanup

**Decision**: Accept the one-time cache miss. Optionally add a cleanup step to purge the old cache directory.
**Rationale**: A one-time re-download of a few small images (~50KB each at 200px max) is negligible. The old files are in a widget cache directory that iOS can purge as needed.
**Alternatives considered**: 
- Two-phase migration (check old key, then new key) — rejected as unnecessary complexity
- Clearing the entire cache on update — acceptable but unnecessary since iOS manages container storage

## Research Task 6: Widget Extension Import Requirements

**Question**: Does the widget extension target already have CryptoKit linked?

**Finding**: `CryptoKit` is a system framework that doesn't require explicit linking — it's available via `import CryptoKit` on any target with the correct deployment target (iOS 13+). No changes to the Xcode project or build settings are needed.

**Decision**: Simply add `import CryptoKit` at the top of `LaunchData.swift`.
**Rationale**: System frameworks like CryptoKit are automatically available.

## Summary of Findings

| Question | Answer | Action |
|----------|--------|--------|
| Root cause | Base64 + prefix(64) cache key collision | Replace with SHA-256 hash |
| Which file | `LaunchData.swift` line ~258 | Modify `cacheFile(for:)` function |
| CryptoKit available? | Yes (iOS 13+) | Add `import CryptoKit` |
| Download loop correct? | Yes | No changes |
| Cache TTL correct? | Yes (1 hour) | No changes |
| Migration impact | One-time cache miss | Acceptable |
| Other files affected? | None | Only LaunchData.swift |

**All NEEDS CLARIFICATION items resolved.** The fix is a 5-line change to one function in one file.
