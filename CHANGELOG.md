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



# [5.44.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.43.0...v5.44.0) (2026-08-31)


### Bug Fixes

* adjust card height and subtitle lines for custom messages in PinnedContentCard ([8f22237](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/8f22237867177186b3638d1365a8be6839c5f573))


### Features

* full-bleed hero header with agency logo on launch detail ([4e96eae](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4e96eae76c9b17754d0acb88c76c68866277efdc))



# [5.43.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.42.3...v5.43.0) (2026-08-31)


### Bug Fixes

* **analytics:** guard subscriber paywall dismissals and dual-pipeline permission results ([be543f3](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/be543f3dce7ff76baf4161dd856214c7b39fc9e0))
* **config:** rethrow cancellation so fetch timeouts are not swallowed as failures ([ab7e979](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ab7e9798c9d6a0b5f73afc90b36d3aeaf5010048))
* request notification permission from settings when the iOS dialog was never shown ([a73e80e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a73e80e2367c26103ca0a21bf03b1ca6ba823708))
* **ui:** guard alternate video link opens against startActivity crash ([4a14399](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4a14399774aa14f1d4d8681590b1c8d155de8c6a)), closes [#180](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/180)
* **widgets:** keep widget refresh running while offline ([1e0d2b6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/1e0d2b620926cb57c2059ddaf6005816656ef9ba)), closes [#170](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/170)


### Features

* **analytics:** add paywall_dismissed with time-on-screen ([676495e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/676495e26e0b872f2c971cef246ed7393e726f81))
* **analytics:** attribute purchase events to their paywall source ([bd4c35b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/bd4c35bdda090b97df4098ddc47558b9e344b973))
* **analytics:** instrument onboarding paywall tier taps, source, and dismissal ([0f493f8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/0f493f8a95eb7a904d8fc77fd56e2fccbb140826))
* **onboarding:** add OnboardingVariant model, storage, and remote config plumbing ([5d16513](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5d16513fd9ca75c7af04d80953d7e771b3be6cb7))
* **onboarding:** gate preload navigation on onboarding variant fetch ([aeaac00](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/aeaac003b554eb5f87982e802884ab05294c845e))
* **onboarding:** variant-driven pager with page-level and permission-outcome analytics ([039011e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/039011eb70277674db48adbba006bbd2ff94eee2))



