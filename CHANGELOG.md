## [5.41.2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.41.1...v5.41.2) (2026-08-22)


### Bug Fixes

* **notifications:** add LocalFilterPolicy so devices on V6 topics stop re-filtering server-targeted sends ([c677ef8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c677ef8cad49bd4bf5e242e1a5d6646da088cb55))
* **notifications:** Android worker skips the legacy filter once the V6 changeover has completed ([91ad718](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/91ad718cfd24356575b6c7c1ce12f5c96f2de00d))
* **notifications:** iOS NSE and in-app paths skip the legacy filter once the V6 changeover has completed ([3f5afd6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3f5afd6ee1f2fbb07c374dd83d15d03de80f44cc))
* **notifications:** keep the iOS in-app filter cache in step with saved state ([fc07108](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/fc07108c761013d8f0d42d8634ed4598c5b54f31))



## [5.41.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.41.0...v5.41.1) (2026-08-22)


### Bug Fixes

* **notifications:** register for APNs on launch and make the iOS FCM bridge fail fast so the V6 reconcile cannot hang ([f2f10b5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/f2f10b579a456c07cc6ba84976cf029ec9bd6cba))



# [5.41.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.40.0...v5.41.0) (2026-08-21)


### Bug Fixes

* **analytics:** move funnel user-property sync to an app-scoped syncer ([b37c4cf](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b37c4cf7ec58925f05e43625225b4a73e1b59280))


### Features

* **notifications:** add Starlink mute toggle backed by the v6 starlinkMuted topic ([686ca1f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/686ca1f64dd6ec7de619fb62546c11fabd1abef6))



# [5.40.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.39.0...v5.40.0) (2026-08-20)


### Bug Fixes

* **ads:** load rewarded ads on demand instead of dead preloaded handles ([a567f0f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a567f0f7be5f835feb3400379a32db933c95e81b))
* **test:** derive expected platform from getPlatform in funnel tests ([7377235](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/7377235baa55df5c0c1de53e57fa99c4b7c353c6))


### Features

* **analytics:** add paywall_tier_selected and funnel dimensions to conversion events ([ee30910](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ee309101533888b23588e387d47c866579085e4a))
* **analytics:** add purchase_failed and notification_shown events ([66738eb](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/66738eb8b2bd231a2dbdc4333ad0399fa971b332))
* **analytics:** attach revenue and failure attribution to purchase flow ([8ccc5fe](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/8ccc5fea2ae42f8e4a8f061efdc3b395147d3154))
* **analytics:** fire notification_shown and notification_tapped at display and tap time ([7c973fc](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/7c973fc76725cd7343e2d8f177f7254e6a8ec927))
* **analytics:** instrument Support-Us paywall view and tier selection ([d49fb6a](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d49fb6af5d24576c38cd521836641c38a367bc0f))
* **analytics:** stamp funnel dimensions and dual-pipeline emission in SubscriptionViewModel ([c08639e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c08639eea31304ef9e31d52f521d91d7f9ac0f20))
* **ui:** show loading state while rewarded ad loads on demand ([f30b559](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/f30b55945cfd32751b32fb7f7dea838c3cb7dcda))



# [5.39.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.38.0...v5.39.0) (2026-08-20)


### Features

* **notifications:** anchor Save & apply to the bottom of the filters screen ([7a2c8c9](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/7a2c8c9ff3d48e6d8c1969ec64dc13d57a30cc8d))
* **notifications:** make the debug topics switch V6-aware with an immediate reconcile ([31e7c01](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/31e7c01e15110c4792f0074bee80a7ca37478c3e))
* **notifications:** sync FCM subscriptions automatically on every filter change ([58a25d6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/58a25d69855c81f637619cae27b029c7c7384bd3))



