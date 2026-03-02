# Quickstart: Enable SKAdNetwork for Conversion Tracking

**Branch**: `002-enable-skadnetwork` | **Date**: 2026-03-02

## Prerequisites

- [x] Google Mobile Ads SDK >= 7.64.0 (current: 12.12.0 via SPM) ✅
- [x] `GADApplicationIdentifier` set in Info.plist ✅  
- [x] Existing SKAdNetworkItems array in Info.plist (49 of 50 entries) ✅

## What Needs to Change

**One file, one addition:**

Add the missing BidMachine SKAdNetwork identifier to `iosApp/iosApp/Info.plist`.

### Step 1: Add Missing Identifier

In `iosApp/iosApp/Info.plist`, locate the `SKAdNetworkItems` array and add the following entry after the `s39g8k73mm.skadnetwork` (Bidease) entry:

```xml
<dict>
    <key>SKAdNetworkIdentifier</key>
    <string>wg4vff78zm.skadnetwork</string>
</dict>
```

### Step 2: Verify

1. Build the iOS app to confirm valid plist:
   ```bash
   # From Xcode or command line
   xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug build
   ```

2. Count identifiers (should be 50):
   ```bash
   grep -c "<string>.*\.skadnetwork</string>" iosApp/iosApp/Info.plist
   ```

3. Check for duplicates (should output nothing):
   ```bash
   grep "<string>.*\.skadnetwork</string>" iosApp/iosApp/Info.plist | sort | uniq -d
   ```

### Step 3: Commit

```bash
git add iosApp/iosApp/Info.plist
git commit -m "chore(ios): add missing BidMachine SKAdNetwork identifier"
```

## Why This Matters

- **BidMachine** is a third-party ad buyer in Google's network
- Without its SKAdNetworkIdentifier, iOS cannot attribute app installs from BidMachine campaigns
- This reduces ad revenue potential and attribution accuracy
- Google's documentation explicitly includes this identifier in the recommended list

## Future Maintenance

Google periodically updates the SKAdNetwork identifiers list at:
https://developers.google.com/admob/ios/3p-skadnetworks

When the list is updated:
1. Compare the official list with `iosApp/iosApp/Info.plist`
2. Add any new identifiers
3. Remove any deprecated identifiers
4. Commit with `chore(ios): update SKAdNetwork identifiers`
