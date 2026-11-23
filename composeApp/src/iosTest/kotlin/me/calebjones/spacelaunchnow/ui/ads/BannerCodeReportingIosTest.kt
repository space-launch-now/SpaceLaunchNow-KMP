package me.calebjones.spacelaunchnow.ui.ads

import cocoapods.Google_Mobile_Ads_SDK.GADBannerView
import cocoapods.Google_Mobile_Ads_SDK.GADBannerViewDelegateProtocol
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.Foundation.NSLocalizedDescriptionKey
import platform.darwin.NSObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class BannerCodeReportingIosTest {

    /** Stands in for the library's BannerViewDelegate: records which callbacks reached it. */
    private class RecordingDelegate : NSObject(), GADBannerViewDelegateProtocol {
        val calls = mutableListOf<String>()
        override fun bannerViewDidReceiveAd(bannerView: GADBannerView) { calls += "received" }
        override fun bannerView(bannerView: GADBannerView, didFailToReceiveAdWithError: NSError) {
            calls += "failed:${didFailToReceiveAdWithError.code}"
        }
        override fun bannerViewDidRecordImpression(bannerView: GADBannerView) { calls += "impression" }
        override fun bannerViewDidRecordClick(bannerView: GADBannerView) { calls += "click" }
        override fun bannerViewWillPresentScreen(bannerView: GADBannerView) { calls += "willPresent" }
        override fun bannerViewWillDismissScreen(bannerView: GADBannerView) { calls += "willDismiss" }
        override fun bannerViewDidDismissScreen(bannerView: GADBannerView) { calls += "didDismiss" }
    }

    private fun noFill(): NSError = NSError(
        domain = "com.google.admob",
        code = 1,
        userInfo = mapOf<Any?, Any?>(NSLocalizedDescriptionKey to "Request Error: No ad to show.")
    )

    @Test
    fun failureReportsNumericCodeAndMessageThenForwards() {
        val original = RecordingDelegate()
        var reported: Pair<String?, Int>? = null
        val delegate = CodeReportingBannerDelegate(original, "320x50", "BANNER") { message, code ->
            reported = message to code
        }

        delegate.bannerView(GADBannerView(), noFill())

        assertEquals("Request Error: No ad to show." to 1, reported)
        assertEquals(listOf("failed:1"), original.calls)
    }

    @Test
    fun everyOtherCallbackReachesTheOriginal() {
        val original = RecordingDelegate()
        val delegate = CodeReportingBannerDelegate(original, "320x50", "BANNER") { _, _ -> }
        val view = GADBannerView()

        delegate.bannerViewDidReceiveAd(view)
        delegate.bannerViewDidRecordImpression(view)
        delegate.bannerViewDidRecordClick(view)
        delegate.bannerViewWillPresentScreen(view)
        delegate.bannerViewWillDismissScreen(view)
        delegate.bannerViewDidDismissScreen(view)

        assertEquals(
            listOf("received", "impression", "click", "willPresent", "willDismiss", "didDismiss"),
            original.calls
        )
    }

    @Test
    fun installWrapsOnceAndKeepsTheLibraryDelegate() {
        val original = RecordingDelegate()
        val view = GADBannerView().apply { delegate = original }

        view.installCodeReporting("320x50", "BANNER")
        val installed = view.delegate
        view.installCodeReporting("320x50", "BANNER")

        assertIs<CodeReportingBannerDelegate>(installed)
        assertSame(installed, view.delegate)
        installed.bannerView(view, noFill())
        assertEquals(listOf("failed:1"), original.calls)
    }
}
