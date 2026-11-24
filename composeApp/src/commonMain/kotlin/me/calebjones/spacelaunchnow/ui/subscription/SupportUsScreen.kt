package me.calebjones.spacelaunchnow.ui.subscription

import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.analytics.events.PaywallSource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import me.calebjones.spacelaunchnow.ui.components.PlatformBackHandler
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.calebjones.spacelaunchnow.data.model.SubscriptionState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.ui.components.AppIconBox
import me.calebjones.spacelaunchnow.ui.components.FinePrint
import me.calebjones.spacelaunchnow.ui.platformShadowGlow
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.ProductType
import me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.launcher
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.pluralStringResource
import spacelaunchnow_kmp.composeapp.generated.resources.common_back
import spacelaunchnow_kmp.composeapp.generated.resources.common_calendar_sync
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_active_calendar_sync
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_active_no_ads
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_active_supporting_description
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_active_supporting_title
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_active_themes
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_active_widget_android
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_active_widget_ios
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_best_value
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_choose_plan
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_choose_upgrade
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_continue_support
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_copy_user_id_description
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_current_plan
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_free_trial
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_free_trial_badge
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_free_trial_days_remaining
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_free_user
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_header_join_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_header_love_title
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_header_member_type
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_header_thank_you
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_legacy_banner_body
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_legacy_banner_prompt
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_legacy_banner_title
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_legacy_benefits_heading
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_lifetime_max_support
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_lifetime_one_time
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_lifetime_perk_access
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_lifetime_perk_subscriptions
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_lifetime_perk_support
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_lifetime_tagline
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_lifetime_title
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_loading_pricing
import spacelaunchnow_kmp.composeapp.generated.resources.common_app_name
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_making_difference
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_member_plan
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_open_form_description
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_or_go_lifetime
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_period_month
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_period_year
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_perk_no_ads_description
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_perk_no_ads_title
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_perk_support_description
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_perk_support_title
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_perk_themes_description
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_perk_themes_title
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_perk_widget_description_android
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_perk_widget_description_ios
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_perk_widget_title
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_plan_monthly
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_plan_yearly
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_premium_features_heading
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_product_line
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_restore_help_body
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_restore_help_title
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_restore_purchases
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_restore_purchases_member
import spacelaunchnow_kmp.composeapp.generated.resources.common_start_free_trial
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_subscribe_now
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_thanks_legacy
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_thanks_member
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_then_price_after_trial
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_unlock_pro_forever
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_upgrade_prompt
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_upgrade_to_lifetime
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_user_id_label
import spacelaunchnow_kmp.composeapp.generated.resources.support_us_user_id_unavailable

