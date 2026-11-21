package me.calebjones.spacelaunchnow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.PlatformType
import me.calebjones.spacelaunchnow.getPlatform
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_fine_print_manage_app_store
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_fine_print_manage_google_play
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_fine_print_manage_other
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_fine_print_secure_app_store
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_fine_print_secure_google_play
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_fine_print_secure_other
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_fine_print_secure_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_fine_print_terms
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_fine_print_trial
import org.jetbrains.compose.resources.stringResource

/**
 * Standard subscription fine-print footer shown on subscription screens.
 *
 * Displays:
 * - Secure billing note (platform-specific store name)
 * - Optional trial disclosure (Google Play / App Store policy requirement)
 * - Auto-renew notice
 * - Tappable [LegalLinksText] (Terms of Service + Privacy Policy)
 * - Platform-specific "Manage Subscriptions" button
 *
 * @param hasTrialOffer         Whether any displayed product has a free trial.
 * @param trialPeriodDisplay    Human-readable trial duration, e.g. "3-day".
 * @param postTrialPrice        Price after trial ends, e.g. "$9.99/year".
 * @param dimColor              Color for plain body text — override for non-Material backgrounds.
 * @param linkColor             Color for tappable legal link spans.
 */
@Composable
fun FinePrint(
    hasTrialOffer: Boolean = false,
    trialPeriodDisplay: String? = null,
    postTrialPrice: String? = null,
    dimColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    linkColor: Color = MaterialTheme.colorScheme.primary,
) {
    val uriHandler = LocalUriHandler.current
    val platformType = getPlatform().type

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = dimColor
        )

        Text(
            text = stringResource(Res.string.app_components_fine_print_secure_title),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = dimColor.copy(alpha = (dimColor.alpha * 2f).coerceAtMost(1f))
        )

        Text(
            text = when (platformType) {
                PlatformType.ANDROID -> stringResource(Res.string.app_components_fine_print_secure_google_play)
                PlatformType.IOS -> stringResource(Res.string.app_components_fine_print_secure_app_store)
                else -> stringResource(Res.string.app_components_fine_print_secure_other)
            },
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = dimColor
        )

        // Trial-specific disclosure (Google Play / App Store policy requirement)
        if (hasTrialOffer && trialPeriodDisplay != null && postTrialPrice != null) {
            Text(
                text = stringResource(Res.string.app_components_fine_print_trial, postTrialPrice, trialPeriodDisplay),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = dimColor.copy(alpha = (dimColor.alpha * 1.6f).coerceAtMost(1f))
            )
        }

        Text(
            text = stringResource(Res.string.app_components_fine_print_terms),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = dimColor
        )

        // Manage Subscriptions button (mobile only)
        if (platformType.isMobile) {
            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = {
                    val url = when (platformType) {
                        PlatformType.ANDROID -> "https://play.google.com/store/account/subscriptions"
                        PlatformType.IOS -> "https://apps.apple.com/account/subscriptions"
                        else -> null
                    }
                    url?.let { uriHandler.openUri(it) }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = linkColor
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = when (platformType) {
                        PlatformType.ANDROID -> stringResource(Res.string.app_components_fine_print_manage_google_play)
                        PlatformType.IOS -> stringResource(Res.string.app_components_fine_print_manage_app_store)
                        else -> stringResource(Res.string.app_components_fine_print_manage_other)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = linkColor
                )
            }
        }

        LegalLinksText(
            dimColor = dimColor,
            linkColor = linkColor
        )
    }
}
