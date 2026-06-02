package me.calebjones.spacelaunchnow.ui.components.webview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders a web page at [url] inside the app.
 *
 * Only Android provides a true in-app WebView (backed by [android.webkit.WebView]).
 * iOS and Desktop actuals fall back to opening the URL in the system browser and
 * render a lightweight "open in browser" surface so the multiplatform build stays green.
 *
 * @param url the URL to load
 * @param modifier layout modifier applied to the web view container
 * @param onLoadingChanged invoked with `true` while a page is loading and `false` once finished
 * @param onCanGoBackChanged invoked whenever the web view's back-history availability changes;
 *        used by the host screen to decide whether back should navigate within the page
 * @param backTrigger incremented by the host to request an in-page back navigation; the actual
 *        only consumes a change when [onCanGoBackChanged] last reported `true`
 */
@Composable
expect fun WebView(
    url: String,
    modifier: Modifier = Modifier,
    onLoadingChanged: (Boolean) -> Unit = {},
    onCanGoBackChanged: (Boolean) -> Unit = {},
    backTrigger: Int = 0,
)
