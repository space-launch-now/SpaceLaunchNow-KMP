package me.calebjones.spacelaunchnow.ui.layout

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import me.calebjones.spacelaunchnow.getPlatform

/**
 * Three-tier layout classification for responsive UI decisions.
 * Replaces the binary phone/tablet split.
 */
enum class LayoutTier {
    /** < 600dp — phone portrait, small phone landscape */
    COMPACT,
    /** 600-840dp — phone landscape, small tablets, foldable inner screen */
    MEDIUM,
    /** ≥ 840dp — tablets, desktop, large foldables */
    EXPANDED
}

/**
 * Unified responsive layout state. Use [rememberAdaptiveLayoutState] to create.
 *
 * Replaces:
 * - `isTabletOrDesktop()` → use [isExpanded]
 * - `isLargeScreen()` → use [isMediumOrLarger]
 */
data class AdaptiveLayoutState(
    val tier: LayoutTier,
    val isDesktopPlatform: Boolean,
    val windowSizeClass: WindowSizeClass
) {
    /** < 600dp — phone portrait layout */
    val isCompact: Boolean get() = tier == LayoutTier.COMPACT

    /** ≥ 600dp — tablet, phone landscape, or desktop */
    val isMediumOrLarger: Boolean get() = tier != LayoutTier.COMPACT

    /** ≥ 840dp — full tablet/desktop two-pane experience */
    val isExpanded: Boolean get() = tier == LayoutTier.EXPANDED

    /** Platform is Desktop (JVM) regardless of window size */
    val isDesktop: Boolean get() = isDesktopPlatform
}

/**
 * Creates and remembers an [AdaptiveLayoutState] based on current window size
 * and platform. Recomposes when window size changes (e.g., rotation, resize).
 */
@Composable
fun rememberAdaptiveLayoutState(): AdaptiveLayoutState {
    val windowInfo = currentWindowAdaptiveInfo()
    val widthClass = windowInfo.windowSizeClass.windowWidthSizeClass

    val tier = when (widthClass) {
        WindowWidthSizeClass.COMPACT -> LayoutTier.COMPACT
        WindowWidthSizeClass.MEDIUM -> LayoutTier.MEDIUM
        else -> LayoutTier.EXPANDED // EXPANDED or any future addition
    }

    return AdaptiveLayoutState(
        tier = tier,
        isDesktopPlatform = getPlatform().type.isDesktop,
        windowSizeClass = windowInfo.windowSizeClass
    )
}
