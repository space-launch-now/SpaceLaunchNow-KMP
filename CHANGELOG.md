## [5.23.2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.23.1...v5.23.2) (2026-03-31)


### Bug Fixes

* **timeline:** enhance TimelineCard to support NET and improve event status visualization ([30ccba6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/30ccba637085c3b80fcc9f6fba5fbb4db089f67d))



## [5.23.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.23.0...v5.23.1) (2026-03-30)


### Bug Fixes

* **datetime:** enhance timeline relative time formatting to support days in ISO-8601 durations ([3994605](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/399460551051ce866e86505fe75a3fe58175d343))
* **notifications:** implement notification filter persistence fix and error reporting ([3f8c036](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3f8c0363539e2a9759f3df0296f5170ff476c2ea))



# [5.23.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.22.4...v5.23.0) (2026-03-25)


### Bug Fixes

* deduplicate combined launches by ID to handle edge cases ([b3f2593](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b3f259372b671db25920466a1a1f899386200bcd))
* handle corrupted subscription data files gracefully ([296094b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/296094b2cafa2c5a6949395e104255dbf59ceff9))
* integrate Firebase Remote Config for dynamic roadmap feature ([4039945](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/40399455c00f2f783c972bb051c093e3f77370a2))
* **logging:** add Crashlytics setup for improved exception handling on iOS [skip ci] ([4ca0c0b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4ca0c0bc24793a3082f1ce149e181264b1f0ef70))
* pre-warming of ad requests and enhance ad handling for improved performance ([079b13f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/079b13f085464045ec5389d5878ffab8ac9c95ba))
* refactor loading indicators to use shimmer effect for improved performance on low-RAM devices ([dc3e4e5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/dc3e4e51be7b9177ff68ce9f6176e9eb05cd02f3))


### Features

* add events and news screen ([2882bd3](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2882bd33428a8698efdc5891a13c66c502b5a39a))



## [5.22.4](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.22.3...v5.22.4) (2026-03-22)


### Bug Fixes

* correct TOTP secret key name in EnvironmentManager ([c2c124d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c2c124d7ff994d523f5fb2b01aa2e2410ea3d38b))
* fix home view models for improved structure and maintainability ([357ff3e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/357ff3e9c90acbaf984aeaa3b2feb5a0c85ef4ad))
* handle billing unavailability in SubscriptionViewModel and set minimum window size in DesktopApp ([b291105](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b291105d9791be2ee012e6fd488a8542d6020114))
* implement stale-while-revalidate pattern for launch details and UI updates ([cc8a06b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/cc8a06b41ba95061be20bed0783f72b9f6acc5a6))
* integrate Firebase Crashlytics for enhanced error reporting and logging ([3145c7f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3145c7faa33acac8b73000c5d33a2a3f9c119eb3))
* integrate Firebase Crashlytics for error reporting and add test functionality in DebugSettingsScreen ([55661ae](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/55661aefd71a5ad8c21785f6ca3eff7c892d0fb8))
* update Firebase Crashlytics integration and dSYM upload script ([1dbd8c6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/1dbd8c6f1eaa3e5fe3487d994d5c163a9eb20ce7))



## [5.22.3](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.22.2...v5.22.3) (2026-03-21)


### Bug Fixes

* update launch mode to singleTop and enhance intent flags for navigation ([ef87af0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ef87af043e3257615225e9b1f71464c7dc75033c))



