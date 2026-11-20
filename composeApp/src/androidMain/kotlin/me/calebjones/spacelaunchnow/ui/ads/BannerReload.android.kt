package me.calebjones.spacelaunchnow.ui.ads

import android.view.ViewGroup
import app.lexilabs.basic.ads.AdSize
import app.lexilabs.basic.ads.BannerAdHandler
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Reloads this handler with this app's ad unit and [loadSize], by default the handler's own
 * size. A bare load() falls back to the library defaults: Google's test ad unit at FULL_BANNER.
 *
 * The library's load() makes a new AdView and never destroys the old one, so this destroys the
 * replaced AdView once the new one is the handler's bannerView. It also wraps the new view's
 * listener so a failure reports the AdMob error code (the library drops it). That wrapper is
 * the single sender of ad_failed: the library's onFailure here does nothing.
 */
@OptIn(DependsOnGoogleMobileAds::class)
internal fun BannerAdHandler.reloadBannerView(placement: String, loadSize: AdSize = adSize) {
    val previous = bannerView
    val size = AdTelemetry.sizeLabel(loadSize.width, loadSize.height)
    load(
        adUnitId = GlobalAdManager.getPlatformAdUnitId(AdType.BANNER),
        adSize = loadSize,
        onLoad = { AdTelemetry.loaded("banner", size, placement) },
        onFailure = { /* reported with its code by CodeReportingAdListener */ },
        onDismissed = {},
        onShown = {},
        onImpression = { AdTelemetry.impression("banner", size, placement) },
        onClick = { AdTelemetry.clicked("banner", size, placement) }
    )
    installCodeReporting(size, placement)
    retireBannerView(previous, bannerView)
}

/**
 * Wraps the handler's AdView listener so ad_failed carries the AdMob error code. Every callback
 * still reaches the library's listener, which drives the handler state. Safe to call twice.
 */
@OptIn(DependsOnGoogleMobileAds::class)
internal fun BannerAdHandler.installCodeReporting(size: String, placement: String) {
    val view = bannerView ?: return
    if (view.adListener is CodeReportingAdListener) return
    view.adListener = CodeReportingAdListener(view.adListener, size, placement)
}

/** Destroys a replaced AdView. No-op for null or when it is still the current view. */
internal fun retireBannerView(previous: AdView?, current: AdView?) {
    if (previous == null || previous === current) return
    (previous.parent as? ViewGroup)?.removeView(previous)
    previous.destroy()
}

/** Forwards every callback to [original] and reports a failed load with its error code. */
internal class CodeReportingAdListener(
    private val original: AdListener?,
    private val size: String,
    private val placement: String
) : AdListener() {
    override fun onAdClicked() {
        original?.onAdClicked()
    }

    override fun onAdClosed() {
        original?.onAdClosed()
    }

    override fun onAdFailedToLoad(error: LoadAdError) {
        AdTelemetry.failed("banner", error.message, error.code, size, placement)
        original?.onAdFailedToLoad(error)
    }

    override fun onAdImpression() {
        original?.onAdImpression()
    }

    override fun onAdLoaded() {
        original?.onAdLoaded()
    }

    override fun onAdOpened() {
        original?.onAdOpened()
    }

    override fun onAdSwipeGestureClicked() {
        original?.onAdSwipeGestureClicked()
    }
}