/**
 * "Support Us" screen - Encouraging membership with focused perks
 *
 * Main selling points:
 * - Premium Widget
 * - Ad-Free Experience
 * - Support Development
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportUsScreen(
    viewModel: SubscriptionViewModel = koinInject(),
    source: String = PaywallSource.SUPPORT_US.value,
    onNavigateBack: () -> Unit = {}
) {
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Available products from BillingManager (platform-agnostic)
    val availableProducts by viewModel.availableProducts.collectAsState()

    // Get products by type using helper function
    val lifetimeProduct = viewModel.getProductByType(ProductType.LIFETIME)
    val annualProduct = viewModel.getProductByType(ProductType.ANNUAL)
    val monthlyProduct = viewModel.getProductByType(ProductType.MONTHLY)

    // Funnel top (spec 014 FR-1): once per presentation, mirroring OnboardingPaywallScreen.
    LaunchedEffect(Unit) {
        viewModel.trackPaywallViewed(source)
    }

    // Dismiss: any exit (top-bar arrow, system back, iOS swipe-back, desktop window close)
    // without a subscription, once per presentation.
    val dismissTracker = remember(source) { PaywallDismissTracker(viewModel, source) }
    val leave = {
        dismissTracker.dismiss()
        onNavigateBack()
    }
    PlatformBackHandler { leave() }
    DismissOnLeaveComposition(dismissTracker)

    // Determine user status for different upgrade flows
    val subscriptionRepo = koinInject<SubscriptionRepository>()
    val fullSubscriptionState by subscriptionRepo.state.collectAsState()
    val isLegacy = subscriptionState.subscriptionType.isLegacy
    val hasCurrentSubscription = subscriptionState.isSubscribed
    val canUpgrade = !subscriptionState.isSubscribed || isLegacy

    // Determine which packages to show based on user's current state
    val userState = determineUserState(subscriptionState)
    val packagesToShow = getPackagesToShow(userState)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
//                    Text(
//                        text = "Space Launch Now", // i18n-ignore: commented-out code
//                        style = MaterialTheme.typography.headlineMedium,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 36.sp,
//                        color = MaterialTheme.colorScheme.onPrimaryContainer,
//                    )
                },
                navigationIcon = {
                    IconButton(onClick = leave) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Section
            item {
                HeroSection(
                    isSubscribed = subscriptionState.isSubscribed,
                    subscriptionType = subscriptionState.subscriptionType
                )
            }

            // Products status (subtle indicator)
            if (availableProducts.isEmpty() && canUpgrade) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.support_us_loading_pricing),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

//            // Current Plan Section (at the top for subscribers)
//            if (subscriptionState.isSubscribed) {
//                item {
//                    Spacer(Modifier.height(24.dp))
//                    CurrentPlanCard(
//                        subscriptionState = subscriptionState,
//                        onVerifySubscription = { viewModel.verifySubscription(forceRefresh = true) },
//                        onRestorePurchases = { viewModel.restorePurchases() },
//                        isProcessing = uiState.isProcessing
//                    )
//                }
//            }

            // Legacy User Upgrade Encouragement Banner (special message for legacy supporters)
            if (isLegacy) {
                item {
                    Spacer(Modifier.height(16.dp))
                    LegacyUserUpgradeBanner()
                }
            }

            if (packagesToShow.showPerks) {
                if (canUpgrade) {
                    item {
                        val headerText = when (subscriptionState.subscriptionType) {
                            SubscriptionType.LEGACY -> stringResource(Res.string.support_us_continue_support)
                            SubscriptionType.PREMIUM -> stringResource(Res.string.support_us_upgrade_to_lifetime)
                            SubscriptionType.LIFETIME -> ""
                            else -> ""
                        }
                        subscriptionState.subscriptionType.isLegacy
                        Spacer(Modifier.height(24.dp))
                        if (headerText.isNotEmpty()) {
                            Text(
                                text = headerText,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    // Premium Widget Perk
                    item {
                        Spacer(Modifier.height(24.dp))
                        PremiumPerkCard(
                            icon = Icons.Default.Widgets,
                            title = stringResource(Res.string.support_us_perk_widget_title),
                            description = if (getPlatform().type.isIOS)
                                stringResource(Res.string.support_us_perk_widget_description_ios)
                            else
                                stringResource(Res.string.support_us_perk_widget_description_android),
                            gradient = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        PremiumPerkCard(
                            icon = Icons.Default.FormatPaint,
                            title = stringResource(Res.string.support_us_perk_themes_title),
                            description = stringResource(Res.string.support_us_perk_themes_description),
                            gradient = listOf(
                                MaterialTheme.colorScheme.error,
                                MaterialTheme.colorScheme.inversePrimary
                            )
                        )
                    }

                    // Ad-Free Perk
                    item {
                        Spacer(Modifier.height(16.dp))
                        PremiumPerkCard(
                            icon = Icons.Default.Block,
                            title = stringResource(Res.string.support_us_perk_no_ads_title),
                            description = stringResource(Res.string.support_us_perk_no_ads_description),
                            gradient = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }

                    // Support Development
                    item {
                        Spacer(Modifier.height(16.dp))
                        PremiumPerkCard(
                            icon = Icons.Default.Favorite,
                            title = stringResource(Res.string.support_us_perk_support_title),
                            description = stringResource(Res.string.support_us_perk_support_description),
                            gradient = listOf(
                                MaterialTheme.colorScheme.error,
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            if (packagesToShow.showLifetime || packagesToShow.showAnnual || packagesToShow.showMonthly) {
                item {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = if (subscriptionState.isSubscribed) stringResource(Res.string.support_us_choose_upgrade) else stringResource(Res.string.support_us_choose_plan),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Annual Plan (Recommended — anchored first) - Show if available in packagesToShow
            if (packagesToShow.showAnnual) {
                item {
                    PricingCard(
                        title = stringResource(Res.string.support_us_plan_yearly),
                        price = annualProduct?.formattedPrice ?: "$-.--",
                        period = stringResource(Res.string.support_us_period_year),
                        savings = uiState.getAnnualMonthlyEquivalent(),
                        isRecommended = true,
                        isProcessing = uiState.isProcessing,
                        onSubscribe = {
                            if (annualProduct != null) {
                                viewModel.trackTierSelected(ProductType.ANNUAL, annualProduct.productId, source)
                                viewModel.purchaseProduct(annualProduct, source)
                            }
                        },
                        enabled = annualProduct != null,
                        hasFreeTrial = annualProduct?.hasFreeTrial ?: false,
                        freeTrialPeriodDisplay = annualProduct?.freeTrialPeriodDisplay
                    )
                }
            }

            // Monthly Plan - Show if available in packagesToShow
            if (packagesToShow.showMonthly) {
                item {
                    Spacer(Modifier.height(12.dp))

                    PricingCard(
                        title = stringResource(Res.string.support_us_plan_monthly),
                        price = monthlyProduct?.formattedPrice ?: "$-.--",
                        period = stringResource(Res.string.support_us_period_month),
                        isRecommended = false,
                        isProcessing = uiState.isProcessing,
                        onSubscribe = {
                            if (monthlyProduct != null) {
                                viewModel.trackTierSelected(ProductType.MONTHLY, monthlyProduct.productId, source)
                                viewModel.purchaseProduct(monthlyProduct, source)
                            }
                        },
                        enabled = monthlyProduct != null,
                        introPriceLine = introPriceText(monthlyProduct)
                    )
                }
            }

            // Pro Lifetime — shown last, for power users who want to commit
            if (packagesToShow.showLifetime) {
                item {
                    // "Or go lifetime" divider
                    if (packagesToShow.showAnnual || packagesToShow.showMonthly) {
                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = stringResource(Res.string.support_us_or_go_lifetime),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    ProLifetimeCard(
                        price = lifetimeProduct?.formattedPrice ?: "$-.--",
                        isProcessing = uiState.isProcessing,
                        onPurchase = {
                            if (lifetimeProduct != null) {
                                viewModel.trackTierSelected(ProductType.LIFETIME, lifetimeProduct.productId, source)
                                viewModel.purchaseProduct(lifetimeProduct, source)
                            }
                        },
                        enabled = lifetimeProduct != null
                    )
                }
            }

            if (subscriptionState.isSubscribed) {
                item {
                    Spacer(Modifier.height(24.dp))
                    ThankYouSection(
                        subscriptionType = subscriptionState.subscriptionType,
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }


            // Current Plan Section (at bottom for non-subscribers)
            if (!subscriptionState.isSubscribed) {
                item {
                    Spacer(Modifier.height(24.dp))
                    CurrentPlanCard(
                        subscriptionState = subscriptionState,
                        onVerifySubscription = { viewModel.verifySubscription(forceRefresh = true) },
                        onRestorePurchases = { viewModel.restorePurchases(source) },
                        isProcessing = uiState.isProcessing
                    )
                }
            }

            // Restore Purchases Button (always visible for all users)
            item {
                Spacer(Modifier.height(16.dp))

                // Restore Purchases
                TextButton(
                    onClick = { viewModel.restorePurchases(source) },
                    modifier = Modifier.padding(horizontal = 24.dp),
                    enabled = !uiState.isProcessing
                ) {
                    Text(
                        if (!subscriptionState.isSubscribed) stringResource(Res.string.support_us_restore_purchases_member)
                        else stringResource(Res.string.support_us_restore_purchases)
                    )
                }
            }

            // Error/Success Messages
            item {
                Spacer(Modifier.height(16.dp))
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.errorMessage?.let { error ->
                        ErrorCard(message = error)
                    }
                }

                AnimatedVisibility(
                    visible = uiState.successMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.successMessage?.let { success ->
                        SuccessCard(message = success)
                    }
                }
            }

            // RevenueCat User ID for Support
            item {
                RevenueCatUserIdCard(viewModel = viewModel)
            }

            // Google Form Link (Android only)
            if (getPlatform().type.isAndroid) {
                item {
                    Spacer(Modifier.height(16.dp))
                    GoogleFormLinkCard()
                }
            }

            // Fine Print
            item {
                Spacer(Modifier.height(24.dp))
                FinePrint(
                    hasTrialOffer = uiState.hasAnyTrialOffer,
                    trialPeriodDisplay = uiState.trialPeriodDisplay,
                    postTrialPrice = uiState.trialPostPrice
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // Clear messages after showing
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        kotlinx.coroutines.delay(5000)
        viewModel.clearMessages()
    }
}

@Composable
private fun HeroSection(
    isSubscribed: Boolean,
    subscriptionType: SubscriptionType
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppIconBox()

            if (isSubscribed) {
                Text(
                    text = stringResource(Res.string.support_us_header_thank_you),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(
                        Res.string.support_us_header_member_type,
                        subscriptionType.name.lowercase()
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = stringResource(Res.string.support_us_header_love_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(Res.string.support_us_header_join_subtitle),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PremiumPerkCard(
    icon: ImageVector,
    title: String,
    description: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Gradient Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PricingCard(
    title: String,
    price: String,
    period: String,
    savings: String? = null,
    isRecommended: Boolean = false,
    isProcessing: Boolean = false,
    onSubscribe: () -> Unit,
    enabled: Boolean = true,
    hasFreeTrial: Boolean = false,
    freeTrialPeriodDisplay: String? = null,
    introPriceLine: String? = null,
    subscribeButtonText: String = if (hasFreeTrial) stringResource(Res.string.common_start_free_trial) else stringResource(Res.string.support_us_subscribe_now)
) {
    val glowPrimary = MaterialTheme.colorScheme.primary
    val glowTertiary = MaterialTheme.colorScheme.tertiary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .then(
                if (isRecommended) Modifier.platformShadowGlow(
                    gradientColors = listOf(
                        glowPrimary.copy(alpha = 0.1f),
                        glowTertiary.copy(alpha = 0.3f)
                    ),
                    borderRadius = 7.dp,
                    blurRadius = 12.dp,
                    spread = 3.dp
                ) else Modifier
            ),
        colors = if (isRecommended) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        },
        elevation = if (isRecommended) {
            CardDefaults.cardElevation(defaultElevation = 8.dp)
        } else {
            CardDefaults.cardElevation()
        }
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecommended) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )

                if (isRecommended) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = stringResource(Res.string.support_us_best_value),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Price
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecommended) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = period,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 6.dp),
                    color = if (isRecommended) MaterialTheme.colorScheme.onPrimaryFixedVariant else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Intro price (first-month offer), under the normal price
            introPriceLine?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isRecommended) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.tertiary
                )
            }

            // Savings
            savings?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecommended) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }

            // Free Trial Badge
            if (hasFreeTrial && freeTrialPeriodDisplay != null) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = stringResource(Res.string.support_us_free_trial_badge, freeTrialPeriodDisplay),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Text(
                    text = stringResource(Res.string.support_us_then_price_after_trial, price, period),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isRecommended) MaterialTheme.colorScheme.onPrimaryFixedVariant else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Subscribe Button
            Button(
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled && !isProcessing,
                colors = if (isRecommended) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(
                    text = subscribeButtonText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PerkCheckmark(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ThankYouSection(
    subscriptionType: SubscriptionType,
    viewModel: SubscriptionViewModel,
    uiState: me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionUiState
) {
    val isLegacy = subscriptionType.isLegacy

    val textColor = MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.launcher),
                contentDescription = stringResource(Res.string.common_app_name),
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = stringResource(Res.string.support_us_making_difference),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = textColor
            )

            Text(
                text = if (isLegacy) {
                    stringResource(Res.string.support_us_thanks_legacy)
                } else {
                    stringResource(
                        Res.string.support_us_thanks_member,
                        subscriptionType.name.lowercase()
                    )
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = textColor
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = if (isLegacy) stringResource(Res.string.support_us_legacy_benefits_heading) else stringResource(Res.string.support_us_premium_features_heading),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isLegacy) {
                    ActivePerkItem(
                        icon = Icons.Default.Widgets,
                        title = stringResource(Res.string.support_us_perk_widget_title),
                        description = if (getPlatform().type.isIOS)
                            stringResource(Res.string.support_us_active_widget_ios)
                        else
                            stringResource(Res.string.support_us_active_widget_android)
                    )
                    ActivePerkItem(
                        icon = Icons.Default.Block,
                        title = stringResource(Res.string.support_us_perk_no_ads_title),
                        description = stringResource(Res.string.support_us_active_no_ads)
                    )
                    ActivePerkItem(
                        icon = Icons.Default.CalendarMonth,
                        title = stringResource(Res.string.common_calendar_sync),
                        description = stringResource(Res.string.support_us_active_calendar_sync)
                    )
                    ActivePerkItem(
                        icon = Icons.Default.Favorite,
                        title = stringResource(Res.string.support_us_active_supporting_title),
                        description = stringResource(Res.string.support_us_active_supporting_description)
                    )
                } else {
                    // Current premium users get full features
                    ActivePerkItem(
                        icon = Icons.Default.Widgets,
                        title = stringResource(Res.string.support_us_perk_widget_title),
                        description = if (getPlatform().type.isIOS)
                            stringResource(Res.string.support_us_active_widget_ios)
                        else
                            stringResource(Res.string.support_us_active_widget_android)
                    )
                    ActivePerkItem(
                        icon = Icons.Default.FormatPaint,
                        title = stringResource(Res.string.support_us_perk_themes_title),
                        description = stringResource(Res.string.support_us_active_themes)
                    )
                    ActivePerkItem(
                        icon = Icons.Default.Block,
                        title = stringResource(Res.string.support_us_perk_no_ads_title),
                        description = stringResource(Res.string.support_us_active_no_ads)
                    )
                    ActivePerkItem(
                        icon = Icons.Default.CalendarMonth,
                        title = stringResource(Res.string.common_calendar_sync),
                        description = stringResource(Res.string.support_us_active_calendar_sync)
                    )
                    ActivePerkItem(
                        icon = Icons.Default.Favorite,
                        title = stringResource(Res.string.support_us_active_supporting_title),
                        description = stringResource(Res.string.support_us_active_supporting_description)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivePerkItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryFixed,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryFixedVariant,
        )
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun SuccessCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun RevenueCatUserIdCard(viewModel: SubscriptionViewModel) {
    val billingManager = koinInject<me.calebjones.spacelaunchnow.data.billing.BillingManager>()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    val appUserId = remember { billingManager.getAppUserId() }
    val hasUserId = appUserId != null
    val userId = appUserId ?: stringResource(Res.string.support_us_user_id_unavailable)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable(enabled = hasUserId) {
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(userId))
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.support_us_user_id_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userId,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            if (hasUserId) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(Res.string.support_us_copy_user_id_description),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


@Composable
private fun GoogleFormLinkCard() {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable {
                uriHandler.openUri("https://docs.google.com/forms/d/e/1FAIpQLSehYiA23d3YYVZOCTDe7XCNGvydK1kgrXhzIOg3-D-inmLBUg/viewform?usp=dialog")
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Feedback,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.support_us_restore_help_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(Res.string.support_us_restore_help_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = stringResource(Res.string.support_us_open_form_description),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProLifetimeCard(
    price: String,
    isProcessing: Boolean,
    onPurchase: () -> Unit,
    enabled: Boolean = true
) {
    val goldGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFFA500), // Orange
            Color(0xFFFFD700)  // Gold
        )
    )

    val goldSurfaceColor = Color(0xFFFFFAF0) // Floral white with gold tint
    val goldSurfaceColorDark = Color(0xFF2A2416) // Dark background with gold tint

    val surfaceColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        goldSurfaceColor
    } else {
        goldSurfaceColorDark
    }

    // Main card with shadow glow effect
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
//            .platformShadowGlow(
//                gradientColors = listOf(
//                    Color(0xFFFFD700).copy(alpha = 0.6f), // Gold
//                    Color(0xFFFFA500).copy(alpha = 0.5f), // Orange
//                    Color(0xFFFF9800).copy(alpha = 0.7f)  // Yellow
//                ),
//                borderRadius = 10.dp,
//                blurRadius = 12.dp,
//                offsetX = 0.dp,
//                offsetY = 0.dp,
//                spread = 8.dp,
//                enableBreathingEffect = true,
//                breathingEffectIntensity = 4.dp,
//                breathingDurationMillis = 3000
//            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Crown Icon + Title Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = Color(0xFFFFD700), // Gold
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = stringResource(Res.string.support_us_lifetime_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB8860B) // Dark goldenrod
                    )
                    Text(
                        text = stringResource(Res.string.support_us_lifetime_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Price with shimmer effect
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFB8860B) // Dark goldenrod
                )
                Text(
                    text = stringResource(Res.string.support_us_lifetime_one_time),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Best Value Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(Res.string.support_us_lifetime_max_support),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB8860B),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Premium Features List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProPerkCheckmark(stringResource(Res.string.support_us_lifetime_perk_subscriptions))
                ProPerkCheckmark(stringResource(Res.string.support_us_lifetime_perk_access))
                ProPerkCheckmark(stringResource(Res.string.support_us_lifetime_perk_support))
            }

            // Purchase Button with Gold Gradient
            Button(
                onClick = onPurchase,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = enabled && !isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF1C1C1C)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.support_us_unlock_pro_forever),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ProPerkCheckmark(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFFFFD700), // Gold checkmark
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CurrentPlanCard(
    subscriptionState: SubscriptionState,
    onVerifySubscription: () -> Unit,
    onRestorePurchases: () -> Unit,
    isProcessing: Boolean
) {
    val surfaceColor = if (subscriptionState.isSubscribed) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (subscriptionState.isSubscribed) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.support_us_current_plan),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            // Plan Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (subscriptionState.isSubscribed) Icons.Default.CheckCircle else Icons.Default.Star,
                    contentDescription = null,
                    tint = if (subscriptionState.isSubscribed) {
                        MaterialTheme.colorScheme.onPrimaryFixedVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = when {
                        subscriptionState.isInTrialPeriod -> {
                            val daysLeft = subscriptionState.trialExpiresAt?.let { expires ->
                                @OptIn(kotlin.time.ExperimentalTime::class)
                                val msRemaining =
                                    expires - kotlin.time.Clock.System.now().toEpochMilliseconds()
                                val days = (msRemaining / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                                days
                            }
                            if (daysLeft != null && daysLeft > 0) {
                                pluralStringResource(
                                    Res.plurals.support_us_free_trial_days_remaining,
                                    daysLeft.toInt(),
                                    daysLeft.toInt()
                                )
                            } else {
                                stringResource(Res.string.support_us_free_trial)
                            }
                        }

                        subscriptionState.isSubscribed -> stringResource(
                            Res.string.support_us_member_plan,
                            subscriptionState.subscriptionType.name.replaceFirstChar { it.uppercase() }
                        )
                        else -> stringResource(Res.string.support_us_free_user)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            // Expiry/Product Info
            if (subscriptionState.isSubscribed) {
                subscriptionState.productId?.let { productId ->
                    Text(
                        text = stringResource(
                            Res.string.support_us_product_line,
                            getProductDisplayName(productId)
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor
                    )
                }
            } else {
                Text(
                    text = stringResource(Res.string.support_us_upgrade_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
            subscriptionState.features.forEach { feature ->
                BenefitItem(
                    icon = Icons.Filled.CheckCircle,
                    feature.getTitle(),
                    color = textColor
                )

            }
        }
    }
}

// Helper function to format dates (simplified)
private fun formatDate(epochMillis: Long): String {
    // TODO: Implement proper date formatting
    return "Date formatting TBD"
}

// Helper function to get user-friendly product names
private fun getProductDisplayName(productId: String): String {
    return me.calebjones.spacelaunchnow.data.billing.SubscriptionProducts.getProductDisplayName(
        productId
    )
}

/**
 * Legacy User Upgrade Banner
 *
 * Compelling banner encouraging legacy supporters to upgrade to new subscription plans.
 * Shows appreciation for past support while highlighting benefits of continuing support.
 */
