package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Ad placement types for context-aware ad sizing.
 * This enum is shared across all platforms.
 */
enum class AdPlacementType {
    NAVIGATION,     // Bottom nav, navigation rail - compact ads
    CONTENT,        // Article content, detail pages - larger content ads
    FEED,           // Home page, lists - medium visibility ads
    INTERSTITIAL    // Between content sections - attention-grabbing ads
}

/**
 * Smart banner ad component that automatically handles platform-specific ad display.
 * 
 * On Android/iOS:
 * - Shows actual ads using GoogleAdMob via basic-ads library
 * - Checks premium subscription (no ads if user has AD_FREE)
 * - Handles Material 3 styling and proper sizing
 * - Optional "Remove Ads" button for premium conversion
 * 
 * On Desktop:
 * - Does nothing (no-op, returns empty composable)
 * 
 * @param modifier Modifier to apply to the ad container
 * @param placementType Context-aware placement type (overrides adSize parameter on Android/iOS)
 * @param showRemoveAdsButton Whether to show "Remove Ads" button (Android/iOS only)
 * @param showCard Whether to wrap ad in Material card (Android/iOS only)
 * @param onRemoveAdsClick Callback when "Remove Ads" button is clicked
 * @param onSizeChanged Callback when ad size changes (widthDp, heightPx: Int)
 */
@Composable
expect fun SmartBannerAd(
    modifier: Modifier = Modifier,
    placementType: AdPlacementType = AdPlacementType.NAVIGATION,
    showRemoveAdsButton: Boolean = false,
    showCard: Boolean = true,
    onRemoveAdsClick: (() -> Unit)? = null,
    onSizeChanged: ((widthDp: Dp, heightPx: Int) -> Unit)? = null
)

/**
 * Interstitial ad handler that shows full-screen ads.
 * 
 * On Android/iOS:
 * - Shows interstitial ads every Nth visit based on GlobalAdManager logic
 * - Respects minimum time intervals between ads
 * 
 * On Desktop:
 * - Does nothing (no-op)
 * 
 * @param onAdShown Called when an interstitial ad is successfully shown
 * @param onAdFailed Called when an interstitial ad fails to load or show
 */
@Composable
expect fun InterstitialAdHandler(
    onAdShown: (() -> Unit)? = null,
    onAdFailed: ((String) -> Unit)? = null
)

/**
 * Rewarded ad handler that shows ads that reward the user.
 * 
 * On Android/iOS:
 * - Shows rewarded ads when triggered
 * - Calls reward callback when user completes watching the ad
 * 
 * On Desktop:
 * - Does nothing (no-op)
 * 
 * @param shouldShow Whether to trigger showing the rewarded ad
 * @param onRewardEarned Called when the user earns a reward (amount, type)
 * @param onAdShown Called when a rewarded ad is successfully shown
 * @param onAdFailed Called when a rewarded ad fails to load or show
 */
@Composable
expect fun RewardedAdHandler(
    shouldShow: Boolean = false,
    onRewardEarned: ((rewardAmount: Int, rewardType: String) -> Unit)? = null,
    onAdShown: (() -> Unit)? = null,
    onAdFailed: ((String) -> Unit)? = null
)

/**
 * Ad consent popup for GDPR/privacy compliance.
 * 
 * On Android/iOS:
 * - Shows Google UMP consent dialog when needed
 * 
 * On Desktop:
 * - Does nothing (no-op)
 * 
 * @param onFailure Called if consent popup fails to show
 */
@Composable
expect fun AdConsentPopup(
    onFailure: ((Throwable) -> Unit)? = null,
    onConsentResolved: (() -> Unit)? = null
)

/**
 * Wrapper composable that provides preloaded ads via CompositionLocal.
 * 
 * On Android/iOS:
 * - Preloads banner, interstitial, and rewarded ads
 * - Provides ad handlers via CompositionLocal for instant rendering
 * 
 * On Desktop:
 * - Simple wrapper that just renders content
 * 
 * @param context Platform-specific context (Activity on Android, null on iOS, ignored on Desktop)
 * @param content The content to wrap
 */
@Composable
expect fun WithPreloadedAds(
    context: Any?,
    shouldPreloadAds: Boolean = true,
    content: @Composable () -> Unit
)

/**
 * Shows the privacy options form so the user can change their ad consent choices.
 *
 * On Android/iOS:
 * - Uses Google UMP to show the privacy options form
 * - Only available when privacy options are required (EEA users)
 *
 * On Desktop:
 * - No-op
 *
 * @param onDismiss Called when the form is dismissed (success or failure)
 * @param onFailure Called if showing the form fails
 */
expect fun showPrivacyOptionsForm(
    activity: Any?,
    onDismiss: () -> Unit = {},
    onFailure: (Throwable) -> Unit = {}
)

/**
 * Returns whether the privacy options entry point should be shown.
 *
 * On Android/iOS:
 * - Returns true if UMP indicates privacy options are required
 *
 * On Desktop:
 * - Always returns false
 */
@Composable
expect fun rememberPrivacyOptionsRequired(): Boolean
