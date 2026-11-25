## [5.53.1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.53.0...v5.53.1) (2026-11-25)


### Bug Fixes

* **release:** send short English notes to Firebase, not the full changelog ([6ce4e7f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/6ce4e7f1e3d0123645982cfa56a6fe5cc0282ee4))



# [5.53.0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/compare/v5.52.0...v5.53.0) (2026-11-25)


### Bug Fixes

* **ads:** drop the banner when the slot is too short and blank it under a cover ([43a01bf](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/43a01bf7d873c314a68e3587377d1d86a4eacdc4))
* **ads:** host the banner AdView at its own size and destroy replaced views ([0611d2f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/0611d2f365888e56265c4d26cafbbc4d0612bcf1))
* **ads:** reload banners when density, font scale or window size class changes ([11be25a](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/11be25ac00c3877600936f2fcf156c775dcf7b68))
* **ads:** rename ad impression event to ad_shown and accept numeric error code ([05fc21e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/05fc21e225599cf70e6318dcdd8b530d34122bf3))
* **ads:** report the AdMob error code for failed iOS banners ([c9dbc86](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c9dbc86ad0b7ca39fd66b141d04bb9a69ee0a16b)), closes [ItsCalebJones/SpaceLaunchNow-Tasks#1267](https://github.com/ItsCalebJones/SpaceLaunchNow-Tasks/issues/1267)
* **ci:** pass test files to node --test on Node 22 ([12e9ea6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/12e9ea6d2619d54e99ba55aa2226d0d4fd204d28))
* **i18n:** do not read a percent sign in prose as a placeholder ([2cd02d2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2cd02d2036a9db34cc680957eaf7b22df02a2f08))
* **i18n:** drop committed bytecode and ignore it ([37f030d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/37f030d0a4544726a02e5ab43e0680067e0a7f55))
* **i18n:** drop the backslash from four apostrophes Compose resources show literally ([86c6035](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/86c6035aac143b84a852fcc04414ede7c5f4b56e))
* **i18n:** inline widget status key ([9743669](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9743669e5ab277d8b6ba4de9e3a753a1a843d094))
* **i18n:** keep ISS marker snippet literal ([39e8ce9](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/39e8ce98c041a3959e7b8ca0e14ebb1550d3b5c4))
* **i18n:** keep moved text in the hunks verify_move checks ([9563016](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9563016d38deecd26c2854f1b1f0e473ca73790f))
* **i18n:** keep onboarding spaces and apostrophes exact at runtime ([5f34aaf](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5f34aafbe987c02780610cd88f68c837cd6eff44))
* **i18n:** keep the store name as its own key in FinePrint ([52ac35b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/52ac35b201e93ef0ebad294de969154ee39a9c17))
* **i18n:** leave FinePrint text for a follow-up that joins split literals ([29ad8c6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/29ad8c6e28da18aa9538abc8b2fbdc30743a0092))
* **i18n:** move FinePrint text to string resources ([b06048c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b06048c6363cd9ff5706d5154fa4633964b2ed41))
* **i18n:** move the subscription plan Member label to a string resource ([5a354ef](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5a354ef137613261d2bee3ffeac4cc74689efb85))
* **i18n:** restore three comments ([c042ae9](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c042ae969335b4a937ba197ac50b33047a389734))
* **i18n:** retry a bad model reply once, name the plural warning in one place ([9ca5cf4](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9ca5cf4a683a8810de2eca79ed7733c970998028))
* **i18n:** reuse time strings for the launch date lead ([032bb5e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/032bb5ee47efa889f5a25363b2ed0b8bb1b18a4f))
* **i18n:** use a plurals resource for the astronaut mission count ([4c0a638](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4c0a638e28a48471c30c08c3cb600d8c0d5e4e79))
* **i18n:** use plain apostrophes in notification, event and schedule strings ([af5c8c7](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/af5c8c76e232a3c54c144497e2dcc00ce8af2fe4))
* **i18n:** use plain apostrophes in subscription strings ([02d5589](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/02d5589b5b6eee1cea6f694be0449ce650201044))
* **i18n:** use plain quotes in calendar setup steps ([00e5214](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/00e521470c430136467c2e06f9211e631bf4ae88))
* **i18n:** use the project resource package in FinePrint ([6b6fbe4](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/6b6fbe4771b147a5eec6581ef2d9fb2dad650223))
* **i18n:** verify_move accepts a key swap with equal English ([ecb3c16](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ecb3c16f19a8c40be27b76eca1145f850b90e3e9))
* **i18n:** verify_move counts keys in new files as new text and reads nested braces in templates ([a98d581](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/a98d581699b63acca2e4dfae4d83034f134a328f))
* **i18n:** verify_move reads every strings*.xml and handles hoisted and concatenated literals ([169fba4](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/169fba4d6a8b33a5cb5d12d7c38fd54801211a47))
* **i18n:** verify_move reads multi-line and nested literals in a template ([e086fc8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e086fc854ae13a18cf2194cbe3d01cb3644090d7))
* **i18n:** write divider padding as escapes, not quotes ([5d7f6d9](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5d7f6d9d7f30d906e1e0324316c4b52e8e6b7a80))
* **ios:** bump compose-shimmer to 1.5.0 so shimmer no longer aborts on skiko 0.150 ([e5d1195](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e5d1195505cbd302bfb1d7f4e048d5f0371a20ee))
* **ios:** keep vehicle names in English and use informal you in fr and cs listings ([77fc0ff](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/77fc0ff154ab8598eec54129f3d5819d1c79c2f8))
* **ios:** trim en-US keywords to 95 bytes ([c228d9e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c228d9e1e1154ea789a294f57fe540bbbfa87e98))
* **ios:** write the downloaded listing where the artifact step reads it and drop the review contact ([62441f0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/62441f0de18a60326dbf57d50d75b55a38f84614))
* keep the raw exception detail in the logs ([00e22a8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/00e22a899e2310fc3ce40841a96bae6bb79426bc))
* **offer:** mark holiday offer shown before checked ([5baa888](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5baa8885dcc3fdc2568b9a653fe7aa81bb7e773d))
* **offer:** no first-month intro promise when a free trial exists ([6aac6e6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/6aac6e6b1ab264f9d729811a1800fe1a7e80ed8c))
* **paywall:** keep bottom bar hidden on the SupportUs route with args ([4513610](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/45136107c410fcb229b168ea8924380e6a69463f))
* **paywall:** let the locked Starlink row take the tap ([af3ee22](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/af3ee22cf7126766f68c525c1d814303c7e5e1b0))
* **paywall:** report paywall_dismissed on iOS swipe-back and desktop window close ([dcf97c6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/dcf97c66fcf6f3dd194536678ec8f5e8da5d441c))
* **paywall:** use ad-covering bottom sheet for win-back ([02ca66b](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/02ca66b0f9d98a32d5a40e95574dc77c80800530))
* **play:** keep Soyuz, Progress, Long March and Sriharikota in English ([5f36a39](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5f36a393fa4441436f8f64aa7bd82a8e1c2da676))
* **play:** let supply download the live listing by not creating its folder first ([0bcb9a4](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/0bcb9a416e5d093335818c94563978b94bd85f48))
* **release:** normalize CRLF and read stdin as UTF-8 in notes splitter ([c60e41c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c60e41cf878c3ebe9c459e0d8df9962ec4841030))
* **release:** repair CRLF normalization in notes splitter ([d5d4cb3](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/d5d4cb3677ee810c252f49ede3911b6b3ee14de7))
* **release:** repair CRLF normalization in notes splitter ([3dda666](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3dda66684b53d1dde6151aa177f2be4aaadc61cc))
* **wear:** escape apostrophe and newlines in wear strings ([05c9028](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/05c9028305010f63e8d79d44e7d34a57c48a0edc))
* **wear:** mark app_name untranslatable so lint finds no missing translation ([82553e5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/82553e530854ba107d55a90767151e40280bc4e3))
* **wear:** send SpaceLaunchNow User-Agent on Coil image loads ([e30cab0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e30cab0238cfa28b9ef5f3373eecf7daf50b7a71))
* **wear:** show localized text when no launch data loads ([ac93d3c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ac93d3ca2801d8fb320251f3ce1c599012f3eb0f))
* **widgets:** pass the access flag to the launch source ([543ffd9](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/543ffd919a79eef0b8ac8ccb91d9bbd04cca9fb6))
* **widgets:** redraw from cache and refresh by launch proximity ([4ebc95f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4ebc95ff3d117664537e797351d957f31e398ea2))


### Features

* **ads:** add ad_frame_mismatch event, frame check and rate limiter ([dacf10e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/dacf10eeed131b89d2a3a30cb7bd432f2adfd30b))
* **ads:** add height-aware banner frame guard ([9baa1da](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9baa1da332a4f7ea0774a6c7054a5bf0976f0c30))
* **ads:** hide banner ads while sheets, dialogs, menus or the keyboard cover them ([1a5c834](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/1a5c834a0864141bd5f7e1dba858be159e7621c5))
* **ads:** report banner frame mismatches from the host ([db46928](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/db46928c1712b103d694cd8625723abc65f7e956))
* **analytics:** refresh app_language when the per-app language changes ([88e3529](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/88e35295c77390f8375ac2663c9965367871fb65))
* **analytics:** set app_language user property from the resolved UI language ([9af2734](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9af2734c06d22fc1fa3360846a3b4b6151de3a88))
* **billing:** add first-month intro offer fields to ProductInfo ([c5d1ca8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c5d1ca885162ee5a28b1e96a5668182a75abd2e1))
* **config:** add holiday offer Remote Config and shown-campaign pref ([b4034ab](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b4034abe0cde20e87b0c22cb670c9f52b03e1cdf))
* dismissible upgrade row on Home behind a remote flag ([3aab4f8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/3aab4f8a2b8ea14862160af63b90445f22367a76))
* fire paywall_dismissed from the Support Us screen ([075a705](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/075a705735f4b628331042bee0d8caae4220a208))
* **i18n:** add Android and Wear OS translations in 10 languages ([866c837](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/866c837367c0a78ee0b908469471ef6bb3dadedb))
* **i18n:** add Crowdin config and move rocket detail error text to string resources ([4e759d2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4e759d23906f47ccaef3a898d85f2b8cc30d05c1))
* **i18n:** add first translations of the shared screens in 10 languages ([84704d2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/84704d2635f1c35d05060fc48e3602e4ee33e5ab))
* **i18n:** add locales_config.xml and localeConfig for per-app language ([8c69059](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/8c69059d3fa08432d7cd4f14159a9f0fb14420de))
* **i18n:** add verify_move.py check and pseudo-locale generator ([7f950ac](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/7f950ac1a793203dd2efc0d3e1828980bac8cb1e))
* **i18n:** add widget and permission prompt translations in 10 languages ([751fc57](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/751fc579de1416372ab38e32ce83331565786d98))
* **i18n:** build launch push body text from translated keys on Android and iOS ([0b4494d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/0b4494d552393b96eafe4b763d7de5537c864b9b))
* **i18n:** check every Compose strings file and the iOS catalogs, run the check in CI ([15e776d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/15e776daf0c379c2505bf226d84389e37051f482))
* **i18n:** CI check for new hard-coded UI text with baseline ([dae33c1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/dae33c1fc606806b8119f76accfa87601d4635bc))
* **i18n:** map Android res and Wear strings in crowdin.yml ([86034fe](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/86034feaab0497b3fc4afa8113518d0b0e619c46))
* **i18n:** map API status, orbit, mission type and country labels to string keys ([4975d24](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/4975d24c479b318429e88371e60cb8509496c6b2))
* **i18n:** move agencies text to string keys ([f65e523](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/f65e523c6fe31a514960f9cb808d7919f7507836))
* **i18n:** move Android notification channel, fallback and share strings to resources ([b949d44](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b949d44c1003d44809304740cae9f031ed8981ca))
* **i18n:** move astronaut text to string keys ([e9d23d6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e9d23d65e26b13bb31910f52a059683a75d75b0d))
* **i18n:** move enum display names to string keys ([6028911](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/60289118d5a2f45904286d312b1a6a5c836373a4))
* **i18n:** move explore, news, roadmap and shared component text to string keys ([040c72f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/040c72f003db4a0190d2e735d85517c399ee620d))
* **i18n:** move iOS widget and Info.plist text to String Catalogs, add 10 languages ([dbe472c](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/dbe472c0b3e0f3638932d2349258a0619033c88b))
* **i18n:** move leftover user text to string keys ([c4f1300](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c4f1300d2f59069431b9a35d101c73a4a837d938))
* **i18n:** move onboarding text to string keys ([08f4b30](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/08f4b30cad996621df6fba9af44d4ccfdc907b09))
* **i18n:** move relative time, countdown labels and timeline offsets to string keys ([720fa94](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/720fa947d227c4a87066fc09394c173981421b0c))
* **i18n:** move rockets screens to string keys ([bb2fc17](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/bb2fc172339454efaf03d1451ce43193d0a28571))
* **i18n:** move space station and starship text to string keys ([cf08873](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/cf08873b8f5700ce7f6627aa54a9fd0ccf355953))
* **i18n:** move subscription and paywall text to string keys ([498e6c6](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/498e6c6ea8c5042465f47db7d59ff2aec49edead))
* **i18n:** sync shared keys from Compose master to Wear and String Catalog ([9a76775](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/9a767757085dc1641d3653d030ef9ce287552054))
* **i18n:** translation script, glossary, placeholder and plural checks ([2017c7d](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2017c7dfb6a0343e9f949c0cb108d7ba0903f447))
* **i18n:** update hardcoded text baseline for subscription move ([7b16925](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/7b16925a62f15f38e6f7ec7b7e16033dd427b6d7))
* **ios:** add App Store listing text for 10 languages ([96ee930](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/96ee93001078f4432de24f130bf081aeb46d5f1a))
* **ios:** add App Store purchase text check, download and upload scripts ([0dec3a5](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/0dec3a57db5ec73ebbfe0ac92606e782e3229430))
* **ios:** add purchase display texts in 10 languages ([610063f](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/610063f28b928769595e69057e5462e2932948d8))
* **ios:** add purchase texts workflow and README ([ff85989](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/ff85989b96ea604b59687112c5d3a2d92075ff30))
* **ios:** add the live en-US App Store keywords ([7d4f7e2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/7d4f7e23f8ba395398105184bdad9fd9ab57f090))
* **ios:** App Store listing text as files with fastlane deliver upload ([589ef19](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/589ef1924adba1f1243915f4e754e55f90491f3b))
* **ios:** live subtitle and a download_metadata lane for keywords ([e1ed6fc](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e1ed6fc088069b9d6b5e9ddf7da165481074f5f9))
* **ios:** propose comma-separated en-US keywords without name and subtitle words ([879fb9a](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/879fb9a58cac89ebe1307b55ea2c757f90bfce2c))
* **ios:** reconcile purchase texts with live App Store English ([6f37164](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/6f3716464192580a6f0fa9bab017a48446a2fa75))
* **launch-detail:** add Share button to tablet layout ([b69a3e8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b69a3e8e59949ed3102fc8f84757b4fd4b5f4b41))
* **offer:** add holiday offer and intro price strings in all locales ([b84f8ad](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b84f8adc426902d40d512ef003764255a9e2093c))
* **offer:** add holiday offer rule, controller and OFFER_SHEET source ([84c10b0](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/84c10b03bd47ab2c3ccb7f4fcf9e2e4d352fe9c3))
* **offer:** add holiday offer sheet with close countdown ([93b7795](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/93b7795f40687f752ac5e4443ab7a4436d58442f))
* **paywall:** day-7 win-back sheet after a dismissed onboarding paywall ([2fc9aa4](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/2fc9aa4c7844e86a7ac9492a4a58fb5ac4eceb5c))
* **paywall:** open the paywall on a tap of a locked notification control behind a flag ([b1e5067](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/b1e50676193c0aead6983ec0562edc2dccd37fc0))
* **paywall:** show first-month intro price line on monthly plan ([049df1e](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/049df1eee2b358494392cb58da73a547d7bcd5c4))
* **paywall:** tag every paywall entry with a source ([41e14f2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/41e14f2e28cdd3ad3a6d289777e00527a20a1ccf))
* **play:** add 10 translated Play listing locales ([078b083](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/078b083678829f4594f677cf1ce57f91774d0bb4))
* **play:** manage Google Play listing text in git with a fastlane upload lane ([7cfade8](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/7cfade8452af00cf1f6802ad00a82fa3afc2f503))
* **release:** translate Play What's new notes into the 10 listing languages ([00914b2](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/00914b211ae77c6bbc66598dd436fdedce465e1a))
* **settings:** add Language row that opens the OS per-app language screen ([be8acdd](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/be8acdd93921adfd0d052a8b8602bf29f4176d7a))
* show errors from a fixed list, never raw exception text ([5da06f1](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/5da06f14f7e0005be615e0e532d77fd04108429d))
* **wear:** move Wear UI, tile and complication text into string resources ([c63cd74](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/c63cd74245bcaef48f124a600498f1ac5bd693d0))


### Reverts

* Revert "build: run the desktop tests from ./gradlew test" ([e2fc767](https://github.com/space-launch-now/SpaceLaunchNow-KMP/commit/e2fc7671bee6b369d32db50d4ef7fd46ea200548))



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