@Composable
private fun LegacyUserUpgradeBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with heart icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryFixedVariant,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = stringResource(Res.string.support_us_legacy_banner_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            // Appreciation message
            Text(
                text = stringResource(Res.string.support_us_legacy_banner_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                lineHeight = 22.sp
            )

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f)
            )

            // Benefits of upgrading
            Text(
                text = stringResource(Res.string.support_us_continue_support),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            // Call to action
            Text(
                text = stringResource(Res.string.support_us_legacy_banner_prompt),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(top = 8.dp),
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Benefit Item for Legacy Banner
 * Simple row showing an icon and benefit text
 */
@Composable
private fun BenefitItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            lineHeight = 20.sp
        )
    }
}

/**
 * User State enum - Represents the 5 different user states for package filtering
 */
enum class UserState {
    NEW_USER,           // No previous SKUs
    LEGACY_USER,        // Has legacy SKUs
    MONTHLY_SUBSCRIBER, // Active monthly subscription
    ANNUAL_SUBSCRIBER,  // Active annual subscription
    LIFETIME_USER       // Has lifetime purchase
}

/**
 * Packages to Show - Flags indicating which packages to display
 */
data class PackagesToShow(
    val showLifetime: Boolean,
    val showAnnual: Boolean,
    val showMonthly: Boolean,
    val showPerks: Boolean
)

