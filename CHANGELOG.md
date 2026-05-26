# [5.31.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.30.1...v5.31.0) (2026-05-26)


### Bug Fixes

* **notification:** simplify default notification settings and fix bug with background notification filtering ([7e364ee](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/7e364ee6e7058566e7bf6e19cf8641667790d21e))
* **subscription:** guard against RC uninitialized state, preserve wasEverPremium on clear ([269dcad](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/269dcad516e3d41de674c1501e52d29d179f9b97))
* **subscription:** persist trial state across app restarts ([45ca39d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/45ca39d95e5046f28b87e7a1c15fa7b6434b5662))
* **subscription:** restore RC user ID display via BillingManager.getAppUserId() ([74fe33f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/74fe33fe9891df9513b04ed42632377578d0d1e5))
* **subscription:** retry sync on cold start when needsSync=true ([70c2a31](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/70c2a31f0c7f6bd7b0908c61e9e7d2f2c2aa7060))
* **tests:** add unit tests for V5 notification payload parsing ([3664533](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3664533110b33defebacc92e8ccfbd437145c24c))


### Features

* Implement V5 Custom and News Notifications for KMP App ([78353fa](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/78353fa705c91635ec1102fcea5ab9fdb7bf6149))
* **notification:** enhance analytics tracking for notification delivery outcomes ([d21ae44](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d21ae444b5d1fad9658bd0f0baa5b251d2eb1afe))
* **tasks:** add task to start ADB server with detailed presentation options ([e8c2b3b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e8c2b3bafe30c7599bf9d6a48671f4e63e845a57))



## [5.30.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.30.0...v5.30.1) (2026-05-15)


### Bug Fixes

* update onboarding page count and improve padding for wear screens ([bb2ebfc](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/bb2ebfc2447cd2ac4de0de8f6294fe4b8a3be4ce))



# [5.30.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.29.1...v5.30.0) (2026-05-13)


### Bug Fixes

* add AlternateVideosCard component for displaying alternative video links ([9a96cd6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9a96cd6091f86ede0dc169e1999e2f0a146f5609))
* add live theme preview and seed color picker ([32b8446](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/32b8446ce1e0f88a58709e9d36086a6d36fbfaea))
* add support button with to home page ([dc90230](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/dc90230f3debf95d9060c85186642ccf4cc1a9a6))
* enhance map loading handling and update dependencies ([392f94e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/392f94effe7632ad4d297ee55ada8789a2a87d0b))
* optimize color picker interaction to reduce flicker during dragging ([76ff7d5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/76ff7d5c088aa1c35959b948135d091f710b6147))
* reorder onboarding pages for improved flow ([99dc56f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/99dc56fc9d917df3b452dc298fd867ee59243480))
* **ui:** add horizontal padding to LaunchListContent for improved layout ([3f95ee7](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3f95ee7d4b7208e0f3a9a7e5429b2ae226c8b4dd))
* update ad content rating to MAX_AD_CONTENT_RATING_G for Android and iOS configurations ([9d7e274](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9d7e274a72b6307b37191e9997f35aadb67b2e9f))
* update to latest m3 expressive animations ([ec87bef](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ec87bef410143863f5aaaf7e793c80221d929249))


### Features

* **analytics:** add rewarded-ad / temp-access events ([b3066d7](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b3066d7f1a666d547c0c7da7e7a24bd2c8dc99e9))
* **analytics:** instrument rewarded-ad / temp-access flow ([a91d24e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a91d24eb8e27363bacb82d9f941cfacb9e4e4895))
* **android:** forward FCM token to RevenueCat on startup ([ef3f372](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ef3f372e4e313cd2ff54d7226ba4395989b64602))
* **android:** start RevenueCatAttributesSyncer after billing init ([09175b3](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/09175b33b898efe13ec5804e9ededc2fa96b6f13))
* **billing:** add RevenueCatAttributes wrapper ([f4915b6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/f4915b64c3fdc21515760411e865782078c139c9))
* **billing:** add RevenueCatAttributesSyncer with debounced flow push ([cb72a56](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/cb72a56909e0035c18d56da04c4734222fac68c6))
* **billing:** add temp-access + ad-shown counters in DataStore ([327dc9b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/327dc9bec7ac83df94c39b15b2b6bcc990fc8ce0))
* **ios:** forward APNS token to RevenueCat ([a723eb2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a723eb21cc107aee7997f516e0298dfbb7c189be))
* **ios:** start RevenueCatAttributesSyncer from AppDelegate ([2d01b92](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2d01b92e84d956776fde2fc9004ce9391e3b2926))
* **platform:** add AppEnvironmentInfo for analytics/RC attrs ([fc0f442](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/fc0f442b187b7be6b285b1e5d96009a585fe7e5b))



## [5.29.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.29.0...v5.29.1) (2026-05-04)


### Bug Fixes

* update versioning scheme for consistency across platforms ([3572926](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3572926838e5471052ca52101553edae5ff3283f))



# [5.29.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.28.11...v5.29.0) (2026-05-04)


### Features

* **wear:** fix text layout sizes for large text settings ([c63fec1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c63fec1804aef682fe4aecb384c38e95c7cc1477))



