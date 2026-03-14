package me.calebjones.spacelaunchnow.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.analytics.DatadogLogger
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.ui.components.AppIconBox
import me.calebjones.spacelaunchnow.ui.components.FinePrint
import me.calebjones.spacelaunchnow.ui.compose.LaunchCardHeaderOverlay
import me.calebjones.spacelaunchnow.ui.compose.PlainShimmerCard
import me.calebjones.spacelaunchnow.ui.compose.toLaunchCardData
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.NextUpViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.ProductType
import me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

// ---------------------------------------------------------------------------
// Stateful entry point
// ---------------------------------------------------------------------------

@Composable
fun OnboardingScreen(
    viewModel: SubscriptionViewModel = koinInject(),
    nextUpViewModel: NextUpViewModel = koinViewModel(),
    appPreferences: AppPreferences = koinInject(),
    onComplete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val nextLaunch by nextUpViewModel.nextLaunch.collectAsState()
    val availableProducts by viewModel.availableProducts.collectAsState()

    LaunchedEffect(Unit) {
        nextUpViewModel.fetchNextLaunch()
        DatadogLogger.info(
            "onboarding_paywall_shown",
            mapOf("source" to "onboarding", "screen" to "OnboardingScreen")
        )
    }

    val annualProduct =
        remember(availableProducts) { viewModel.getProductByType(ProductType.ANNUAL) }
    val monthlyProduct =
        remember(availableProducts) { viewModel.getProductByType(ProductType.MONTHLY) }
    val lifetimeProduct =
        remember(availableProducts) { viewModel.getProductByType(ProductType.LIFETIME) }

    // Calculate savings percent (annual vs monthly)
    val savingsPercent: String? = remember(annualProduct, monthlyProduct) {
        val annualMicros = annualProduct?.priceAmountMicros
        val monthlyMicros = monthlyProduct?.priceAmountMicros
        if (annualMicros != null && monthlyMicros != null && monthlyMicros > 0) {
            val annualEquivalentMonthly = annualMicros / 12.0
            val savingsPct =
                ((monthlyMicros - annualEquivalentMonthly) / monthlyMicros * 100).toInt()
            if (savingsPct > 0) "$savingsPct%" else null
        } else null
    }

    // Navigate away on successful purchase
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            coroutineScope.launch {
                appPreferences.setOnboardingCompleted(true)
                appPreferences.setOnboardingPaywallV1Shown(true)
            }
            onComplete()
        }
    }

    OnboardingContent(
        annualProduct = annualProduct,
        monthlyProduct = monthlyProduct,
        lifetimeProduct = lifetimeProduct,
        savingsPercent = savingsPercent,
        isProcessing = uiState.isProcessing,
        errorMessage = uiState.errorMessage,
        billingUnavailable = uiState.billingUnavailable,
        nextLaunch = nextLaunch,
        isSubscribed = subscriptionState.isSubscribed,
        onSubscribe = { product -> viewModel.purchaseProduct(product) },
        onRestorePurchases = { viewModel.restorePurchases() },
        onDismiss = {
            coroutineScope.launch {
                appPreferences.setOnboardingCompleted(true)
                appPreferences.setOnboardingPaywallV1Shown(true)
                onComplete()
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Stateless content — previewable
// ---------------------------------------------------------------------------

@Composable
fun OnboardingContent(
    annualProduct: ProductInfo? = null,
    monthlyProduct: ProductInfo? = null,
    lifetimeProduct: ProductInfo? = null,
    savingsPercent: String? = null,
    isProcessing: Boolean = false,
    errorMessage: String? = null,
    billingUnavailable: Boolean = false,
    nextLaunch: LaunchNormal? = null,
    isSubscribed: Boolean = false,
    onSubscribe: (ProductInfo) -> Unit = {},
    onRestorePurchases: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val spaceGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0A0E2A),
            Color(0xFF1A1040),
            Color(0xFF2A1060),
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(spaceGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // ── Hero section ──────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIconBox(boxSize = 56.dp, iconSize = 44.dp)

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSubscribed) "Welcome Back!" else "Welcome to Space Launch Now",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (isSubscribed) {
                                "You're a Premium member. Thanks for supporting!"
                            } else {
                                "Track every launch, mission, and crew... go ad-free with Premium."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Next launch teaser ────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "NEXT UPCOMING LAUNCH",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    if (nextLaunch != null) {
                        OnboardingNextLaunchCard(launch = nextLaunch)
                    } else {
                        PlainShimmerCard(height = 200, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.15f)
                        )
                        Text(
                            text = "  Explore the App  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.SemiBold
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.15f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OnboardingTrackingFeatureRow()
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // ── Premium features (condensed single card) ──────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.08f)
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        text = "Unlock Premium Features",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OnboardingPerkRow(
                        icon = Icons.Default.Block,
                        label = "Ad-free experience"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OnboardingPerkRow(
                        icon = Icons.Default.Widgets,
                        label = "Additional Premium widgets"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OnboardingPerkRow(
                        icon = Icons.Default.FormatPaint,
                        label = "Exclusive themes & customization"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Primary trial CTA ─────────────────────────────────────────
            if (!isSubscribed) {
                if (annualProduct != null) {
                    val trialLabel = annualProduct.freeTrialPeriodDisplay
                    val ctaLabel =
                        if (trialLabel != null) "Start $trialLabel Free Trial" else "Start Free Trial"
                    val disclosureText =
                        "Then ${annualProduct.formattedPrice}/year. Cancel anytime."

                    Button(
                        onClick = { onSubscribe(annualProduct) },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally

                            ) {
                                Text(
                                    text = ctaLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = disclosureText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // ── Monthly fallback pill (secondary, non-competing) ──
                    if (monthlyProduct != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val monthlyMicros = monthlyProduct.priceAmountMicros
                        val perMonth =
                            if (monthlyMicros > 0) monthlyProduct.formattedPrice else null
                        OutlinedButton(
                            onClick = { onSubscribe(monthlyProduct) },
                            enabled = !isProcessing,
                            modifier = Modifier.fillMaxWidth(),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.25f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White.copy(alpha = 0.6f)
                            )
                        ) {
                            Text(
                                text = buildString {
                                    append("Or try Monthly")
                                    if (perMonth != null) append(" · $perMonth/mo")
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    TextButton(
                        onClick = onDismiss,
                        enabled = !isProcessing
                    ) {
                        Text(
                            text = "Continue for Free",
                            color = Color.White.copy(alpha = 0.35f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else if (billingUnavailable) {
                    // Billing unavailable (emulator, unsupported device, etc.) — skip CTA
                    Text(
                        text = "Purchases not available on this device.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Continue for Free",
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // Still loading products
                    PlainShimmerCard(height = 52, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            // ── Continue to App (subscribed users only) ──────────────────
            if (isSubscribed) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Continue to App",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Legal footer ──────────────────────────────────────────────
            FinePrint(
                dimColor = Color.White.copy(alpha = 0.25f),
                linkColor = Color.White.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Internal sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun OnboardingPerkRow(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun OnboardingPerkCard(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val iconGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun OnboardingPricingCard(
    label: String,
    price: String,
    subLabel: String,
    badge: String?,
    isHighlighted: Boolean,
    trialLabel: String?,
    isProcessing: Boolean,
    onClick: () -> Unit
) {
    val cardContainerColor = if (isHighlighted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.White.copy(alpha = 0.08f)
    }
    val textColor = if (isHighlighted) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isProcessing, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        elevation = if (isHighlighted) CardDefaults.cardElevation(defaultElevation = 6.dp)
        else CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                if (trialLabel != null) {
                    Text(
                        text = trialLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.75f)
                    )
                    Text(
                        text = subLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.5f)
                    )
                } else {
                    Text(
                        text = subLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.75f)
                    )
                }
            }
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = textColor
                )
            } else {
                Text(
                    text = price,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun OnboardingNextLaunchCard(launch: LaunchNormal) {
    val launchCardData = remember(launch) { launch.toLaunchCardData() }
    val imageUrl = launch.image?.imageUrl ?: launch.image?.thumbnailUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // Background: full-size image
            if (imageUrl != null) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Next launch image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            MaterialTheme.colorScheme.surface
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CustomIcons.RocketLaunch,
                                contentDescription = "Launch placeholder",
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CustomIcons.RocketLaunch,
                        contentDescription = "Launch placeholder",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }

            // Launch info overlay (title, agency logo, date)
            LaunchCardHeaderOverlay(
                launchData = launchCardData,
                showAgencyLogo = true,
                logoSize = 56.dp,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun OnboardingTrackingFeatureRow() {
    val features = listOf(
        Pair(Icons.Default.Timer, "Countdown"),
        Pair(Icons.Default.Map, "Map View"),
        Pair(Icons.Default.TravelExplore, "Trajectory"),
        Pair(Icons.Default.Timeline, "Timeline"),
        Pair(Icons.Default.Notifications, "Alerts"),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        features.forEach { (icon, label) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

private val previewAnnualProduct = ProductInfo(
    productId = "sln_premium",
    basePlanId = "annual",
    title = "Space Launch Now Premium",
    description = "Annual subscription",
    formattedPrice = "$9.99",
    priceAmountMicros = 9_990_000L,
    currencyCode = "USD",
    hasFreeTrial = true,
    freeTrialPeriodDisplay = "2-week",
    freeTrialPeriodValue = 2,
    freeTrialPeriodUnit = "WEEK"
)

private val previewMonthlyProduct = ProductInfo(
    productId = "sln_premium",
    basePlanId = "monthly",
    title = "Space Launch Now Premium",
    description = "Monthly subscription",
    formattedPrice = "$1.49",
    priceAmountMicros = 1_490_000L,
    currencyCode = "USD"
)

// Loading — products not yet fetched
@Preview
@Composable
private fun OnboardingLoadingPreview() {
    SpaceLaunchNowPreviewTheme {
        OnboardingContent(savingsPercent = null)
    }
}

// Products loaded, free trial available + monthly pill
@Preview
@Composable
private fun OnboardingTrialPreview() {
    SpaceLaunchNowPreviewTheme {
        OnboardingContent(
            annualProduct = previewAnnualProduct,
            monthlyProduct = previewMonthlyProduct,
            savingsPercent = "38%"
        )
    }
}

@Preview
@Composable
private fun OnboardingTrialDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        OnboardingContent(
            annualProduct = previewAnnualProduct,
            monthlyProduct = previewMonthlyProduct,
            savingsPercent = "38%"
        )
    }
}

// Existing subscriber — welcome back view
@Preview
@Composable
private fun OnboardingSubscribedPreview() {
    SpaceLaunchNowPreviewTheme {
        OnboardingContent(
            annualProduct = previewAnnualProduct,
            savingsPercent = "38%",
            isSubscribed = true
        )
    }
}

@Preview
@Composable
private fun OnboardingSubscribedDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        OnboardingContent(
            annualProduct = previewAnnualProduct,
            savingsPercent = "38%",
            isSubscribed = true
        )
    }
}

// Billing unavailable (emulator / unsupported device)
@Preview
@Composable
private fun OnboardingBillingUnavailablePreview() {
    SpaceLaunchNowPreviewTheme {
        OnboardingContent(billingUnavailable = true)
    }
}

@Preview
@Composable
private fun OnboardingBillingUnavailableDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        OnboardingContent(billingUnavailable = true)
    }
}

// Error state
@Preview
@Composable
private fun OnboardingErrorPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        OnboardingContent(
            annualProduct = previewAnnualProduct,
            savingsPercent = "38%",
            errorMessage = "Purchase failed. Please try again."
        )
    }
}
