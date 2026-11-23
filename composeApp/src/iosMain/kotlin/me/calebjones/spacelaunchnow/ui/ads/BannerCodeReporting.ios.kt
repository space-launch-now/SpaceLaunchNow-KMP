package me.calebjones.spacelaunchnow.ui.ads

import app.lexilabs.basic.ads.BannerAdHandler
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import cocoapods.Google_Mobile_Ads_SDK.GADBannerView
import cocoapods.Google_Mobile_Ads_SDK.GADBannerViewDelegateProtocol
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import platform.Foundation.NSError
import platform.darwin.NSObject
import platform.objc.OBJC_ASSOCIATION_RETAIN_NONATOMIC
import platform.objc.objc_setAssociatedObject
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

private val log by lazy { SpaceLogger.getLogger("BannerCodeReporting") }

/**
 * Key for the associated-object slot on a GADBannerView that keeps its [CodeReportingBannerDelegate]
 * alive. One pointer for the process; never freed.
 */
private val codeReportingKey = nativeHeap.alloc<IntVar>().ptr

/**
 * Wraps the handler's GADBannerView delegate so ad_failed carries the AdMob error code. basic-ads
 * turns the NSError into an AdException built from its localizedDescription, which loses
 * `NSError.code` before any onFailure lambda runs. Every callback still reaches the library's
 * delegate, which drives the handler state. Safe to call twice. Call it right after `load()`:
 * the library replaces `bannerView` (and its delegate) on each load.
 */
@OptIn(DependsOnGoogleMobileAds::class, ExperimentalForeignApi::class)
internal fun BannerAdHandler.installCodeReporting(size: String, placement: String) {
    bannerView.installCodeReporting(size, placement)
}

@OptIn(ExperimentalForeignApi::class)
internal fun GADBannerView.installCodeReporting(size: String, placement: String) {
    val current = delegate
    if (current is CodeReportingBannerDelegate) return
    log.d { "Installing code reporting for $placement ($size); library delegate present=${current != null}" }
    val wrapper = CodeReportingBannerDelegate(current, size, placement)
    // GADBannerView.delegate is a weak property, so the view does not keep the wrapper alive.
    // Retain it (and, through it, the library's delegate) for as long as the view exists.
    objc_setAssociatedObject(this, codeReportingKey, wrapper, OBJC_ASSOCIATION_RETAIN_NONATOMIC)
    delegate = wrapper
}

/**
 * Forwards every GADBannerViewDelegate callback to [original] and reports a failed load with its
 * numeric error code through [report] (by default [AdTelemetry.failed]).
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class CodeReportingBannerDelegate(
    private val original: GADBannerViewDelegateProtocol?,
    size: String,
    placement: String,
    private val report: (message: String?, code: Int) -> Unit = { message, code ->
        AdTelemetry.failed("banner", message, code, size, placement)
    }
) : NSObject(), GADBannerViewDelegateProtocol {

    override fun bannerViewDidReceiveAd(bannerView: GADBannerView) {
        original?.bannerViewDidReceiveAd(bannerView)
    }

    override fun bannerView(bannerView: GADBannerView, didFailToReceiveAdWithError: NSError) {
        report(didFailToReceiveAdWithError.localizedDescription, didFailToReceiveAdWithError.code.toInt())
        original?.bannerView(bannerView, didFailToReceiveAdWithError)
    }

    override fun bannerViewDidRecordImpression(bannerView: GADBannerView) {
        original?.bannerViewDidRecordImpression(bannerView)
    }

    override fun bannerViewDidRecordClick(bannerView: GADBannerView) {
        original?.bannerViewDidRecordClick(bannerView)
    }

    override fun bannerViewWillPresentScreen(bannerView: GADBannerView) {
        original?.bannerViewWillPresentScreen(bannerView)
    }

    override fun bannerViewWillDismissScreen(bannerView: GADBannerView) {
        original?.bannerViewWillDismissScreen(bannerView)
    }

    override fun bannerViewDidDismissScreen(bannerView: GADBannerView) {
        original?.bannerViewDidDismissScreen(bannerView)
    }
}
