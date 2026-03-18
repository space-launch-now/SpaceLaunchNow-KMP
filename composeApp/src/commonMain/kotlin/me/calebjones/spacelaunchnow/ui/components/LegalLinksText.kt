package me.calebjones.spacelaunchnow.ui.components

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import me.calebjones.spacelaunchnow.PlatformType
import me.calebjones.spacelaunchnow.getPlatform

private const val TERMS_URL_IOS =
    "https://www.apple.com/legal/internet-services/itunes/dev/stdeula/"
private const val TERMS_URL_DEFAULT = "https://spacelaunchnow.app/app/tos"
private const val PRIVACY_URL = "https://spacelaunchnow.app/app/privacy"

/**
 * Standard legal footer shown on all subscription screens.
 *
 * Renders: "By using this app you agree to our [Terms of Service] and [Privacy Policy].
 * Subscriptions auto-renew unless cancelled."
 *
 * The EULA URL is platform-specific (iOS → Apple standard EULA, other → hosted ToS).
 *
 * @param modifier   Applied to the [ClickableText].
 * @param dimColor   Color for the plain text portions.
 * @param linkColor  Color for the tappable link spans (always underlined).
 */
@Composable
fun LegalLinksText(
    modifier: Modifier = Modifier,
    dimColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
    linkColor: Color = MaterialTheme.colorScheme.primary,
) {
    val uriHandler = LocalUriHandler.current
    val termsUrl = if (getPlatform().type == PlatformType.IOS) TERMS_URL_IOS else TERMS_URL_DEFAULT

    val annotated = buildAnnotatedString {
        withStyle(SpanStyle(color = dimColor)) {
            append("By using this app you agree to our ")
        }
        pushStringAnnotation(tag = "URL", annotation = termsUrl)
        withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
            append("Terms of Service")
        }
        pop()
        withStyle(SpanStyle(color = dimColor)) { append(" and ") }
        pushStringAnnotation(tag = "URL", annotation = PRIVACY_URL)
        withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
            append("Privacy Policy")
        }
    }

    ClickableText(
        text = annotated,
        style = MaterialTheme.typography.labelSmall.copy(textAlign = TextAlign.Center),
        modifier = modifier,
        onClick = { offset ->
            annotated.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { uriHandler.openUri(it.item) }
        }
    )
}
