package me.calebjones.spacelaunchnow.ui.winback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import me.calebjones.spacelaunchnow.ui.ads.AdCoveringModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.analytics.events.PaywallSource
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.ProductType
import me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModel
import me.calebjones.spacelaunchnow.winback.WinbackSheetController
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.common_close
import spacelaunchnow_kmp.composeapp.generated.resources.winback_body
import spacelaunchnow_kmp.composeapp.generated.resources.winback_cta
import spacelaunchnow_kmp.composeapp.generated.resources.winback_cta_trial
import spacelaunchnow_kmp.composeapp.generated.resources.winback_not_now
import spacelaunchnow_kmp.composeapp.generated.resources.winback_price_no_trial
import spacelaunchnow_kmp.composeapp.generated.resources.winback_price_trial
import spacelaunchnow_kmp.composeapp.generated.resources.winback_title
import kotlin.time.TimeSource

/**
 * Day-7 win-back offer. Checks eligibility once per app open, after the user reaches Home, and
 * shows a dismissible sheet at most once. Renders nothing when the flag is off, the user
 * subscribes, or the annual product is not loaded.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WinbackSheetHost(
    isOnHome: Boolean,
    controller: WinbackSheetController = koinInject(),
    viewModel: SubscriptionViewModel = koinInject(),
) {
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val availableProducts by viewModel.availableProducts.collectAsState()
    val annualProduct = remember(availableProducts) { viewModel.getProductByType(ProductType.ANNUAL) }
    val source = PaywallSource.WINBACK.value

    var eligible by remember { mutableStateOf(false) }
    var checked by remember { mutableStateOf(false) }
    var closed by remember { mutableStateOf(false) }

    // One check per app open, once subscription state has settled.
    LaunchedEffect(isOnHome, subscriptionState.isLoading) {
        if (isOnHome && !checked && !subscriptionState.isLoading) {
            checked = true
            eligible = controller.isEligible(isSubscribed = subscriptionState.isSubscribed)
        }
    }

    val visible = eligible && !closed && !subscriptionState.isSubscribed && annualProduct != null
    val shownAt = remember { TimeSource.Monotonic.markNow() }

    LaunchedEffect(visible) {
        if (visible) {
            controller.markShown() // once only, even if the app closes before the user acts
            viewModel.trackPaywallViewed(source)
        }
    }
    LaunchedEffect(uiState.successMessage) {
        if (visible && uiState.successMessage != null) closed = true
    }

    if (!visible || annualProduct == null) return

    val close = {
        if (!subscriptionState.isSubscribed) {
            viewModel.trackPaywallDismissed(source, shownAt.elapsedNow().inWholeSeconds)
        }
        closed = true
    }
    AdCoveringModalBottomSheet(
        onDismissRequest = { close() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        WinbackSheetContent(
            product = annualProduct,
            isProcessing = uiState.isProcessing,
            errorMessage = uiState.errorMessage,
            onSubscribe = {
                viewModel.trackTierSelected(ProductType.ANNUAL, annualProduct.productId, source = source)
                viewModel.purchaseProduct(annualProduct, source = source)
            },
            onClose = { close() },
        )
    }
}

@Composable
fun WinbackSheetContent(
    product: ProductInfo,
    isProcessing: Boolean = false,
    errorMessage: String? = null,
    onSubscribe: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val trial = product.freeTrialPeriodDisplay.takeIf { product.hasFreeTrial }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.common_close))
            }
        }
        Text(
            text = stringResource(Res.string.winback_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.winback_body),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (trial != null) {
                stringResource(Res.string.winback_price_trial, trial, product.formattedPrice)
            } else {
                stringResource(Res.string.winback_price_no_trial, product.formattedPrice)
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSubscribe, enabled = !isProcessing, modifier = Modifier.fillMaxWidth()) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(if (trial != null) Res.string.winback_cta_trial else Res.string.winback_cta))
            }
        }
        TextButton(onClick = onClose, enabled = !isProcessing) {
            Text(stringResource(Res.string.winback_not_now))
        }
    }
}

private val previewAnnual = ProductInfo(
    productId = "sln_premium",
    basePlanId = "annual",
    title = "Space Launch Now Premium", // i18n-ignore: preview sample data
    description = "Annual subscription", // i18n-ignore: preview sample data
    formattedPrice = "$9.99",
    priceAmountMicros = 9_990_000L,
    currencyCode = "USD",
    hasFreeTrial = true,
    freeTrialPeriodDisplay = "2-week",
)

@Preview
@Composable
private fun WinbackSheetPreview() {
    SpaceLaunchNowPreviewTheme { WinbackSheetContent(product = previewAnnual) }
}

@Preview
@Composable
private fun WinbackSheetDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) { WinbackSheetContent(product = previewAnnual) }
}
