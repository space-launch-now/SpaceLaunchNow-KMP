package me.calebjones.spacelaunchnow.ui.components.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView as AndroidWebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Android actual: a real in-app [android.webkit.WebView].
 *
 * JavaScript and DOM storage are enabled (most news sites need them). Navigations stay inside
 * the WebView instead of bouncing out to the browser. Loading state and back-history
 * availability are reported back to the host so it can show a progress indicator and route the
 * system back button into the page history.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun WebView(
    url: String,
    modifier: Modifier,
    onLoadingChanged: (Boolean) -> Unit,
    onCanGoBackChanged: (Boolean) -> Unit,
    backTrigger: Int,
) {
    // Remember the last back trigger we acted on so a recomposition doesn't replay it.
    val lastBackTrigger = remember { intArrayOf(0) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            AndroidWebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(
                        view: AndroidWebView?,
                        url: String?,
                        favicon: Bitmap?,
                    ) {
                        super.onPageStarted(view, url, favicon)
                        onLoadingChanged(true)
                        onCanGoBackChanged(view?.canGoBack() == true)
                    }

                    override fun onPageFinished(view: AndroidWebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingChanged(false)
                        onCanGoBackChanged(view?.canGoBack() == true)
                    }
                }
                webChromeClient = WebChromeClient()

                loadUrl(url)
            }
        },
        update = { webView ->
            // Host requested an in-page back navigation.
            if (backTrigger != lastBackTrigger[0]) {
                lastBackTrigger[0] = backTrigger
                if (webView.canGoBack()) {
                    webView.goBack()
                }
            }
        },
        onRelease = { webView ->
            webView.stopLoading()
            webView.destroy()
        },
    )
}
