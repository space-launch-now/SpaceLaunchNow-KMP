# Research: Enable SKAdNetwork for Conversion Tracking

**Branch**: `002-enable-skadnetwork` | **Date**: 2026-03-02

## Research Task 1: Current SKAdNetwork State

**Question**: What SKAdNetwork identifiers are currently in the iOS Info.plist?

**Finding**: The Info.plist at `iosApp/iosApp/Info.plist` currently contains **49** SKAdNetworkIdentifier entries. Comparing against Google's official list of 50 identifiers (last updated January 30, 2026), the missing entry is:

| Network | Identifier | Status |
|---------|-----------|--------|
| BidMachine | `wg4vff78zm.skadnetwork` | **MISSING** |

**Decision**: Add `wg4vff78zm.skadnetwork` to the existing SKAdNetworkItems array.
**Rationale**: Google's official documentation explicitly lists this identifier. Missing it means BidMachine cannot attribute conversions, reducing ad revenue potential.
**Alternatives considered**: None — this is a straightforward additions based on Google's canonical list.

## Research Task 2: Google Mobile Ads SDK Version Prerequisite

**Question**: Does the current SDK version meet the >= 7.64.0 prerequisite for SKAdNetwork support?

**Finding**: 
- iOS: Google Mobile Ads SDK **>=12.12.0** via Swift Package Manager (from `swift-package-manager-google-mobile-ads`)
- iOS: Google UMP SDK **>=3.0.0** via Swift Package Manager
- Android: Play Services Ads **23.6.0** (via Gradle)
- KMP wrapper: basic-ads **0.2.7**

**Decision**: Prerequisite is satisfied (12.12.0 >> 7.64.0). No SDK version change needed.
**Rationale**: The minimum version requirement was introduced with iOS 14's SKAdNetwork support. Current SDK is many major versions ahead.
**Alternatives considered**: Upgrading to latest SDK — not needed for this feature, can be done independently.

## Research Task 3: SKAdNetwork Identifier Source

**Question**: What is the authoritative source for the SKAdNetwork identifiers list?

**Finding**: Google maintains the official list at:
- **Primary**: https://developers.google.com/admob/ios/3p-skadnetworks
- **Last updated**: January 30, 2026
- **Total identifiers**: 50 (1 Google + 49 third-party buyers)

The complete list of 50 identifiers from Google (in order):

