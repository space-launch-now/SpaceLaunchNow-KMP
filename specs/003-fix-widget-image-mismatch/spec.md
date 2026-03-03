# Feature Specification: Fix iOS Widget Image/Launch Mismatch on Refresh

**Feature Branch**: `003-fix-widget-image-mismatch`  
**Created**: 2026-03-02  
**Status**: Draft  
**Input**: User description: "The iOS widgets picture doesn't match the launch that's associated on a refresh."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Correct Image per Launch in Widget (Priority: P1)

As an iOS user with the SpaceLaunchNow widget on my home screen, I expect each launch entry in the widget to display the correct image for that specific launch, not an image from a different launch.

**Why this priority**: This is the core bug — incorrect images erode user trust and make the widget confusing/useless for identifying launches at a glance.

**Independent Test**: Add two different widgets (NextUpWidget + LaunchListWidget), trigger a timeline refresh, and verify every launch entry shows its own correct image.

**Acceptance Scenarios**:

1. **Given** a widget displaying launches from different missions (e.g., Falcon 9, Atlas V), **When** the widget refreshes, **Then** each launch row shows the image corresponding to its own mission, not images from other launches.
2. **Given** two launches with image URLs from the same CDN domain (e.g., `spacelaunchnow-prod-east.nyc3.cdn.digitaloceanspaces.com`), **When** the widget loads images, **Then** each launch gets its own distinct cached image file (no cache key collisions).
3. **Given** a launch whose image URL has changed in the API, **When** the widget refreshes, **Then** the new image is downloaded and displayed (not a stale cached version from a different URL).

---

### User Story 2 - Image Cache Reliability (Priority: P2)

As an iOS user, I expect the widget to load images efficiently using a cache, but never at the cost of displaying wrong images.

**Why this priority**: Caching is important for performance and battery life in widget extensions, but the current caching is the root cause of the bug.

**Independent Test**: Clear the app group container's `widget_image_cache` directory, trigger a widget refresh, and verify all images download correctly and persist in cache with unique filenames.

**Acceptance Scenarios**:

1. **Given** a cold cache (no cached images), **When** the widget refreshes, **Then** all launch images are downloaded, resized, and cached with unique filenames derived from a collision-resistant hash of the full URL.
2. **Given** a warm cache with valid images (<1 hour old), **When** the widget refreshes, **Then** cached images are reused without re-downloading.
3. **Given** a cache file older than 1 hour, **When** the widget refreshes, **Then** the image is re-downloaded from the source URL.

---

### Edge Cases

- What happens when two launches share the exact same image URL? Both should correctly display the same image (same cache file is fine — it's the correct image for both).
- What happens when the image URL is nil? No image should be shown; no crash should occur.
- What happens when the image download fails? The launch should appear without an image; previously cached images for other launches should not be affected.
- What happens during migration from old cache keys to new cache keys? Old cache files become orphaned and stale; they will be re-downloaded on next refresh. This is acceptable.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The image cache key function MUST produce unique cache filenames for distinct URLs. Cache collisions MUST NOT occur for URLs differing only in path segments.
- **FR-002**: The image cache key MUST use a cryptographic hash (SHA-256) of the full URL string to eliminate collision risk.
- **FR-003**: The `CryptoKit` framework MUST be imported for SHA-256 hashing (available on iOS 13+, which is the app's deployment target).
- **FR-004**: The image download and assignment loop MUST correctly pair each downloaded image with its corresponding `LaunchData` entry by index. *(Existing behavior — verified correct in research.md Task 4, no code change required.)*
- **FR-005**: The 1-hour cache TTL MUST remain to balance freshness with battery/network efficiency.

### Key Entities

- **LaunchData**: Holds per-launch data including `imageUrl` (source) and `image` (downloaded UIImage). The `image` field is populated after download.
- **Image Cache**: Disk-based cache in the app group container (`widget_image_cache/`) mapping URL hashes to JPEG files.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Given N launches with N distinct image URLs, the cache directory contains exactly N distinct `.jpg` files after a widget refresh (no collisions).
- **SC-002**: Every launch displayed in the widget shows its correct image (verified visually on device/simulator for both NextUpWidget and LaunchListWidget).
- **SC-003**: No regressions — widget continues to load, refresh on schedule, and handle error cases gracefully.
- **SC-004**: Cache key generation produces different outputs for URLs like `https://cdn.example.com/media/launch_images/falcon925/img.jpg` vs `https://cdn.example.com/media/launch_images/falcon926/img.jpg` (proven by unit test or manual verification).
