# Contract: SKAdNetwork Identifiers in Info.plist

**Branch**: `002-enable-skadnetwork` | **Date**: 2026-03-02

## Contract Definition

This document defines the expected structure of the `SKAdNetworkItems` key in `iosApp/iosApp/Info.plist`.

### Required Keys

| Key | Type | Required | Description |
|-----|------|----------|-------------|
| `GADApplicationIdentifier` | String | Yes | Google AdMob app ID (`ca-app-pub-9824528399164059~3133099497`) |
| `SKAdNetworkItems` | Array of Dict | Yes | Array of dictionaries, each containing one `SKAdNetworkIdentifier` string |

### SKAdNetworkItems Array Contract

- **Minimum entries**: 50 (per Google's official list as of 2026-01-30)
- **Each entry format**: `<dict><key>SKAdNetworkIdentifier</key><string>{id}.skadnetwork</string></dict>`
- **No duplicates**: Each identifier must appear exactly once
- **Source of truth**: https://developers.google.com/admob/ios/3p-skadnetworks

### Validation Rules

1. The plist must be valid XML conforming to Apple's PropertyList DTD
2. All 50 identifiers from Google's official list must be present
3. No duplicate `SKAdNetworkIdentifier` values
4. Each identifier must match the pattern: `[a-z0-9]+\.skadnetwork`

### Change Required

Add the following entry to the `SKAdNetworkItems` array (between `s39g8k73mm.skadnetwork` and `3qy4746246.skadnetwork` to maintain the order from Google's documentation):

```xml
<dict>
    <key>SKAdNetworkIdentifier</key>
    <string>wg4vff78zm.skadnetwork</string>
</dict>
```

### Verification

After modification, the `SKAdNetworkItems` array should contain exactly 50 entries. Verify with:

```bash
# Count SKAdNetworkIdentifier entries
grep -c "SKAdNetworkIdentifier" iosApp/iosApp/Info.plist
# Expected: 100 (50 keys + 50 values = 100 lines containing the string)

# Check for duplicates
grep "<string>.*skadnetwork</string>" iosApp/iosApp/Info.plist | sort | uniq -d
# Expected: no output (no duplicates)
```