/**
 * Determine the user's current state based on their subscription
 *
 * @param subscriptionState Current subscription state from repository
 * @return UserState enum representing user's subscription status
 */
private fun determineUserState(
    subscriptionState: SubscriptionState
): UserState {
    return when (subscriptionState.subscriptionType) {
        SubscriptionType.LIFETIME -> UserState.LIFETIME_USER
        SubscriptionType.PREMIUM -> {
            // For Premium users, determine if monthly or annual based on productId
            val isMonthly = subscriptionState.productId?.let { productId ->
                productId.contains("monthly", ignoreCase = true) ||
                        productId.contains("base-plan", ignoreCase = true) ||
                        productId.contains("base_plan", ignoreCase = true)
            } ?: false

            if (isMonthly) UserState.MONTHLY_SUBSCRIBER else UserState.ANNUAL_SUBSCRIBER
        }

        SubscriptionType.LEGACY -> UserState.LEGACY_USER
        SubscriptionType.FREE -> UserState.NEW_USER
    }
}

/**
 * Get packages to show based on user state
 *
 * Rules:
 * 1) New User - Show all (lifetime, annual, monthly)
 * 2) Legacy User - Show lifetime and monthly (no annual - they can upgrade to Pro monthly or go straight to lifetime)
 * 3) Monthly Subscriber (Pro) - Show lifetime only (already have Pro, can only upgrade to lifetime)
 * 4) Annual Subscriber (Pro) - Show lifetime only (already have Pro, can only upgrade to lifetime)
 * 5) Lifetime User - Show nothing (they have the best tier)
 *
 * @param userState The user's current state
 * @return PackagesToShow with boolean flags for each package type
 */
