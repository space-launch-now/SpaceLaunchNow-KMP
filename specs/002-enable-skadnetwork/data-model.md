# Data Model: SKAdNetwork Configuration

**Branch**: `002-enable-skadnetwork` | **Date**: 2026-03-02

## Entities

### SKAdNetworkItem

Represents a single ad network identifier entry in the iOS Info.plist `SKAdNetworkItems` array.

| Field | Type | Description | Validation |
|-------|------|-------------|------------|
| `SKAdNetworkIdentifier` | String | Ad network identifier in format `<id>.skadnetwork` | Must match pattern `[a-z0-9]+\.skadnetwork`; must be unique within the array |

### Info.plist SKAdNetworkItems Structure

```xml
<key>SKAdNetworkItems</key>
<array>
    <dict>
        <key>SKAdNetworkIdentifier</key>
        <string>{identifier}.skadnetwork</string>
    </dict>
    <!-- ... repeated for each network -->
</array>
```

### Relationships

- **SKAdNetworkItems** → **Info.plist**: One-to-one (single array in a single plist)
- **SKAdNetworkItem** → **SKAdNetworkItems**: Many-to-one (50 entries in the array)
- **GADApplicationIdentifier** → **Info.plist**: One-to-one (single key identifying the app)

### State Transitions

N/A — SKAdNetworkItems is static configuration. It is read at app launch by the Google Mobile Ads SDK and does not change at runtime.

## Current vs Target State

| Metric | Current | Target |
|--------|---------|--------|
| Total identifiers | 49 | 50 |
| Missing entry | `wg4vff78zm.skadnetwork` | Added |
| Duplicates | 0 | 0 |

## Complete Identifier Registry

See [research.md](research.md) for the full list of 50 identifiers with network names.
