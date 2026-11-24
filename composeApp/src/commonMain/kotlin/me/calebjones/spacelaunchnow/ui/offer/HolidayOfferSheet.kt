package me.calebjones.spacelaunchnow.ui.offer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.analytics.events.PaywallSource
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.offer.HolidayOfferConfig
import me.calebjones.spacelaunchnow.offer.HolidayOfferController
import me.calebjones.spacelaunchnow.offer.HolidayOfferSession
import me.calebjones.spacelaunchnow.offer.OfferCloseCountdown
import me.calebjones.spacelaunchnow.ui.ads.AdCoveringModalBottomSheet
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.ProductType
import me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.common_close
import spacelaunchnow_kmp.composeapp.generated.resources.offer_benefit_more
import spacelaunchnow_kmp.composeapp.generated.resources.offer_benefit_no_ads
import spacelaunchnow_kmp.composeapp.generated.resources.offer_benefit_themes
import spacelaunchnow_kmp.composeapp.generated.resources.offer_benefit_widgets
import spacelaunchnow_kmp.composeapp.generated.resources.offer_close_loading_description
import spacelaunchnow_kmp.composeapp.generated.resources.offer_cta
import spacelaunchnow_kmp.composeapp.generated.resources.offer_price_line
import spacelaunchnow_kmp.composeapp.generated.resources.offer_product_name
import spacelaunchnow_kmp.composeapp.generated.resources.offer_see_all_plans
import spacelaunchnow_kmp.composeapp.generated.resources.offer_title
import kotlin.time.TimeSource

