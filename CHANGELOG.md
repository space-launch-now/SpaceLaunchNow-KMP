# [5.45.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.44.4...v5.45.0) (2026-10-02)


### Features

* **ads:** load a fresh banner for each launch detail visit ([dd11a9c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/dd11a9c845b5cf662de9eb9f82f56e242fa55961))



## [5.44.4](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.44.3...v5.44.4) (2026-10-02)


### Bug Fixes

* **ads:** raise max ad content rating from G to PG ([c94a660](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c94a6608ea2f1b637ad47ce2325d00536a57a3ac))



## [5.44.3](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.44.2...v5.44.3) (2026-10-01)


### Bug Fixes

* **android:** clear WebView accessibility focus before release ([1bf2038](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/1bf203889782c40c7b7210690aaf329b927c96dc))
* **billing:** initialise subscription repository at most once ([d2967d8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d2967d877900a503ff0393e37140e8f6dad1c4ae))
* **billing:** retry subscription init when billing initialisation fails ([c429f95](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c429f95dab9065789c75eae6ca171a3c5c0cb99f))
* **ios:** avoid competing crash handlers ([a407c62](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a407c62afd41a5a86fe04ac29ece34a2738d8d94))
* **ios:** collapse duplicate Datadog user-context writes ([6afef87](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/6afef87521dae00c48553cc5f28478807e7544ac))
* **ios:** report Crashlytics non-fatals at Error and above only ([2dde315](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2dde3154c6a75e07cfdddb3233c59923510ab277))
* **ios:** serialise billing init and skip cached CustomerInfo after failed sync ([518dabb](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/518dabb8e8535f5c2becb6d614aea06a92425718))
* **logging:** collapse duplicate Datadog user-context writes on Android ([38bd584](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/38bd584d6e6d28cf546bff31ae1165fb9b113f57))
* **news:** stop reporting cache-rescued SNAPI errors as non-fatals ([639794b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/639794b6a76275e4fc90b9a7b6f730a58b7861ef))
* route space station articles through repository ([83f6b7f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/83f6b7f5288b236cbf20462f541836f4fd55ff6c))
* **ui:** cancel in-flight pagination when rocket, astronaut or agency list reloads ([457b4ad](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/457b4ad949ab88f0cdfd8168a352949ed52aad60))
* **ui:** dedupe paginated appends in rocket, astronaut and agency lists ([3316dda](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3316ddaf0256c940d1cb9cfd4ba14ec668bfe8da))
* **ui:** keep full news card layout for domain article summaries ([097a087](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/097a087b35b6ad7d5d0549b1c48e795e37ef3c0f))



## [5.44.2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.44.1...v5.44.2) (2026-10-01)


### Performance Improvements

* **api:** round time filters and sort id filters for cacheable URLs ([2dce1e6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2dce1e6a315c95ebdcec787b88f0e294686b41e1))



## [5.44.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.44.0...v5.44.1) (2026-08-31)


### Bug Fixes

* **ads:** keep banner mounted in AdState.SHOWN instead of tearing it down ([#179](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/179)) ([8e72425](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/8e72425e41c5ceb4e9ae160afef5a745876ab65b))
* **articles:** fail non-2xx SNAPI responses into the stale-cache fallback ([#190](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/190)) ([cc1cc7b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/cc1cc7b6fdd29bae07ff2636a01b7ef4746f1346))
* **ci:** queue production releases so a newer push never cancels an in-flight Play upload ([37110a9](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/37110a9e7f8b574dfa73d4272fd8ffb2339d5cb6))
* **ios:** make setRevenueCatPushToken non-throwing at the ObjC boundary ([#187](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/187)) ([c7e296f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c7e296f8536240130234b2aa5e4b04b827390339))
* **ios:** skip ad preload while the root view controller is unattached ([#168](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/168)) ([b28832b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b28832bda6829d511f80a3ea601c2645f93f2090))
* **ui:** route LaunchVideoPlayer link opens through openUriSafely ([#194](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/194)) ([deaf213](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/deaf21365f35eb840a0b2d4b438ba8e1fd98c413))