| # | Network | Identifier |
|---|---------|-----------|
| 1 | Google | `cstr6suwn9.skadnetwork` |
| 2 | Aarki | `4fzdc2evr5.skadnetwork` |
| 3 | Adform | `2fnua5tdw4.skadnetwork` |
| 4 | Adikteev | `ydx93a7ass.skadnetwork` |
| 5 | Amazon | `p78axxw29g.skadnetwork` |
| 6 | Appier | `v72qych5uu.skadnetwork` |
| 7 | AppLovin | `ludvb6z3bs.skadnetwork` |
| 8 | Arpeely | `cp8zw746q7.skadnetwork` |
| 9 | Basis | `3sh42y64q3.skadnetwork` |
| 10 | Beeswax.io | `c6k4g5qg8m.skadnetwork` |
| 11 | Bidease | `s39g8k73mm.skadnetwork` |
| 12 | BidMachine | `wg4vff78zm.skadnetwork` |
| 13 | Bigabid Media | `3qy4746246.skadnetwork` |
| 14 | Chartboost | `f38h382jlk.skadnetwork` |
| 15 | Criteo | `hs6bdukanm.skadnetwork` |
| 16 | Digital Turbine DSP | `mlmmfzh3r3.skadnetwork` |
| 17 | i-mobile | `v4nxqhlyqp.skadnetwork` |
| 18 | InMobi | `wzmmz9fp6w.skadnetwork` |
| 19 | ironSource Ads | `su67r6k2v3.skadnetwork` |
| 20 | Jampp | `yclnxrl5pm.skadnetwork` |
| 21 | LifeStreet Media | `t38b2kh725.skadnetwork` |
| 22 | Liftoff | `7ug5zh24hu.skadnetwork` |
| 23 | Liftoff Monetize | `gta9lk7p23.skadnetwork` |
| 24 | LINE Ads Network | `vutu7akeur.skadnetwork` |
| 25 | Mediaforce | `y5ghdn5j9k.skadnetwork` |
| 26 | Meta (1 of 2) | `v9wttpbfk9.skadnetwork` |
| 27 | Meta (2 of 2) | `n38lu8286q.skadnetwork` |
| 28 | MicroAd | `47vhws6wlr.skadnetwork` |
| 29 | Mintegral / Mobvista | `kbd757ywx3.skadnetwork` |
| 30 | Moloco | `9t245vhmpl.skadnetwork` |
| 31 | Opera | `a2p9lx4jpn.skadnetwork` |
| 32 | Pangle | `22mmun2rn5.skadnetwork` |
| 33 | Persona.ly Ltd. | `44jx6755aq.skadnetwork` |
| 34 | PubMatic | `k674qkevps.skadnetwork` |
| 35 | Realtime Technologies GmbH | `4468km3ulz.skadnetwork` |
| 36 | Remerge | `2u9pt9hc89.skadnetwork` |
| 37 | RTB House | `8s468mfl3y.skadnetwork` |
| 38 | Sift Media | `klf5c3l5u5.skadnetwork` |
| 39 | Smadex | `ppxm28t8ap.skadnetwork` |
| 40 | StackAdapt | `kbmxgpxpgc.skadnetwork` |
| 41 | The Trade Desk | `uw77j35x4d.skadnetwork` |
| 42 | Unicorn | `578prtvx9j.skadnetwork` |
| 43 | Unity Ads | `4dzt52r2t5.skadnetwork` |
| 44 | Verve | `tl55sbb4fm.skadnetwork` |
| 45 | Viant | `c3frkrj4fj.skadnetwork` |
| 46 | Yahoo! | `e5fvkxwrpn.skadnetwork` |
| 47 | Yahoo! Japan Ads | `8c4e2ghe7u.skadnetwork` |
| 48 | YouAppi | `3rd42ekr43.skadnetwork` |
| 49 | Zemanta | `97r2b46745.skadnetwork` |
| 50 | Zucks | `3qcr597p9d.skadnetwork` |

**Decision**: Use Google's official list as the single source of truth.
**Rationale**: Google maintains and updates this list with participating buyers. Following Google's canonical list ensures maximum ad attribution coverage.
**Alternatives considered**: Using a third-party aggregator list (e.g., SKAdNetwork.org) — rejected because Google's official list is the recommended source for AdMob integrations.

## Research Task 4: Impact on Android

**Question**: Does SKAdNetwork affect Android builds?

**Finding**: SKAdNetwork is an Apple-only technology for iOS 14+. It has no impact on Android builds. Android uses its own attribution mechanisms (Google Play Install Referrer API, etc.).

**Decision**: No Android changes needed.
**Rationale**: SKAdNetwork is exclusively an iOS/iPadOS framework.
**Alternatives considered**: N/A.

## Research Task 5: Widget Extension Info.plist

**Question**: Do widget extensions or notification service extensions also need SKAdNetworkItems?

**Finding**: The `LaunchWidget` extension and `NotificationServiceExtension` have their own targets but do not serve ads. SKAdNetworkItems only need to be in the main app's Info.plist because ad SDK initialization happens in the main app process.

**Decision**: Only update the main app's Info.plist (`iosApp/iosApp/Info.plist`).
**Rationale**: Extensions don't load ads and don't need attribution identifiers.
**Alternatives considered**: Adding to all plists — rejected as unnecessary.

## Summary of Findings

| Item | Status | Action |
|------|--------|--------|
| SDK Version Prerequisite (>=7.64.0) | ✅ Satisfied (12.12.0) | None |
| SKAdNetworkItems count | 49 of 50 | Add 1 missing |
| Missing identifier | `wg4vff78zm.skadnetwork` (BidMachine) | Add to Info.plist |
| GADApplicationIdentifier | ✅ Present | None |
| Android impact | None | None |
| Widget/Extension plists | Not needed | None |

**All NEEDS CLARIFICATION items resolved.** No blockers identified.
