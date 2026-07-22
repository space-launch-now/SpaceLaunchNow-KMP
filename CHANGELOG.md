# [5.37.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.36.0...v5.37.0) (2026-07-22)


### Bug Fixes

* **logging:** gate push summary direct upload on active diagnostics consent ([1dafbb1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/1dafbb130bc7800ba7e31289ecfc8638b87a0867))


### Features

* **logging:** add FCM/push health rows to diagnostics report ([b05d300](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b05d3008e44b3c449e1bffde0b87a1940fba45f6))
* **logging:** add Play Services availability expect/actual ([d473bec](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d473becc12c365180fc296b5cb94d8c62b44295a))
* **logging:** add PushDiagnostics runtime snapshot with summary and report builders ([8fab850](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/8fab850140e7fb1dc30a4ebe73d4a73ffee0d218))
* **logging:** add remote diagnostics config parsing and resolver ([17c8744](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/17c874494dccdddfdf72458e4d71488945bcd38c))
* **logging:** apply remote sample-rate and diagnostic-level overrides ([4a7bcd5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4a7bcd5a5efaa91b444fdfd49b753a916ba8dadf))
* **logging:** attach rc_user_id to Datadog logs and RUM on both platforms ([bb051cc](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/bb051ccc8fc108187cadeaa35e4009063fa888f5))
* **logging:** remotely control per-user diagnostics via Firebase Remote Config ([171e400](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/171e40027994f4ca7fee316f5323fcbb24fab35d))
* **notifications:** make startup push registration observable at info/warn ([d3d7ec1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d3d7ec101826b2adde8776575fda9788f57d7a84))



# [5.36.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.35.0...v5.36.0) (2026-07-09)


### Features

* enhance News & Events navigation with initial tab support and update dependencies ([df122c8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/df122c839c27fd5b7e09594c61b6e9431abce3ad))



# [5.35.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.34.0...v5.35.0) (2026-07-08)


### Bug Fixes

* remove outdated templates and add new documentation for monetization and analytics setup ([d62e39f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d62e39f5cc8b02dc58c476f6aa4eede2df7c99ad))


### Features

* add business analysis skill and references for monetization, analytics, and app store strategies ([2de719c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2de719c114f0eb80cb21dd23b1ed5344fca57ccb))
* add compact countdown display and link to full schedule in featured launches row ([46c9756](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/46c9756fc46aea903190ce15142aa9753a7c8c6d))



# [5.34.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.33.1...v5.34.0) (2026-07-07)


### Bug Fixes

* implement iOS Notification Re-Alert Policy ([1ca9844](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/1ca984435853e7591545d7fb7d8b37947852f7d5))
* **logging:** apply diagnostic level at desktop startup; correct stale startup logs ([e9c8a5d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e9c8a5dc1c3d5b609646ecdf3e0640d79cbd033b))
* **logging:** non-destructive NSE breadcrumb drain + peek + prefs snapshot ([54d4a4c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/54d4a4c5eae714f2e8153ad1c690e915053e36e3))


### Features

* **diagnostics:** ground-truth diagnostics screen with log export ([dbdcdef](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/dbdcdefc5c31d6bccf72efb1ee2f51ceea74a6a8))
* **logging:** add DiagnosticLevel with per-sink policy mapping ([16f9ab4](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/16f9ab42247ac054813eca1b06a1d492d12b9730))
* **logging:** auto-revert Verbose diagnostic level after 72 hours ([5f9484c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5f9484c83a154cad62e10d252b8fccb5dce39978))
* **logging:** on-device diagnostics file log with capped ring buffer ([fe388e5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/fe388e5904b3421a2df76b3666d104ddae768098))
* **logging:** persist DiagnosticLevel with legacy-key migration ([41378b8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/41378b888f9bfc600ae0569845df65f6728bcff1))
* **logging:** replace Datadog init gate with SDK TrackingConsent flow ([e1c7e07](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e1c7e07c0119b6a1039a83c79f807dc320be5cbe))
* **logging:** structured single-event startup state for Datadog ([acf6e4a](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/acf6e4a2259d57fdbbccd2cbf5e36e4838b925da))
* **settings:** collapse logging knobs into single Off/Standard/Verbose control ([fcef82e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/fcef82e8ebcc1c0ed8ed956bdbb584fcf7420007))
* **share:** platform sharePlainText for diagnostics export ([b5b2a20](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b5b2a20267925fbabcfb17c53c9f0f5ff8473799))



## [5.33.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.33.0...v5.33.1) (2026-06-03)


### Bug Fixes

* update ad banner placement and improve URL opening logic on iOS ([e747d22](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e747d2267dcac9a909d07883632fe673b61cb7f9))



