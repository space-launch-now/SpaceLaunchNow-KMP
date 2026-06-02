package me.calebjones.spacelaunchnow.ui.components.webview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

/**
 * iOS actual: a real in-app [WKWebView] embedded via [UIKitView], matching the Android actual's
 * contract (same expect signature).
 *
 * JavaScript is on by default for WKWebView. Navigations stay inside the web view; loading state
 * and back-history availability are reported back to the host through a [WKNavigationDelegateProtocol]
 * so the host screen can show a progress indicator and route the system back button into the page
 * history (via [backTrigger]).
 *
 * VERIFICATION STATUS: this file type-checks against the Kotlin/Native WebKit bindings
 * (compileKotlinIosSimulatorArm64 passes). RUNTIME behavior (page rendering, back navigation,
 * loading callbacks firing) is still UNVERIFIED — it must be run on a Mac/Simulator/device, since
 * linking the framework and building the Xcode app require macOS. See the team report.
 *
 * Interop notes:
 *  - `navigationDelegate` on WKWebView is a *weak* reference, so the delegate is held in [remember]
 *    to keep it alive for the lifetime of the composition.
 *  - The delegate's callbacks are refreshed every recomposition so the latest [onLoadingChanged] /
 *    [onCanGoBackChanged] lambdas are invoked (avoids capturing stale closures).
 *  - The [WKWebView] instance is held in [remember] so [backTrigger] changes can call `goBack()` on
 *    the same instance created in `factory`.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun WebView(
    url: String,
    modifier: Modifier,
    onLoadingChanged: (Boolean) -> Unit,
    onCanGoBackChanged: (Boolean) -> Unit,
    backTrigger: Int,
) {
    // Delegate is created once and retained by the composition (navigationDelegate is weak).
    val navigationDelegate = remember { WebViewNavigationDelegate() }
    // Keep its callbacks pointing at the latest lambdas on every recomposition.
    navigationDelegate.onLoadingChanged = onLoadingChanged
    navigationDelegate.onCanGoBackChanged = onCanGoBackChanged

    // Hold the web view so update {} can drive goBack() on the same instance.
    val webView = remember {
        WKWebView(
            frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
            configuration = WKWebViewConfiguration(),
        ).apply {
            setNavigationDelegate(navigationDelegate)
        }
    }

    // Remember the last back trigger we acted on so recomposition doesn't replay it.
    val lastBackTrigger = remember { intArrayOf(0) }

    UIKitView(
        factory = {
            NSURL.URLWithString(url)?.let { nsUrl ->
                webView.loadRequest(NSURLRequest.requestWithURL(nsUrl))
            }
            webView
        },
        modifier = modifier,
        update = { view ->
            // Host requested an in-page back navigation.
            if (backTrigger != lastBackTrigger[0]) {
                lastBackTrigger[0] = backTrigger
                if (view.canGoBack) {
                    view.goBack()
                }
            }
        },
    )
}

/**
 * WKNavigationDelegate that forwards page-load lifecycle into Compose callbacks. Callbacks are
 * `var`s so the host composable can refresh them each recomposition.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class WebViewNavigationDelegate :
    NSObject(),
    WKNavigationDelegateProtocol {

    var onLoadingChanged: (Boolean) -> Unit = {}
    var onCanGoBackChanged: (Boolean) -> Unit = {}

    // All four delegate methods share the Objective-C `webView:...` selector family, so each
    // collides on the Kotlin side and needs @ObjCSignatureOverride to be accepted.
    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
        onLoadingChanged(true)
        onCanGoBackChanged(webView.canGoBack)
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        onLoadingChanged(false)
        onCanGoBackChanged(webView.canGoBack)
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailNavigation: WKNavigation?,
        withError: NSError,
    ) {
        onLoadingChanged(false)
        onCanGoBackChanged(webView.canGoBack)
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: NSError,
    ) {
        onLoadingChanged(false)
        onCanGoBackChanged(webView.canGoBack)
    }
}
