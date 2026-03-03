# Data Model: Widget Image Cache

**Branch**: `003-fix-widget-image-mismatch` | **Date**: 2026-03-02

## Entities

### LaunchData

Holds per-launch data for widget display. Image is loaded asynchronously after API fetch.

| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `id` | String | Unique launch identifier | Non-empty |
| `name` | String | Launch mission name | Non-empty, defaults to "Unknown Launch" |
| `agency` | String? | Launch service provider name | Optional |
| `agencyAbbrev` | String? | LSP abbreviation | Optional |
| `location` | String | Launch pad location | Defaults to "Unknown Location" |
| `launchTime` | Date | NET (No Earlier Than) time | Must be valid Date |
| `status` | String | Launch status name | Defaults to "Unknown" |
| `imageUrl` | String? | Source URL for launch image | Optional; valid URL if present |
| `image` | UIImage? | Downloaded and resized image | Populated after download; nil on failure |

### Image Cache Entry

Each cached image is a JPEG file on disk in the app group container.

| Property | Current (Broken) | Target (Fixed) |
|----------|-----------------|----------------|
| **Directory** | `{app_group}/widget_image_cache/` | `{app_group}/widget_image_cache/` (unchanged) |
| **Filename** | `{base64_prefix_64}.jpg` | `{sha256_hex}.jpg` |
| **Key derivation** | `base64(url).prefix(64)` → **COLLISIONS** | `SHA256(url).hexString` → collision-resistant |
| **Key length** | 64 chars (variable input, truncated) | 64 chars (fixed SHA-256 hex output) |
| **TTL** | 1 hour | 1 hour (unchanged) |
| **Max image size** | 200px (downscaled) | 200px (unchanged) |
| **Format** | JPEG, 70% quality | JPEG, 70% quality (unchanged) |

### Cache Key Collision Proof

| URL | Old Key (base64 prefix 64) | New Key (SHA-256 hex) |
|-----|---------------------------|----------------------|
| `https://cdn.example.com/media/launch_images/falcon925/img1.jpg` | `aHR0cHM6Ly9jZG4uZXhh...` | `a1b2c3d4...` (unique) |
| `https://cdn.example.com/media/launch_images/falcon926/img2.jpg` | `aHR0cHM6Ly9jZG4uZXhh...` (SAME!) | `e5f6g7h8...` (unique) |

## Relationships

```
LaunchEntry (TimelineEntry)
  └── launches: [LaunchData]
        ├── LaunchData[0] → imageUrl → cacheFile(sha256(url)) → image
        ├── LaunchData[1] → imageUrl → cacheFile(sha256(url)) → image
        └── LaunchData[N] → imageUrl → cacheFile(sha256(url)) → image
```

## State Transitions

```
Image Loading State Machine (per launch):

  [No URL]        → image = nil (no download attempted)
  [Cache Hit]     → image = cached UIImage (< 1 hour old)
  [Cache Miss]    → Download → Resize → Cache → image = resized UIImage
  [Download Fail] → image = nil (graceful degradation)
```
