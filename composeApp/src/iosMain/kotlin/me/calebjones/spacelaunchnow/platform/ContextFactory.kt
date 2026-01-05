package me.calebjones.spacelaunchnow.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene

@OptIn(ExperimentalForeignApi::class)
actual class ContextFactory {
    // Bundle allows you to lookup resources
    actual fun getContext(): Any = NSBundle
    // UIApplication allows you to access all app info
    actual fun getApplication(): Any = UIApplication
    
    // RootViewController - used for presenting ads and sharing
    // NOTE: This is called from Compose UI which is always on main thread
    // Returns UIViewController or empty string if not available
    actual fun getActivity(): Any {
        // iOS 13+: Use connectedScenes instead of deprecated keyWindow
        val scenes = UIApplication.sharedApplication.connectedScenes
        val windowScene = scenes.firstOrNull { scene ->
            (scene as? UIWindowScene)?.let { 
                it.activationState == platform.UIKit.UISceneActivationStateForegroundActive 
            } ?: false
        } as? UIWindowScene
        
        // Find the key window and return its root view controller
        val keyWindow = windowScene?.windows?.firstOrNull { window ->
            (window as? platform.UIKit.UIWindow)?.keyWindow == true
        } as? platform.UIKit.UIWindow
        
        return keyWindow?.rootViewController ?: ""
    }
}