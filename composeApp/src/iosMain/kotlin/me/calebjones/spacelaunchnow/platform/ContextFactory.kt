package me.calebjones.spacelaunchnow.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSThread
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UISceneActivationStateForegroundInactive
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_sync

@OptIn(ExperimentalForeignApi::class)
actual class ContextFactory {
    // Bundle allows you to lookup resources
    actual fun getContext(): Any = NSBundle
    // UIApplication allows you to access all app info
    actual fun getApplication(): Any = UIApplication

    // RootViewController - used for presenting ads and sharing
    // Returns UIViewController or empty string if not available
    //
    // Fallback order handles startup race conditions:
    //   1. Foreground-active scene (normal steady state)
    //   2. Foreground-inactive scene (during launch/transition)
    //   3. Any connected scene with windows (very early startup)
    actual fun getActivity(): Any {
        var result: Any = ""

        val resolveOnMain = {
            val scenes = UIApplication.sharedApplication.connectedScenes

            // Prefer active, then inactive (startup race), then any scene
            val windowScene = (
                scenes.firstOrNull { scene ->
                    (scene as? UIWindowScene)?.activationState == UISceneActivationStateForegroundActive
                }
                ?: scenes.firstOrNull { scene ->
                    (scene as? UIWindowScene)?.activationState == UISceneActivationStateForegroundInactive
                }
                ?: scenes.firstOrNull { it is UIWindowScene }
            ) as? UIWindowScene

            if (windowScene != null) {
                // Prefer key window; fall back to first window that has a root view controller
                val window = windowScene.windows
                    .mapNotNull { it as? UIWindow }
                    .firstOrNull { it.keyWindow }
                    ?: windowScene.windows
                        .mapNotNull { it as? UIWindow }
                        .firstOrNull { it.rootViewController != null }

                result = window?.rootViewController ?: ""
            }
        }

        if (NSThread.isMainThread()) {
            resolveOnMain()
        } else {
            dispatch_sync(dispatch_get_main_queue(), resolveOnMain)
        }

        return result
    }
}