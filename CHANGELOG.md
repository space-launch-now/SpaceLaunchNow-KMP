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



# [5.33.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.32.0...v5.33.0) (2026-06-03)


### Features

* **navigation:** add article click handling to navigate to NewsDetail ([5dc8510](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5dc8510fc410af6ed0d3b76f1174fd9272077b25))



# [5.32.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.31.2...v5.32.0) (2026-06-02)


### Features

* **notifications:** open news notifications in an in-app NewsDetail WebView ([57a4699](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/57a4699e532a10b617c23ecd30ab20a2d462dc69))



## [5.31.2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.31.1...v5.31.2) (2026-05-28)


### Bug Fixes

* **api:** update datamodels from ll schema change ([886e4ac](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/886e4acabec7a266a2ac0485d4bdc134298e26fa))
* **tests:** update LaunchMappersTest to use testPadDetailed for pad field ([37f2fbd](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/37f2fbdf8a56aa571f57c72a1f67d09edae4fc6f))



