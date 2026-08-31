## [5.44.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.44.0...v5.44.1) (2026-08-31)


### Bug Fixes

* **ads:** keep banner mounted in AdState.SHOWN instead of tearing it down ([#179](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/179)) ([8e72425](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/8e72425e41c5ceb4e9ae160afef5a745876ab65b))
* **articles:** fail non-2xx SNAPI responses into the stale-cache fallback ([#190](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/190)) ([cc1cc7b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/cc1cc7b6fdd29bae07ff2636a01b7ef4746f1346))
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



## [5.42.3](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.42.2...v5.42.3) (2026-08-27)


### Bug Fixes

* **newsevents:** clear the load-more flag on every reload path ([4b9bd96](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4b9bd96ad91ea12f633a3e56552421af370347ec))
* **newsevents:** clear the load-more flag when a fetch is cancelled ([e73a4c4](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e73a4c4c19b754897e45a67b8166a19cffe82168))
* **newsevents:** dedupe paginated appends and close load-more race ([fc86705](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/fc86705bb1ca8acb04eb1d9032bf9bd008de3297)), closes [#182](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/182) [#179](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/179)



## [5.42.2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.42.1...v5.42.2) (2026-08-27)


### Bug Fixes

* **android:** make WorkManager init non-fatal by removing startup auto-init ([1a49905](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/1a49905e56b5d7072918ae59a3de10aaba814da5)), closes [#181](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/181)
* **wear:** make WorkManager init non-fatal by removing startup auto-init ([73e6a00](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/73e6a0056033140fbaa0efada171c46004b6a8c1)), closes [#181](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/181)



