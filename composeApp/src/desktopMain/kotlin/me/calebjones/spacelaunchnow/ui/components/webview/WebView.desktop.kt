package me.calebjones.spacelaunchnow.ui.components.webview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Desktop (JVM) actual: no embedded browser. Falls back to opening the URL in the system default
 * browser and rendering an "open in browser" surface. Reports no in-page back history so the host
 * screen treats back as a normal screen pop.
 */
@Composable
actual fun WebView(
    url: String,
    modifier: Modifier,
    onLoadingChanged: (Boolean) -> Unit,
    onCanGoBackChanged: (Boolean) -> Unit,
    backTrigger: Int,
) {
    onLoadingChanged(false)
    onCanGoBackChanged(false)
    WebViewBrowserFallback(url = url, modifier = modifier)
}