/**
 * Holiday offer sheet. Checks eligibility once per app open, after the user reaches Home and
 * the subscription state and products have loaded, and shows the sheet at most once. The
 * sheet cannot be dismissed until its close delay ends.
 *
 * [onSeeAllPlans] opens the normal paywall with [PaywallSource.OFFER_SHEET]; the host closes
 * the sheet first.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayOfferSheetHost(
    isOnHome: Boolean,
    onSeeAllPlans: () -> Unit,
    controller: HolidayOfferController = koinInject(),
    viewModel: SubscriptionViewModel = koinInject(),
) {
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val availableProducts by viewModel.availableProducts.collectAsState()
    val monthlyProduct = remember(availableProducts) { viewModel.getProductByType(ProductType.MONTHLY) }
    val source = PaywallSource.OFFER_SHEET.value

    var config by remember { mutableStateOf<HolidayOfferConfig?>(null) }
    var checked by remember { mutableStateOf(false) }
    var closed by remember { mutableStateOf(false) }

    // One check per app open, once subscription state and products have settled.
    val productsLoaded = availableProducts.isNotEmpty()
    LaunchedEffect(isOnHome, subscriptionState.isLoading, productsLoaded) {
        if (isOnHome && !checked && !subscriptionState.isLoading && productsLoaded) {
            checked = true
            if (controller.isEligible(subscriptionState.subscriptionType, monthlyProduct)) {
                config = controller.config()
            }
            HolidayOfferSession.markChecked()
        }
    }

    val activeConfig = config
    val visible = activeConfig != null && !closed && !subscriptionState.isSubscribed &&
        monthlyProduct != null && monthlyProduct.introOfferPrice != null
    val shownAt = remember { TimeSource.Monotonic.markNow() }
    val countdown = remember(activeConfig) { OfferCloseCountdown(activeConfig?.closeDelaySeconds ?: 0) }

    LaunchedEffect(visible) {
        if (visible && activeConfig != null) {
            HolidayOfferSession.markShown()
            controller.markShown(activeConfig.campaignId) // once only, even if the app closes first
            viewModel.trackPaywallViewed(source)
            countdown.run()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        if (visible && uiState.successMessage != null) closed = true
    }

    if (!visible || monthlyProduct == null) return

    val close = {
        if (!subscriptionState.isSubscribed) {
            viewModel.trackPaywallDismissed(source, shownAt.elapsedNow().inWholeSeconds)
        }
        closed = true
    }
    // During the delay the sheet cannot be swiped away, dismissed by tap outside, or closed by back.
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { target -> target != SheetValue.Hidden || countdown.canClose },
    )
    AdCoveringModalBottomSheet(
        onDismissRequest = { if (countdown.canClose) close() },
        sheetState = sheetState,
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = countdown.canClose),
    ) {
        HolidayOfferSheetContent(
            product = monthlyProduct,
            canClose = countdown.canClose,
            closeProgress = countdown.progress,
            isProcessing = uiState.isProcessing,
            errorMessage = uiState.errorMessage,
            onSubscribe = {
                viewModel.trackTierSelected(ProductType.MONTHLY, monthlyProduct.productId, source = source)
                viewModel.purchaseProduct(monthlyProduct, source = source)
            },
            onSeeAllPlans = {
                close()
                onSeeAllPlans()
            },
            onClose = { if (countdown.canClose) close() },
        )
    }
}

@Composable
fun HolidayOfferSheetContent(
    product: ProductInfo,
    canClose: Boolean = true,
    closeProgress: Float = 1f,
    isProcessing: Boolean = false,
    errorMessage: String? = null,
    onSubscribe: () -> Unit = {},
    onSeeAllPlans: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val introPrice = product.introOfferPrice.orEmpty()
    val loadingDescription = stringResource(Res.string.offer_close_loading_description)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Close slot: a filling spinner during the delay, then the X. Same size, so nothing jumps.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                if (canClose) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(Res.string.common_close))
                    }
                } else {
                    CircularProgressIndicator(
                        progress = { closeProgress },
                        modifier = Modifier
                            .size(24.dp)
                            .semantics { contentDescription = loadingDescription },
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
        Text(
            text = stringResource(Res.string.offer_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.offer_product_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BenefitRow(stringResource(Res.string.offer_benefit_no_ads))
            BenefitRow(stringResource(Res.string.offer_benefit_widgets))
            BenefitRow(stringResource(Res.string.offer_benefit_themes))
            BenefitRow(stringResource(Res.string.offer_benefit_more))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.offer_price_line, introPrice, product.formattedPrice),
            style = MaterialTheme.typography.bodyMedium,
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
                Text(stringResource(Res.string.offer_cta, introPrice))
            }
        }
        TextButton(onClick = onSeeAllPlans, enabled = !isProcessing) {
            Text(stringResource(Res.string.offer_see_all_plans))
        }
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

private val previewMonthly = ProductInfo(
    productId = "sln_premium",
    basePlanId = "monthly",
    title = "Space Launch Now Premium", // i18n-ignore: preview sample data
    description = "Monthly subscription", // i18n-ignore: preview sample data
    formattedPrice = "$2.99",
    priceAmountMicros = 2_990_000L,
    currencyCode = "USD",
    hasIntroOffer = true,
    introOfferPrice = "$0.99",
    introOfferPeriodValue = 1,
    introOfferPeriodUnit = "MONTH",
    introOfferCycles = 1,
)

@Preview
@Composable
private fun HolidayOfferSheetPreview() {
    SpaceLaunchNowPreviewTheme { HolidayOfferSheetContent(product = previewMonthly) }
}

@Preview
@Composable
private fun HolidayOfferSheetDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) { HolidayOfferSheetContent(product = previewMonthly) }
}

@Preview
@Composable
private fun HolidayOfferSheetCountdownPreview() {
    SpaceLaunchNowPreviewTheme {
        HolidayOfferSheetContent(product = previewMonthly, canClose = false, closeProgress = 0.4f)
    }
}

@Preview
@Composable
private fun HolidayOfferSheetCountdownDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        HolidayOfferSheetContent(product = previewMonthly, canClose = false, closeProgress = 0.4f)
    }
}
