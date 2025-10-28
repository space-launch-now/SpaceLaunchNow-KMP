# [4.0.0](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/compare/v4.0.0-b27...v4.0.0) (2025-10-28)


### Bug Fixes

* error with test and deprecated things ([fe6e081](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/fe6e0818760939f59f39bdfe996b8918c24b2150))
* fix layout for medium size screen ([fd792d8](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/fd792d85eb22f1c56e2b5dd7c8ea60b385644833))
* premium badge layout and improve UI responsiveness ([682ef0a](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/682ef0ab8b1c66845b3b8a1ab2f577ad853c93c1))


### Features

* Add ad unit IDs to env example and update release version ([2ecb78d](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/2ecb78d8c6872d7c7374f5bbd2d0d8668c8436de))
* Add FCMBridge and NotificationTestHelper for Firebase Cloud Messaging integration ([e967612](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/e967612d25f061070a845b99d05012abdbe4d4e7))
* Update app name for debug mode and enhance ad handling ([5322df8](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/5322df8653343d37a00f512fbc450d3beccd603c))



# [4.0.0-b27](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/compare/504006e2e33c1667c6e60f84f920d76ed77d02c9...v4.0.0-b27) (2025-10-24)


### Bug Fixes

* add permissions section to workflows for better access control ([6ac064e](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/6ac064e35d7b6f3a15b20e1ff3539afe1725c387))
* **build:** improved version tracking cross-platform ([7a3048c](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/7a3048c566dad6dc123f5ebe0f95d098aee2f5c2))
* **build:** increase Gradle heap size for iOS framework compilation ([5b871e8](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/5b871e8de94b53b77d020d7e3cd9374d4ed2dd6b))
* **build:** increase Gradle heap size for iOS framework compilation in CI/CD ([418b450](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/418b450683012855cbe1665f232ab3e9b11aa224))
* **build:** update iOS build process to include framework configuration and paths ([6b6f523](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/6b6f5238c09dab7ae977affba73ca691d1898f0a))
* cicd [skip ci] ([c660cee](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/c660cee2571e219efb9d45b544b684fd46ba0fba))
* **cicd:** update cicd process ([5d6f018](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/5d6f018cf240228eb59f0356108bd59d05b1727a))
* **ci:** correct artifact path resolution for Firebase Distribution ([#21](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/issues/21)) ([0390ba5](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/0390ba56e259b98ad5a1b3dad6b66ffd0eb3c00b))
* **ci:** disable all jobs but ios and bump memory ([0aa5145](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/0aa5145f82c52ef7af154275d2ebb7b009197ea8))
* **ci:** remove duplicate step name in master-deploy workflow ([afe269f](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/afe269f0b677625ae27ea27f347fdda58e0723c9))
* **ci:** working on CI issues ([541f9ee](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/541f9eebd57d9bb7ded2f6f3ba99dcdc8eb86f00))
* enhance APK resolution and Firebase deployment steps in master-deploy workflow ([a870d61](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/a870d61dd5a7f24759120c256358479ef7718bf1))
* Enhance launch detail UI and fix nav state management ([9c339e0](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/9c339e027de9d8ca21b807dd35687d51a2d806f1))
* enhance master-deploy workflow to support AAB deployment and versioning improvements ([b7b7667](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/b7b76678abab04a8f0f8b58b1522aa5d4b5241b2))
* **notification:** clicking on notification should properly open detailed view ([195a2af](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/195a2afa6d88039310672e8833b2095d4a73d093))
* remove 'keepLaunchesFor24Hours' setting and related functionality ([01b5296](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/01b529647ca0c1c30cca5faa7ee7397ca1ba1d3b))
* remove iOS IPA handling from master deploy workflow ([#17](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/issues/17)) ([1f86f12](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/1f86f1268841a934755c3c3de3d990b38a7018b4))
* remove pre-release flag from version update step in master-deploy workflow ([a704563](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/a704563dc925af78f41c7b20c65d4cb3c4167298))
* Remove unnecessary dependency on build-ios in deploy job [skip ci] ([5420a46](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/5420a46fb6382abdf5120bc70a83891931f64ed0))
* scrolling in detailed view before loading is finished ([4feb17f](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/4feb17f77e14a4f5aeeee1381caee479bd8abcbe))
* standardize formatting in version.properties ([fcba685](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/fcba68568666be0abe71daa8f61dd2945bc32841))
* **test:** update to properly use the correct HTTP client per platform ([4c622c5](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/4c622c51d2167aaddec32cd76d541e823cb50d12))
* **ui:** settings and theme customization screens ([3e86507](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/3e865071927ad545d1fda7defd87b7eb8ea3171b))
* update ProLifetimeCard styling with new color and reduced effects ([b3ed691](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/b3ed69185c3c54f2885ab3315838897a24ebc104))
* update section title from "Upcoming Launches" to "Launch Schedule" ([c4282f4](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/c4282f4783ebefa2e2d5f99c6cb228f7061528cc))
* update TEAM_ID in iOS configuration ([1b9dbbb](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/1b9dbbba1aa9dad7a32dc2e5c8da11441e04a132))
* update test report path pattern to include all subdirectories ([e8e92f3](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/e8e92f3ced29a281d4598ba523b0d55cff012273))
* update versioning logic in master-deploy workflow and version.properties ([91462e8](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/91462e86f188ab9bb3a3f322e76b27535b2b9842))
* **workflow:** update versioning logic for pre-release [skip ci] ([8159882](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/8159882b33596499e5bf845a64a3aca3ea533949))


### Features

* Add AdMob ad unit IDs to CI/CD workflows and documentation for ad revenue integration ([2372044](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/2372044dbe7b3160bcd8e132d0c074e665ee02fc))
* add bidirectional carousel for launches ([2356025](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/2356025b41ce640c65548be33e41732b502a13eb))
* add billing ([75b1985](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/75b19852e73dee72fee347b2a1ac07d77f3e5f61))
* add Calendar Sync feature with UI and navigation ([fe65161](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/fe65161c969ea4ff6f54e0afd54dba8685c044f4))
* add compose-ShadowGlow library with glow effect on Gold card ([b54ae06](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/b54ae06d8afc21004301b6e094df8fb8b08d13cc))
* add environment variable management and API key configuration ([98738c4](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/98738c4119a8518d661efc404f8f543b18dcc463))
* add expect/actual pattern for platform-specific shadow glow ([80fbcac](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/80fbcac33f6330473d1b3bd116efd01af6bd8aa2))
* Add iOS and Android release workflows ([20cbd51](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/20cbd51924b775822597b16f1d45644f183f99b1))
* add launch image display to LaunchDetailView and implement scroll behavior in LaunchDetailScreen ([c63a059](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/c63a059451a9c8f7c994b96280aff0baf89576ba))
* add mission name display to detailed view ([5e9ed4a](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/5e9ed4af2d3e1a7f768fa087923303360b144bd9))
* add responsive layout for home screen and implement screen height functions for all platforms ([a9b54e2](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/a9b54e25e872572a2f19d4f85432bf64851c4677))
* add SharedElementKey class for shared element transitions ([0a29d69](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/0a29d695429756006b13e6d52d718421fa9b7563))
* add Spaceflight News API and wrap up home screen ([d04e803](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/d04e803885f11b04ffd45073a425d1bf0d970bcd))
* center first upcoming launch card with adjacent peek ([112ceef](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/112ceef970116b3d058f1b9f67327e8c68aacaf3))
* **ci:** update workflows to include RevenueCat API keys and create comprehensive secrets documentation ([bfc8cec](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/bfc8cecd6d5544ce8403246afec10af7954f7d4b))
* complete iOS billing implementation (pre-RevenueCat) ([50e3fe0](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/50e3fe0a0442b01153a906417394483485ba2a51))
* **debug:** implement secure debug menu unlock ([01102b9](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/01102b9f1c07524e35989a8625e8ee3d3070145b))
* enable notifications and navigation improvements ([eb5a8a9](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/eb5a8a9e684097be67119ceb9ecf1f1f0ed8e2a5))
* Enhance ad integration with new ad placements and improved sizing logic ([06cf224](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/06cf224a2f309d88d94e12a856916b37129012b8))
* Enhance debug feature handling and improve ad loading behavior ([f121c70](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/f121c70cd1eab163ed79e699d56151b76a66cfee))
* **home:** Refactor home screen layout to improve responsiveness and add quick stats ([571f899](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/571f899801bc1cd16c20e1151fe99e5f47cd0432))
* implement caching for launch data to optimize API calls and improve performance ([e55142c](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/e55142c23254768e59b049f3b8f1e5259c526f7d))
* implement platform color extraction ([45593b6](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/45593b632942c9cd79badda3e85c38a579d42a31))
* implement platform-specific sharing functionality ([b4821e2](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/b4821e2a12c78a23019ff99d3e79331a028122a7))
* implement premium upgrade prompt for NotificationSettingsScreen ([4f1c3ec](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/4f1c3ec74e746decd565ddbeb90f6e984cb93a32))
* Implement temporary premium access via rewarded ads and update related UI components ([18b706d](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/18b706d046c89c7e16a83c729d0368140cc45df6))
* implement the useUtc setting ([060c156](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/060c15647f121c41887fb84ea7fbae26666d46e5))
* **init:** add initial project setup ([504006e](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/504006e2e33c1667c6e60f84f920d76ed77d02c9))
* initial work to support iOS app ([e8a09c7](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/e8a09c73c85df9d789df97d00b6ed7f2df7e8a56))
* Integrate ads into the app ([58087fb](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/58087fb7e07eeed416a7d402aff7ba08181138a9))
* Integrate GlobalAdManager for improved ad handling and update widget access logic ([8763245](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/87632450ba65db2a6e96859e8c2bcd184f5177fc))
* **ios:** widget ([ba0417b](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/ba0417b9d9dbdf4bf9007c0c01901f033c03ca3b))
* **notifications:** add notification customization feature and update related UI components ([1032c0e](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/1032c0eac4e664ab8bab4efc3b211d588a8e04d1))
* **notifications:** implement granular notification channels and settings management ([563a897](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/563a89782a8b5e2eb08b052b61dfe37b5341ee8f))
* **notification:** update notification icon and improve filtering logic ([fa2dfbf](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/fa2dfbf3879cef881a3e8d1afa978a60afee2498))
* Refactor release workflow to support parallel jobs and version calculation ([f9ad766](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/f9ad766f1f1ec084b90d362f14b027285c016546))
* route to notif page from home page ([376671f](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/376671f17d92d0bcb4aefef7f8a3ad76d7c417a0))
* update CI/CD pipeline and documentation ([b3ddfbc](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/b3ddfbcda4d8ad2f9e3444dc33ec0b777f1e368a))
* update splash background colors ([c4e9502](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/c4e950208816879ac30944085489e4837956b70f))
* **widget:** add android widget ([#13](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/issues/13)) ([261fc3d](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/261fc3dda2590dd17a505ce599892f0e30c8b4d8))
* **widgets:** implement theme and widget customization ([83df2a8](https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/commit/83df2a88e98df14b8abceccf90f344c054e76f37))



