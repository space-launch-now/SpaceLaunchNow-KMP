# [5.32.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.31.2...v5.32.0) (2026-06-02)


### Features

* **notifications:** open news notifications in an in-app NewsDetail WebView ([57a4699](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/57a4699e532a10b617c23ecd30ab20a2d462dc69))



## [5.31.2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.31.1...v5.31.2) (2026-05-28)


### Bug Fixes

* **api:** update datamodels from ll schema change ([886e4ac](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/886e4acabec7a266a2ac0485d4bdc134298e26fa))
* **tests:** update LaunchMappersTest to use testPadDetailed for pad field ([37f2fbd](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/37f2fbdf8a56aa571f57c72a1f67d09edae4fc6f))



## [5.31.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.31.0...v5.31.1) (2026-05-26)


### Bug Fixes

* **version:** update build number to 73 ([1925540](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/192554040721fa9b95feebe2a9b0777faa74b8ec))



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



