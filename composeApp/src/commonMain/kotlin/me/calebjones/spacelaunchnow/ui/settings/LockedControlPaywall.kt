package me.calebjones.spacelaunchnow.ui.settings

/**
 * A tap on a locked notification control opens the paywall only for a free user and only
 * while the Remote Config flag is on.
 */
internal fun shouldOpenPaywallOnLockedTap(flagEnabled: Boolean, hasCustomization: Boolean): Boolean =
    flagEnabled && !hasCustomization