private fun getPackagesToShow(
    userState: UserState
): PackagesToShow {
    return when (userState) {
        UserState.NEW_USER -> PackagesToShow(
            showLifetime = true,
            showAnnual = true,
            showMonthly = true,
            showPerks = true
        )

        UserState.LEGACY_USER -> PackagesToShow(
            showLifetime = true,   // Show lifetime upgrade option
            showAnnual = true,    // Don't show annual (they can upgrade to lifetime)
            showMonthly = true,     // Show monthly Pro option
            showPerks = false
        )

        UserState.MONTHLY_SUBSCRIBER -> PackagesToShow(
            showLifetime = true,   // Pro users get lifetime upgrade option only
            showAnnual = true,    // Don't show annual (they're already Pro)
            showMonthly = false,    // Don't show monthly (they already have it)
            showPerks = true

        )

        UserState.ANNUAL_SUBSCRIBER -> PackagesToShow(
            showLifetime = true,   // Pro users get lifetime upgrade option only
            showAnnual = false,    // Don't show annual (they already have it)
            showMonthly = false,    // Don't show monthly (they're already Pro)
            showPerks = true
        )

        UserState.LIFETIME_USER -> PackagesToShow(
            showLifetime = false,  // They already have lifetime
            showAnnual = false,    // No downgrades
            showMonthly = false,    // No downgrades
            showPerks = false
        )
    }
}

