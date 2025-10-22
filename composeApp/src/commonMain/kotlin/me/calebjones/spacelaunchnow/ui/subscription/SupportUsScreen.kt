package me.calebjones.spacelaunchnow.ui.subscription

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.launcher
import me.calebjones.spacelaunchnow.data.billing.SubscriptionProducts
import me.calebjones.spacelaunchnow.data.model.SubscriptionState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.ui.platformShadowGlow
import me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

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
    onNavigateBack: () -> Unit = {}
) {
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // RevenueCat offerings for dynamic pricing
    val currentOffering by viewModel.currentOffering.collectAsState()

    // Determine user status for different upgrade flows
    val subscriptionRepo = koinInject<SubscriptionRepository>()
    val fullSubscriptionState by subscriptionRepo.state.collectAsState()
    val isLegacy = subscriptionState.subscriptionType.isLegacy
    val hasCurrentSubscription = subscriptionState.isSubscribed && !isLegacy
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
//                        text = "Space Launch Now",
//                        style = MaterialTheme.typography.headlineMedium,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 36.sp,
//                        color = MaterialTheme.colorScheme.onPrimaryContainer,
//                    )
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

            // RevenueCat offerings status (subtle indicator)
            if (currentOffering == null && canUpgrade) {
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
                            text = "💰 Loading pricing from store...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Current Plan Section (at the top for subscribers)
            if (subscriptionState.isSubscribed) {
                item {
                    Spacer(Modifier.height(24.dp))
                    CurrentPlanCard(
                        subscriptionState = subscriptionState,
                        onVerifySubscription = { viewModel.verifySubscription(forceRefresh = true) },
                        onRestorePurchases = { viewModel.restorePurchases() },
                        isProcessing = uiState.isProcessing
                    )
                }
            }

            // Legacy User Upgrade Encouragement Banner (special message for legacy supporters)
            if (isLegacy) {
                item {
                    Spacer(Modifier.height(16.dp))
                    LegacyUserUpgradeBanner()
                }
            }

            // Show full upgrade options for non-subscribers AND legacy users
            if (canUpgrade) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = if (!subscriptionState.isSubscribed) "Become a Member" else "Upgrade to Premium",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (!subscriptionState.isSubscribed)
                            "Unlock premium features and support development"
                        else
                            "Unlock all premium features including widgets and more!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                // Premium Widget Perk
                item {
                    Spacer(Modifier.height(24.dp))
                    PremiumPerkCard(
                        icon = Icons.Default.Widgets,
                        title = "Premium Widget",
                        description = "Unlock additional Launch List widget for your home screen",
                        gradient = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                }

                item {
                    Spacer(Modifier.height(24.dp))
                    PremiumPerkCard(
                        icon = Icons.Default.DashboardCustomize,
                        title = "Widget Customization",
                        description = "Customize widgets with customizable themes and layouts",
                        gradient = listOf(
                            MaterialTheme.colorScheme.primaryFixed,
                            MaterialTheme.colorScheme.tertiaryFixed
                        )
                    )
                }

                item {
                    Spacer(Modifier.height(24.dp))
                    PremiumPerkCard(
                        icon = Icons.Default.EditCalendar,
                        title = "Calendar Sync",
                        description = "Access to Calendar Sync link for Launch and Event calendar sync",
                        gradient = listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.inversePrimary
                        )
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    PremiumPerkCard(
                        icon = Icons.Default.FormatPaint,
                        title = "Premium Themes",
                        description = "Utilize premium themes to customize your app experience",
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
                        title = "No Ads",
                        description = "Enjoy an uninterrupted, ad-free experience throughout the entire app",
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
                        title = "Support Development",
                        description = "Help us continue improving the app and adding new features",
                        gradient = listOf(
                            MaterialTheme.colorScheme.error,
                            MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Pricing Cards
                item {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = "Choose Your Plan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Pro Lifetime (Golden Premium Option) - Show if available in packagesToShow
                if (packagesToShow.showLifetime) {
                    item {
                        val lifetimePackage = currentOffering?.lifetime

                        // Only show if we have the actual package from RevenueCat

                        ProLifetimeCard(
                            price = lifetimePackage?.storeProduct?.price?.formatted ?: "$-.--",
                            isProcessing = uiState.isProcessing,
                            onPurchase = {
                                if (lifetimePackage != null) {
                                    viewModel.purchasePackage(lifetimePackage)
                                }
                            },
                            enabled = lifetimePackage != null
                        )

                        // Show divider only if lifetime was shown AND there are subscription plans to show
                        if (packagesToShow.showAnnual || packagesToShow.showMonthly) {
                            Spacer(Modifier.height(20.dp))

                            // "Or subscribe" divider
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f))
                                Text(
                                    text = "  Or subscribe  ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                        // Don't show fallback UI or divider - just hide until loaded
                    }
                }

                // Yearly Plan (Recommended) - Show if available in packagesToShow
                if (packagesToShow.showAnnual) {
                    item {
                        val annualPackage = currentOffering?.annual

                        PricingCard(
                            title = "Yearly",
                            price = annualPackage?.storeProduct?.price?.formatted ?: "$-.--",
                            period = "/year",
                            savings = uiState.getSavingsPercent(),
                            isRecommended = true,
                            isProcessing = uiState.isProcessing,
                            onSubscribe = {
                                if (annualPackage != null) {
                                    viewModel.purchasePackage(annualPackage)
                                }
                            },
                            enabled = annualPackage != null
                        )
                        // Don't show fallback UI - just hide until loaded
                    }
                }

                // Monthly Plan - Show if available in packagesToShow
                if (packagesToShow.showMonthly) {
                    item {
                        Spacer(Modifier.height(12.dp))

                        val monthlyPackage = currentOffering?.monthly

                        PricingCard(
                            title = "Monthly",
                            price = monthlyPackage?.storeProduct?.price?.formatted ?: "$-.--",
                            period = "/month",
                            isRecommended = false,
                            isProcessing = uiState.isProcessing,
                            onSubscribe = {
                                if (monthlyPackage != null) {
                                    viewModel.purchasePackage(monthlyPackage)
                                }
                            },
                            enabled = monthlyPackage != null
                        )
                        // Don't show fallback UI - just hide until loaded
                    }
                }
            } else {
                // Thank You Section for existing members
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
                        onRestorePurchases = { viewModel.restorePurchases() },
                        isProcessing = uiState.isProcessing
                    )
                }
            }

            // Restore Purchases Button (for non-subscribers or those who need verification)
            if (canUpgrade) {
                item {
                    Spacer(Modifier.height(16.dp))

                    // Restore Purchases
                    TextButton(
                        onClick = { viewModel.restorePurchases() },
                        modifier = Modifier.padding(horizontal = 24.dp),
                        enabled = !uiState.isProcessing
                    ) {
                        Text(if (!subscriptionState.isSubscribed) "Already a member? Restore Purchases" else "Restore Purchases")
                    }
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

            // Fine Print
            item {
                Spacer(Modifier.height(24.dp))
                FinePrint()
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
            // Animated Rocket Icon
            val infiniteTransition = rememberInfiniteTransition(label = "rocket")
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rocketFloat"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .offset(y = offsetY.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(Res.drawable.launcher),
                    contentDescription = "Space Launch Now",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
            }

            if (isSubscribed) {
                Text(
                    text = "Thank You! 🎉",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "You're a ${subscriptionType.name.lowercase()} member!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Love Space Launch Now?",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Join as a member to unlock premium features",
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
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
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
                            text = "BEST VALUE",
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

            // Savings
            savings?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecommended) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }

            // Features Quick List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PerkCheckmark("Premium Themes")
                PerkCheckmark("Premium Widget")
                PerkCheckmark("Widget Customization")
                PerkCheckmark("Calendar Sync")
                PerkCheckmark("No Ads")
                PerkCheckmark("Support Development")
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
                    text = "Subscribe Now",
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
                contentDescription = "Space Launch Now",
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "You're Making a Difference!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = textColor
            )

            Text(
                text = if (isLegacy) {
                    "Thank you for your legacy support of Space Launch Now. You have ad-free access."
                } else {
                    "Thank you for supporting Space Launch Now. Your ${subscriptionType.name.lowercase()} membership helps us continue developing features and keeping the app running."
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = textColor
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = if (isLegacy) "Your Legacy Benefits:" else "Your Premium Features:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isLegacy) {
                    // Legacy users only get ad-free
                    ActivePerkItem(
                        icon = Icons.Default.Block,
                        title = "No Ads",
                        description = "Ad-free experience"
                    )
                    ActivePerkItem(
                        icon = Icons.Default.Favorite,
                        title = "Supporting Development",
                        description = "Thank you!"
                    )
                } else {
                    // Current premium users get full features
                    ActivePerkItem(
                        icon = Icons.Default.Widgets,
                        title = "Premium Widget",
                        description = "Unlocked"
                    )
                    ActivePerkItem(
                        icon = Icons.Default.FormatPaint,
                        title = "Premium Themes",
                        description = "Unlocked"
                    )
                    ActivePerkItem(
                        icon = Icons.Default.Block,
                        title = "No Ads",
                        description = "Active"
                    )
                    ActivePerkItem(
                        icon = Icons.Default.Favorite,
                        title = "Supporting Development",
                        description = "Thank you!"
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
private fun FinePrint() {
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Text(
            text = "Secure Billing",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "All subscriptions are processed securely through Google Play or the App Store. " +
                    "You can manage or cancel your subscription at any time through your account settings. " +
                    "No refunds for partial subscription periods.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
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
            .padding(horizontal = 24.dp)
            .platformShadowGlow(
                gradientColors = listOf(
                    Color(0xFFFFD700).copy(alpha = 0.6f), // Gold
                    Color(0xFFFFA500).copy(alpha = 0.5f), // Orange
                    Color(0xFFFF9800).copy(alpha = 0.7f)  // Yellow
                ),
                borderRadius = 10.dp,
                blurRadius = 12.dp,
                offsetX = 0.dp,
                offsetY = 0.dp,
                spread = 8.dp,
                enableBreathingEffect = true,
                breathingEffectIntensity = 4.dp,
                breathingDurationMillis = 3000
            ),
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
                        text = "Pro Lifetime",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB8860B) // Dark goldenrod
                    )
                    Text(
                        text = "Buy once, own forever",
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
                    text = "one time",
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
                    text = "✨ MAX SUPPORT ✨",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB8860B),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Premium Features List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProPerkCheckmark("Everything in Subscriptions")
                ProPerkCheckmark("Lifetime Access")
                ProPerkCheckmark("Support Independent Developer")
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
                    text = "Unlock Pro Forever",
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
    subscriptionState: me.calebjones.spacelaunchnow.data.model.SubscriptionState,
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
                    text = "Current Plan",
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
                        subscriptionState.isSubscribed -> "${subscriptionState.subscriptionType.name.replaceFirstChar { it.uppercase() }} Member"
                        else -> "Free User"
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
                        text = "Product: ${getProductDisplayName(productId)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor
                    )
                }
            } else {
                Text(
                    text = "Upgrade to unlock premium features and support development",
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
                    text = "Thank You, Early Supporter!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            // Appreciation message
            Text(
                text = "Your legacy purchase helped build Space Launch Now into what it is today. I am incredibly grateful for your early support!",
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
                text = "Continue Your Support",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            // Call to action
            Text(
                text = "Choose a plan below to continue supporting development!",
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
    val showMonthly: Boolean
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
    // Check if user has lifetime purchase - match against actual lifetime product IDs
    val hasLifetime = subscriptionState.productId?.let { productId ->
        productId == SubscriptionProducts.PRO_LIFETIME ||  // Current lifetime: "spacelaunchnow_pro"
                productId.contains("lifetime", ignoreCase = true)  // Other lifetime variants
    } ?: false

    if (hasLifetime) {
        return UserState.LIFETIME_USER
    }

    // Check if user is a legacy user
    if (subscriptionState.subscriptionType.isLegacy) {
        return UserState.LEGACY_USER
    }

    // Check if user has an active subscription
    if (subscriptionState.isSubscribed) {
        // Determine if monthly or annual based on productId or basePlanId
        val isMonthly = subscriptionState.productId?.let { productId ->
            productId.contains("monthly", ignoreCase = true) ||
                    productId.contains("base-plan", ignoreCase = true) ||
                    productId.contains("base_plan", ignoreCase = true)
        } ?: false

        return if (isMonthly) UserState.MONTHLY_SUBSCRIBER else UserState.ANNUAL_SUBSCRIBER
    }

    // Default: New user with no subscriptions
    return UserState.NEW_USER
}

/**
 * Get packages to show based on user state
 *
 * Rules:
 * 1) New User - Show all (lifetime, annual, monthly)
 * 2) Legacy User - Show all (lifetime, annual, monthly)
 * 3) Monthly Subscriber - Show annual and lifetime
 * 4) Annual Subscriber - Show lifetime only
 * 5) Lifetime User - Show nothing
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
            showMonthly = true
        )

        UserState.LEGACY_USER -> PackagesToShow(
            showLifetime = true,
            showAnnual = true,
            showMonthly = true
        )

        UserState.MONTHLY_SUBSCRIBER -> PackagesToShow(
            showLifetime = true,
            showAnnual = true,
            showMonthly = false
        )

        UserState.ANNUAL_SUBSCRIBER -> PackagesToShow(
            showLifetime = true,
            showAnnual = false,
            showMonthly = false
        )

        UserState.LIFETIME_USER -> PackagesToShow(
            showLifetime = false,
            showAnnual = false,
            showMonthly = false
        )
    }
}
