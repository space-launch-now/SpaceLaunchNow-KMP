# Feature Specification: Enable SKAdNetwork for Conversion Tracking

**Feature Branch**: `002-enable-skadnetwork`  
**Created**: 2026-03-02  
**Status**: Draft  
**Input**: User description: "Enable SKAdNetwork to track conversions. Prerequisites: Google Mobile Ads SDK 7.64.0 or higher. Update the SKAdNetworkItems key with an additional dictionary that defines Google and participating third-party buyers' SKAdNetworkIdentifier values in Info.plist."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - SKAdNetwork Identifiers Updated (Priority: P1)

As an app publisher, I want all Google-recommended SKAdNetwork identifiers present in my iOS app's Info.plist so that Google and participating third-party ad buyers can attribute app installs even when IDFA is unavailable.

**Why this priority**: Without the complete set of SKAdNetworkIdentifier values, ad conversion tracking is incomplete, reducing ad revenue potential and attribution accuracy for the app.

**Independent Test**: Verify Info.plist contains all 50 SKAdNetworkIdentifier values listed on the official Google documentation page (https://developers.google.com/admob/ios/3p-skadnetworks, last updated January 30, 2026).

**Acceptance Scenarios**:

1. **Given** the iOS app's Info.plist, **When** the app is built, **Then** the `SKAdNetworkItems` array contains exactly 50 entries matching Google's official list.
2. **Given** the current Info.plist has 49 entries (missing `wg4vff78zm.skadnetwork` for BidMachine), **When** the update is applied, **Then** the missing identifier is added in alphabetical position among the existing entries.
3. **Given** the Google Mobile Ads SDK version is >=12.12.0 (via SPM), **When** the app runs, **Then** the SDK can read all SKAdNetworkIdentifier values from Info.plist for conversion attribution.

---

### Edge Cases

- What happens if a duplicate SKAdNetworkIdentifier is accidentally added? The app should not crash; iOS ignores duplicates, but we should ensure no duplicates exist.
- What happens if the Google documentation adds new identifiers in the future? The process should be documented for easy future updates.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Info.plist MUST contain the `SKAdNetworkItems` key with all 50 SKAdNetworkIdentifier values from Google's official list (as of January 30, 2026).
- **FR-002**: The missing identifier `wg4vff78zm.skadnetwork` (BidMachine) MUST be added to the existing list.
- **FR-003**: No duplicate SKAdNetworkIdentifier entries MUST exist in the array.
- **FR-004**: The `GADApplicationIdentifier` key MUST remain set to the existing app's AdMob ID (`ca-app-pub-9824528399164059~3133099497`).
- **FR-005**: Google Mobile Ads SDK version MUST be >= 7.64.0 (currently 12.12.0 via SPM — prerequisite satisfied).

### Key Entities

- **SKAdNetworkItems**: Array of dictionaries in Info.plist, each containing an `SKAdNetworkIdentifier` string value identifying an ad network for conversion attribution.
- **GADApplicationIdentifier**: String value in Info.plist identifying the app's Google AdMob account.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Info.plist contains exactly 50 SKAdNetworkIdentifier entries matching Google's official list.
- **SC-002**: iOS app builds successfully with the updated Info.plist.
- **SC-003**: No duplicate entries exist in the SKAdNetworkItems array.
- **SC-004**: Google Mobile Ads SDK can initialize successfully and attribute conversions via SKAdNetwork.
