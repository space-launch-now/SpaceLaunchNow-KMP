## [5.42.2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.42.1...v5.42.2) (2026-08-27)


### Bug Fixes

* **android:** make WorkManager init non-fatal by removing startup auto-init ([1a49905](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/1a49905e56b5d7072918ae59a3de10aaba814da5)), closes [#181](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/181)
* **wear:** make WorkManager init non-fatal by removing startup auto-init ([73e6a00](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/73e6a0056033140fbaa0efada171c46004b6a8c1)), closes [#181](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/181)



## [5.42.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.42.0...v5.42.1) (2026-08-26)


### Bug Fixes

* **ci:** use standard runners so workflows can be scheduled ([25b14b7](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/25b14b7fb5efa0c2f55b1bd772a92e1ba2caeb81)), closes [#174](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/174) [#175](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/175) [#176](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/176) [#174](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/174) [#175](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/175) [#174](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/174) [#175](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/175)
* **claude:** correct stale Gradle test task and record toolchain limits ([c8f4880](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c8f488065b63e9ff202ebdec1acb181622c8095c)), closes [#169](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/169)
* **logging:** stop reporting coroutine cancellation as a Crashlytics non-fatal ([077f9ff](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/077f9ffdc9c749767eebaf274c7821035e15b525))
* **logging:** stop sending coroutine cancellation to Datadog as an error ([a7fbded](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a7fbded8ba157caaee30a9b9f439f03c730cc8b0))



# [5.42.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.41.2...v5.42.0) (2026-08-26)


### Features

* **claude:** add triage skill and /investigate_issue team pipeline ([d564a8d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d564a8d5608a0992a60b218d549fdfe013da0c94))



## [5.41.2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.41.1...v5.41.2) (2026-08-22)


### Bug Fixes

* **notifications:** add LocalFilterPolicy so devices on V6 topics stop re-filtering server-targeted sends ([c677ef8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c677ef8cad49bd4bf5e242e1a5d6646da088cb55))
* **notifications:** Android worker skips the legacy filter once the V6 changeover has completed ([91ad718](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/91ad718cfd24356575b6c7c1ce12f5c96f2de00d))
* **notifications:** iOS NSE and in-app paths skip the legacy filter once the V6 changeover has completed ([3f5afd6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3f5afd6ee1f2fbb07c374dd83d15d03de80f44cc))
* **notifications:** keep the iOS in-app filter cache in step with saved state ([fc07108](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/fc07108c761013d8f0d42d8634ed4598c5b54f31))



## [5.41.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.41.0...v5.41.1) (2026-08-22)


### Bug Fixes

* **notifications:** register for APNs on launch and make the iOS FCM bridge fail fast so the V6 reconcile cannot hang ([f2f10b5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/f2f10b579a456c07cc6ba84976cf029ec9bd6cba))



