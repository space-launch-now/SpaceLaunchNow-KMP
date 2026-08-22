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



# [5.38.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.37.1...v5.38.0) (2026-08-18)


### Bug Fixes

* **notifications:** align topic names with the V6 contract (isroAgency, india, dedupe other IDs, drop the stale type list) ([2783ad2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2783ad29596b9361d30b2b520b2a33e87131a3d0))
* **notifications:** detach the app-start reconcile so it cannot block startup or wipe loaded state ([cc73336](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/cc73336b15b51e4e13e183cfece8fcba2e844f5a))
* **notifications:** persist the changeover flag from storage and move reconcile I/O off the main dispatcher ([3f97dd3](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3f97dd368ecd290be02e4148d343edde0bb0234e))


### Features

* **notifications:** add the Other Agencies settings row for the otherAgency catch-all ([2a7fd1a](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2a7fd1adce55f268855d8fdadf625c3d8b0147bc))
* **notifications:** add the TopicSubscription ledger table and store (schema v11) ([5f1d577](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5f1d5778781dccb2330b7a6b1e0cb4101326c855))
* **notifications:** add the V6 reconciler with unsubscribe-first ordering and per-row FCM outcomes ([3a7f8dd](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3a7f8dd94733fc822ea15f715b68a2165a319a1a))
* **notifications:** derive the V6 topic set as a pure function ([9e8dca5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9e8dca5d4183cbc8837211093688225b68b6cfa9))
* **notifications:** exclude the subscription ledger database from platform backup on both platforms ([04800e1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/04800e159e44a026e13a4434340309b6388d06af))
* **notifications:** log the V5-to-V6 changeover durably - analytics event, diagnostics row, named failing topics ([d4a1177](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d4a1177768eb9fb4ed8d1526fb314d5320b7c6fc))
* **notifications:** make the notification filters screen save-based with a single reconcile per save ([89cdacc](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/89cdacc8f78d8e7476f97a9535bd6007d2fb8f9f))
* **notifications:** persist the V6 changeover flag and pin changeover and reset behaviour ([fd5ec5a](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/fd5ec5a4c66fb3eabc37cbca3eefec3249dc0399))
* **notifications:** reconcile on Android token refresh and drop the iOS k_debug_v4 auto-subscribe ([53cc828](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/53cc828a74d64eec28cfb9d18fd09ade7fbf050d))
* **notifications:** surface the V6 subscription ledger and a resubscribe-from-scratch action in diagnostics ([6998703](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/69987031c51876df33d769d2e5c11e314290edbb))
* **notifications:** vendor the V6 topic contract with a desktop conformance test ([bec2aaa](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/bec2aaa4427473263bcb9edf503d9bc5ed307fc5))
* **notifications:** wire the V6 reconciler through the repository and retire the V5 subscribe trigger ([5067b13](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5067b13b4fe5223211e9159d967cdc17abc20800))