// ==================== Previews ====================

@Preview
@Composable
private fun PricingCardTrialPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface {
            PricingCard(
                title = stringResource(Res.string.support_us_plan_yearly),
                price = "$9.99",
                period = stringResource(Res.string.support_us_period_year),
                savings = "Save 58%!",
                isRecommended = true,
                isProcessing = false,
                onSubscribe = {},
                enabled = true,
                hasFreeTrial = true,
                freeTrialPeriodDisplay = "3-day"
            )
        }
    }
}

@Preview
@Composable
private fun PricingCardTrialDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface {
            PricingCard(
                title = stringResource(Res.string.support_us_plan_yearly),
                price = "$9.99",
                period = stringResource(Res.string.support_us_period_year),
                savings = "Save 58%!",
                isRecommended = true,
                isProcessing = false,
                onSubscribe = {},
                enabled = true,
                hasFreeTrial = true,
                freeTrialPeriodDisplay = "3-day"
            )
        }
    }
}

@Preview
@Composable
private fun PricingCardNoTrialPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface {
            PricingCard(
                title = stringResource(Res.string.support_us_plan_monthly),
                price = "$2.99",
                period = stringResource(Res.string.support_us_period_month),
                isRecommended = false,
                isProcessing = false,
                onSubscribe = {},
                enabled = true
            )
        }
    }
}

