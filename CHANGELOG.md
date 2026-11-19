# [5.52.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.51.0...v5.52.0) (2026-11-19)


### Bug Fixes

* **ci:** keep the encoding check ASCII and cover 3-byte and 4-byte damage ([decdad7](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/decdad7aae6e510d450c68c7015cee7fdfe71057))
* **onboarding:** restore paywall text damaged by wrong encoding and add a CI check ([44fefed](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/44fefed03bb2e17c57c30fed0fa2efce12c91e74))


### Features

* **filters:** add TerminusFilterList table and SQL filter option store ([deeeeb6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/deeeeb6db9002cd9935ea9197e56400eb742c11f))
* **filters:** keep Terminus filter options on disk for 7 days ([f038628](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/f0386287f1efd6d2139b0bc65c2b2a8ba792f8c9))



# [5.51.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.50.2...v5.51.0) (2026-11-19)


### Bug Fixes

* **ads:** host iOS banners at their loaded size to meet AdMob ad-frame policy ([e4921e8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e4921e85106f245d8f85f1c82d3b1d38a49c2ddb))
* **android:** declare org.apache.http.legacy so the Maps renderer loads ([20ec834](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/20ec834fd210e9007c65a65ef6cb6fb73495586b))
* **api:** generate Terminus free-form fields as JsonElement ([398cef2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/398cef22401280392e6808c5dc95ebf565c639da))
* **api:** map Terminus uri-or-empty fields to plain strings in the generated client ([5986fde](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5986fde91725b92a2453783a648f59a77e9883ba))
* **api:** restore backticks in generated Terminus SearchApi ([924d4c0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/924d4c0c742497517a6b4ad62c88d03ab3795c64))
* **api:** strip escaped backticks from generated Terminus SearchApi ([ff3c352](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ff3c352534430dbb1c93df6c712d3f0a4a9a30cc))
* **build:** align okhttp-sse with OkHttp 5 core so release R8 passes ([fa69187](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/fa69187f080a522acbc889a3c203d9aa9b71ece8))
* **config:** never create Remote Config in a process without a Firebase app ([f56ffb8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/f56ffb89c6143fc59ea7b94f7c7a1094ddda245e))
* **countdown:** hide digits once a launch has a final status ([0e406fd](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/0e406fd531cb96baa6d13c31782caefda3d97b62))
* **preload:** import month number ([9ab2a31](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9ab2a31e70653805a025778ed6573de7533e3363))
* **preload:** warm the by-day history cache with the 1-based month ([46696d9](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/46696d92d1b20b2d77906a3434000b611f910908))
* **rating:** evaluate cached launches, expire pending id, always consume prompt ([170533d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/170533d05dec6f267ea674a93ac2bdea76f26b24))
* **terminus:** add the shared 25-row page reader ([0f6f56d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/0f6f56deb4e2b04d92a60e62ef0d86c144182445))
* **terminus:** cap filter paging at 50 pages ([a46a7d2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a46a7d2ec4fd54166c364485ad2e7ab2b48df716))
* **terminus:** honour forceRefresh for mini and filtered launch lists ([69e29ac](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/69e29acee4c4f8d0e3d4a6bdf8cc524c0eab78e6))
* **terminus:** keep search text out of logs and escape list cache keys ([1baf891](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/1baf8919027c08fa801f1d1db67a9c5bed317247))
* **terminus:** keep the filter page cap and test, restore comments ([9df05e6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9df05e622f786573af0fb3231d95d09a94a1a485))
* **terminus:** leave offset 0 out of the agency search request ([27feede](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/27feede7103ff3071737dd95e2511cef3eb6db0b))
* **terminus:** read filter lists 25 rows at a time ([6459bea](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/6459bea79aaa503f3ac6be28c3dab1ba4135207e))
* **terminus:** read launches by day, Starship history and agency search 25 rows at a time ([98a946c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/98a946c7f2550095495e8bbfd115e283cadd2a43))
* **terminus:** redact list cache key from undecodable-entry log ([99c0c98](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/99c0c98c91a876acce0a2435a3fd7186f10445e4))
* **wear:** send SpaceLaunchNow-v User-Agent ([7ed5f6d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/7ed5f6d4bf78e913be355dd130630c5ed94e9dbd))
* **widget:** show the premium lock in the Next Launch widget ([0ef7ed4](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/0ef7ed4c3842c5671b87e7f0dccb5b63e3962d52))


### Features

* add Shop link row to Settings ([4802526](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4802526991e362815af3403264640cb759ebf53d))
* **api:** add Terminus launch rocket, timeline and detailed rockets endpoints ([8ef77ed](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/8ef77edcce35bc795bbbe32fec2fa6b099fb2044))
* **config:** default to Terminus with use_legacy_backend emergency switch ([9dd6ed8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9dd6ed817302be3e97b877cddf475a0e644fc619))
* **data:** read featured, in-flight, Starship, stats and by-day launches from Terminus ([1126e0f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/1126e0f60dee5d2d1a401dbc33f039d6b76c28b7))
* **data:** read filter options from Terminus behind DataBackend ([f4790cc](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/f4790ccf222c4574140b7533080bd570624e52fd))
* **data:** read launch detail from Terminus behind DataBackend ([967c647](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/967c647a461da452705756b2a176fcd5de4587f6))
* **data:** serve spacecraft and spacecraft configs from Terminus behind DataBackend ([2b6740d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2b6740d31efd7b92ae59e48eb081b71252408915))
* **data:** serve stations, astronauts and programs from Terminus behind DataBackend ([170c6b7](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/170c6b7dd0f783d0451fbeed50b4e4775e5f633d))
* **debug:** add data source switch and clear-all-caches action ([f7dc4e5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/f7dc4e5bdf6af26af664d79e528b6bbbf7bc22be))
* **debug:** Terminus prod/staging host switch in the debug menu ([bb24507](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/bb245072ba53a70169923ef5b7c8eb49355c453d))
* **di:** add Terminus API Koin module and base URL ([3f8fc02](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3f8fc020193746d4b5c03c0b6bf261d25c3068c1))
* **di:** bind vehicle repositories on DataBackend ([f0e3360](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/f0e336053c7046792378079cc42cf78c09acd31f))
* **rating:** prompt for review after opening a launch from a notification ([e0be1f6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e0be1f62dc7ef63e446f791fc034148d1aa5aae2))
* **terminus:** add Terminus agency repository and vehicle mappers ([2a40681](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2a406814069e7129964ee0193c7209912788fe50))
* **terminus:** add Terminus launcher config, launcher and rocket repositories ([8acdf19](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/8acdf19bd0a57d7953ca94ab4e3fec855b843a93))
* **terminus:** events and updates repositories behind DataBackend ([ee73b53](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ee73b53f6a42e78642938c87b1643c0a9099db1a))
* **terminus:** read launch rocket and timeline from their own endpoints ([a650fac](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a650fac44d247eb315db1f1866ec90aef9661fbd))
* **terminus:** serve launch lists from Terminus behind DataBackend ([b794f55](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b794f557fa90c7c7fc0e4bf10109b966b1f7f16a))
* **wear:** read launches from Terminus when the phone sends the backend ([d2a639e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d2a639e0ce0398e3892790f2a9cdc42f78fc47d1))



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



