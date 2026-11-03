## [5.50.2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.50.1...v5.50.2) (2026-11-03)


### Bug Fixes

* **ads:** drop banner chrome before hiding the ad on narrow slots ([e596bfd](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e596bfd1adeb87f5f41826f973b99698bf657486))
* **ads:** host Android banners at their loaded size to meet AdMob ad-frame policy ([4a1833d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4a1833d420c17b0d623772c41d517433f2e6f3d1))



## [5.50.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.50.0...v5.50.1) (2026-11-03)


### Bug Fixes

* **api:** keep the LL2 request URL out of non-2xx exception messages ([ce69034](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ce69034935b047948a4b5fd72a62d3c672a496a3)), closes [#240](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/240)
* **api:** raise ResponseException for non-2xx LL2 responses ([d705c9e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d705c9e86f9503a2ac4ee5bfd9b0053bf9d687dc)), closes [#190](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/190) [#240](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/240)
* **astronaut:** use a flower icon on the Died tile, not a cake ([5834abb](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5834abb080dd5f94342861e34278b131a853102c))
* **config:** stop recreating the Remote Config session on every fetch ([e50ff46](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e50ff46fc3cefb6c05f5a6bc524a43240713266d)), closes [#247](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/247)
* **data:** fall back to stale cache when a 200 LL2 response fails to deserialize ([62f5022](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/62f5022b9aaef2a92b706eefcbb75eadaf3ca83b)), closes [#240](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/240)
* **data:** guard remaining LL2 launch repository call sites against non-2xx responses ([8c6fb3d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/8c6fb3d4923502906234192bca37a296bca12065)), closes [#240](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/240)
* **data:** serve stale cache when LL2 returns non-2xx for featured and in-flight launches ([d378e75](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d378e75f3d4f9b40fa2b7d3c57c3755224607cc5)), closes [#240](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/240) [#240](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/240)



# [5.50.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.49.0...v5.50.0) (2026-10-20)


### Features

* **logging:** emit push_summary after the app-start reconcile with toggle state ([1986654](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/1986654e58db0ae428db8486cd024ce25c8610bf))



# [5.49.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.48.1...v5.49.0) (2026-10-18)


### Features

* **logging:** add DiagnosticEvents and push_reconcile event with failure classes ([4658be5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4658be5e978585f0f6f4d87810a54d4cf56adee2))
* **logging:** emit filter_decision and trim iOS notification log noise ([b7c5b8a](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b7c5b8ae7e9365c26a44c6a0469f0193e2232adb))
* **logging:** emit notification_displayed and trim Android push log noise ([c6e5100](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c6e51005d8d7f43ddf2a45da5bac7cfbbc5a8f6f))
* **logging:** route iOS startup/NSE events through DiagnosticEvents and pass POST_APNS trigger ([2f527bc](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2f527bc0f90dfc3fe6887f9207b2eaf9bba3a8dc))



## [5.48.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.48.0...v5.48.1) (2026-10-13)


### Bug Fixes

* **logging:** cap Ktor HTTP logging at INFO so API bodies never ship ([53483e5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/53483e5d3dc95ad939cb78a862b9226b4fcfd3ae))
* **logging:** collapse purchase-state, widget, feature-gate and ad-size trace bursts ([d57834e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d57834e9e03493c5e67b69831432d905e2b7b8a0))
* **logging:** demote billing activity reflection misses to debug ([a4c193c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a4c193ca0ab332c63eab87da720f6e1267d07d0d))
* **logging:** drop Datadog logger init line and demote consent and user-set lines ([7b0b629](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/7b0b62995c4f398c6cd41a77e5c143dc6ba77100))
* **logging:** rethrow coroutine cancellation before logging in catch-alls and warn on remote config backend failures ([d4ae003](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d4ae0030852ec8506cd42ebfcfe416b786dd967d))
* **subscription:** ignore lastSynced when verifying the storage read-back ([0f2ce7c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/0f2ce7c2f997e8e54ea9f2bcc272de851f8bb6c7))
* **wear:** skip watch sync once the Wearable API is missing instead of logging errors ([8c1fdd0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/8c1fdd0f541f7e9186e0eddfc71add43af317fa2))



