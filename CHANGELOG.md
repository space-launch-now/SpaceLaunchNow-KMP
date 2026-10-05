## [5.46.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.46.0...v5.46.1) (2026-10-05)


### Performance Improvements

* **api:** round LL time filters to 1 minute ([#230](https://github.com/space-launch-now/SpaceLaunchNow-KMP/issues/230)) ([35ba2ee](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/35ba2ee25ae33e0a9e110b1a386523e2fdbaf70f))



# [5.46.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.45.0...v5.46.0) (2026-10-03)


### Bug Fixes

* **ads:** drop dead preWarmAdRequests and its false warn on Android ([50a6881](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/50a68818f4713ab2a5712afa38aea43a5e3e7732))
* **ads:** hold the consent timeout while a consent form is required ([c379141](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c37914136632188064c730d15e1a2b0502692911))
* **ads:** resolve consent gate via callbacks and polling, once ([36979da](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/36979da224fdef9f12855a4bc8b2e44813110c97))
* **ads:** retry failed banners with backoff and keep shown creatives ([d14324a](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d14324a75446a9da2ac35a370860c4b7d01e21d6))


### Features

* **ads:** persist interstitial cadence and drive it from Remote Config ([700e6a8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/700e6a8cac8f24551839e3ccbc67bf9f65c4b1a0))
* **ads:** reload banners per screen visit with a 45s rate floor ([735ecae](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/735ecae3c9832db7e2b5bae2a380cf76de41f9f6))
* **ads:** report banner, interstitial and rewarded ad lifecycle events ([f4697da](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/f4697da616a8c92c395b3546d465ced2f36eff9a))
* **ads:** request iOS App Tracking Transparency after consent ([ced8a3b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ced8a3baa6519744659220189ff4be2c047bb636))
* **ads:** show one inline banner in each Schedule tab on phones ([8978051](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/8978051f367ddc7a2cb44656ee0b586cd4880c56))



# [5.45.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.44.4...v5.45.0) (2026-10-02)


### Features

* **ads:** load a fresh banner for each launch detail visit ([dd11a9c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/dd11a9c845b5cf662de9eb9f82f56e242fa55961))



## [5.44.4](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.44.3...v5.44.4) (2026-10-02)


### Bug Fixes

* **ads:** raise max ad content rating from G to PG ([c94a660](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c94a6608ea2f1b637ad47ce2325d00536a57a3ac))



## [5.44.3](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.44.2...v5.44.3) (2026-10-01)


### Bug Fixes

* **android:** clear WebView accessibility focus before release ([1bf2038](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/1bf203889782c40c7b7210690aaf329b927c96dc))
* **billing:** initialise subscription repository at most once ([d2967d8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d2967d877900a503ff0393e37140e8f6dad1c4ae))
* **billing:** retry subscription init when billing initialisation fails ([c429f95](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c429f95dab9065789c75eae6ca171a3c5c0cb99f))
* **ios:** avoid competing crash handlers ([a407c62](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a407c62afd41a5a86fe04ac29ece34a2738d8d94))
* **ios:** collapse duplicate Datadog user-context writes ([6afef87](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/6afef87521dae00c48553cc5f28478807e7544ac))
* **ios:** report Crashlytics non-fatals at Error and above only ([2dde315](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2dde3154c6a75e07cfdddb3233c59923510ab277))
* **ios:** serialise billing init and skip cached CustomerInfo after failed sync ([518dabb](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/518dabb8e8535f5c2becb6d614aea06a92425718))
* **logging:** collapse duplicate Datadog user-context writes on Android ([38bd584](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/38bd584d6e6d28cf546bff31ae1165fb9b113f57))
* **news:** stop reporting cache-rescued SNAPI errors as non-fatals ([639794b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/639794b6a76275e4fc90b9a7b6f730a58b7861ef))
* route space station articles through repository ([83f6b7f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/83f6b7f5288b236cbf20462f541836f4fd55ff6c))
* **ui:** cancel in-flight pagination when rocket, astronaut or agency list reloads ([457b4ad](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/457b4ad949ab88f0cdfd8168a352949ed52aad60))
* **ui:** dedupe paginated appends in rocket, astronaut and agency lists ([3316dda](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3316ddaf0256c940d1cb9cfd4ba14ec668bfe8da))
* **ui:** keep full news card layout for domain article summaries ([097a087](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/097a087b35b6ad7d5d0549b1c48e795e37ef3c0f))