@Preview
@Composable
private fun PricingCardIntroPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface {
            PricingCard(
                title = stringResource(Res.string.support_us_plan_monthly),
                price = "$2.99",
                period = stringResource(Res.string.support_us_period_month),
                isRecommended = false,
                isProcessing = false,
                onSubscribe = {},
                enabled = true,
                introPriceLine = introPriceText(previewIntroMonthlyProduct)
            )
        }
    }
}

@Preview
@Composable
private fun PricingCardIntroDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface {
            PricingCard(
                title = stringResource(Res.string.support_us_plan_monthly),
                price = "$2.99",
                period = stringResource(Res.string.support_us_period_month),
                isRecommended = false,
                isProcessing = false,
                onSubscribe = {},
                enabled = true,
                introPriceLine = introPriceText(previewIntroMonthlyProduct)
            )
        }
    }
}

private val previewIntroMonthlyProduct = ProductInfo(
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
    introOfferCycles = 1
)

@Preview
@Composable
private fun FinePrintTrialPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface {
            FinePrint(
                hasTrialOffer = true,
                trialPeriodDisplay = "3-day",
                postTrialPrice = "$9.99/year"
            )
        }
    }
}

@Preview
@Composable
private fun FinePrintTrialDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface {
            FinePrint(
                hasTrialOffer = true,
                trialPeriodDisplay = "3-day",
                postTrialPrice = "$9.99/year"
            )
        }
    }
}

@Preview
@Composable
private fun FinePrintNoTrialPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface {
            FinePrint()
        }
    }
}

@Preview
@Composable
private fun CurrentPlanCardTrialPreview() {
    SpaceLaunchNowPreviewTheme {
        Surface {
            CurrentPlanCard(
                subscriptionState = SubscriptionState(
                    isSubscribed = true,
                    subscriptionType = me.calebjones.spacelaunchnow.data.model.SubscriptionType.PREMIUM,
                    isInTrialPeriod = true,
                    trialExpiresAt = null
                ),
                onVerifySubscription = {},
                onRestorePurchases = {},
                isProcessing = false
            )
        }
    }
}

@Preview
@Composable
private fun CurrentPlanCardTrialDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        Surface {
            CurrentPlanCard(
                subscriptionState = SubscriptionState(
                    isSubscribed = true,
                    subscriptionType = me.calebjones.spacelaunchnow.data.model.SubscriptionType.PREMIUM,
                    isInTrialPeriod = true,
                    trialExpiresAt = null
                ),
                onVerifySubscription = {},
                onRestorePurchases = {},
                isProcessing = false
            )
        }
    }
}
