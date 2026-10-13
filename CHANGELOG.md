## [5.48.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.48.0...v5.48.1) (2026-10-13)


### Bug Fixes

* **logging:** cap Ktor HTTP logging at INFO so API bodies never ship ([53483e5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/53483e5d3dc95ad939cb78a862b9226b4fcfd3ae))
* **logging:** collapse purchase-state, widget, feature-gate and ad-size trace bursts ([d57834e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d57834e9e03493c5e67b69831432d905e2b7b8a0))
* **logging:** demote billing activity reflection misses to debug ([a4c193c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a4c193ca0ab332c63eab87da720f6e1267d07d0d))
* **logging:** drop Datadog logger init line and demote consent and user-set lines ([7b0b629](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/7b0b62995c4f398c6cd41a77e5c143dc6ba77100))
* **logging:** rethrow coroutine cancellation before logging in catch-alls and warn on remote config backend failures ([d4ae003](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d4ae0030852ec8506cd42ebfcfe416b786dd967d))
* **subscription:** ignore lastSynced when verifying the storage read-back ([0f2ce7c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/0f2ce7c2f997e8e54ea9f2bcc272de851f8bb6c7))
* **wear:** skip watch sync once the Wearable API is missing instead of logging errors ([8c1fdd0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/8c1fdd0f541f7e9186e0eddfc71add43af317fa2))



# [5.48.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.47.0...v5.48.0) (2026-10-12)


### Features

* **detail:** add Share button to phone launch detail hero ([4489156](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/448915620beb9b0734959f0d2d8b8125357dbb70))



# [5.47.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.46.2...v5.47.0) (2026-10-09)


### Bug Fixes

* **ads:** load new inline slot handlers at 300x250, not the library default ([619f183](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/619f18329e33e312e4bb9f60a524ddac4de15ab5))


### Features

* **schedule:** drop the inline ad after row 4, keep only page-end ads ([33f6be9](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/33f6be98e0ce4e5e0173bb982c5c1a8f7c4cea96))
* **schedule:** show 300x250 inline ads after row 4 and each loaded page ([24a0a7b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/24a0a7b2fe420d2017821275e9dab8127e3abfd2))



## [5.46.2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.46.1...v5.46.2) (2026-10-06)


### Bug Fixes

* **ios:** reference UIApplicationStateActive through its enum class ([3303b56](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3303b56dc09f54f0b4300367861324f719bb2660))



## [5.46.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.46.0...v5.46.1) (2026-10-05)


### Performance Improvements

* **api:** round LL time filters to 1 minute ([#230](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/230)) ([35ba2ee](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/35ba2ee25ae33e0a9e110b1a386523e2fdbaf70f))



