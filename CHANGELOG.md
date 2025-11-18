# [5.0.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.0.0-b22...v5.0.0) (2025-11-18)


### Bug Fixes

* correct month off-by-one error in iOS date formatting ([810426e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/810426e79dd34a5c871e09aae5d6c1412d73559f))
* Improve ad handling ([c7cb537](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c7cb537c1d8551ecf2e86a6c8ff3e62b3e8cf717))
* **ios:** fix date formatting ([5dc3b3a](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5dc3b3a9bd98cd7695b38ef7d1828d35bc287cfb))
* refresh related news on pull-to-refresh ([8cc4f27](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/8cc4f278f6051b7caa1ffb8de3c5247efbf59037))
* show date and time for docking/departing events instead of just time ([ffc4c16](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ffc4c169eaa2799f103b8bd60f5d7d29726c8480))
* update Koin module to inject ArticlesRepository into LaunchViewModel ([0fc0788](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/0fc078821e7f98ffdf81ef94d616ee939af08498))


### Features

* Add cache and busting ([53387f5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/53387f5fd92de4d65d1b4b21f530b7cf3e729eba))
* add related news to launch detail page - initial implementation ([4641987](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/464198795362f332a4d2a28e995faf7997246472))
* add retrieve related articles and improve ad handling ([5c534a7](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5c534a7421cf3878e35f7d540f287b38cb3cae7c))



# [5.0.0-b22](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.0.0-b21...v5.0.0-b22) (2025-11-13)


### Bug Fixes

* datadog integration and iOS build ([4f43a7a](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4f43a7a6c8705bcf46053d674b9f6d158af21c99))
* implement fixes for merge conflict ([2d1c471](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2d1c471f8c75b177493b07fe2860a44239a41f66))
* resolve Kotlin build configuration issues ([6a6ef6a](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/6a6ef6a0f4bd10b9bb7d4b196301c5bc77fa25f6))


### Features

* add SQLDelight database for persistent caching ([2f36576](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2f36576187c1325c2f8bc45bcf928e4fef40a954))
* Enhance billing and subscription initialization for iOS and Android ([c6c8326](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c6c8326728d8ddbb0dfb96017126899735975e6f))
* implement cache-first repository pattern with SQLDelight ([1ffde72](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/1ffde72d232604398f99e3dccb8a00856365ceba))
* parse event duration field using common utility function ([3fb6b43](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3fb6b43a5466666cb760f80c56338de893a607b0))



# [5.0.0-b21](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.0.0-b20...v5.0.0-b21) (2025-11-10)


### Bug Fixes

* improve URL removal regex to match all bracket contents ([5e89f77](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5e89f7719d739e92cd9a16bea118f192254add29))


### Features

* add script to truncate Google Play release notes to 500 chars ([d101d4f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d101d4f90dc9ae49ef2947438205e15fa06f57f8))



# [5.0.0-b20](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.0.0-b19...v5.0.0-b20) (2025-11-09)


### Bug Fixes

* add launcher config image ([a7028f6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a7028f6334fd1e44a9b7d0ffbc48292fee676260))
* date handling and locale utilities ([16a32b6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/16a32b68493e7082970a355587a9d871be090ee2))
* enhance ad display logic to prevent showing ads to premium users ([f146193](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/f14619308149a0a270cfafdad4daaa12dd588dd0))
* improve fullscreen video controls and navigation visibility ([95c49b1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/95c49b1fe9ad2dba76573b4a29f5d9a14685a117))
* improve landing details rendering ([d2766b8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d2766b839befbc6f3fbc25678942fa795e116609))
* improve notification dates format ([ed4e452](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ed4e45218a7f21066b117b05e685dd5663e146f8))
* notification icon resource ([d208435](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d2084350369bbeb394323361df2f39719872610b))
* remove totalLaunchCount from RocketListView (not available in LauncherConfigNormal) ([a63030e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a63030ec8182ab53b3ba92762616c2f7b9bd98c4))
* update environment variable name for TOTP secret ([ae59dc1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ae59dc124a5ba8fd6fd35557199c31147544db17))


### Features

* add Agency Detail Screen with ViewModel and Views ([ed6260e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ed6260e96fb8da6362c72ca35b60195482cbae7a))
* add navigation to rockets screen from settings and tablet layout ([4ec33cc](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4ec33ccef74e12f4261e3a9ca9ea5612cf1de33d))
* add refresh and time cache bust to detailed view ([a67ec07](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a67ec07cbf1ce1e9d474c9bc98ccca703a094364))
* add rocket list and detail screens with API integration ([e2f84b7](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e2f84b7f9d6f23b1c37df8448a59c4837619768f))
* enhance screen rotation handling ([9e7fc7c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9e7fc7ce3cc3bcb9a6338ee7e2d8818065abaf8a))
* implement WorkManager for background notification processing and enhance logging ([a4b142d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a4b142d4cf8af25619e0abab32d571a83dc9ccf6))
* improve event details and launch details ([9d6bfd0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9d6bfd0835a99ec7142c77651747dc836095afd6))
* improve schedule view experience ([35acdf9](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/35acdf9bdfbc0bfa71eae6a51c929698c5034545))
* integrate Agency Detail Screen into navigation ([688f5ed](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/688f5ed5bfba84a65c79eda3450f346d1a39a063))



# [5.0.0-b19](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.0.0-b18...v5.0.0-b19) (2025-11-04)


### Bug Fixes

* remove DirectBillingClient implementation and related code roll back to older Android billing library ([71019d7](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/71019d7a268bd2a9dce3f091fb7b851cd64d8604))



