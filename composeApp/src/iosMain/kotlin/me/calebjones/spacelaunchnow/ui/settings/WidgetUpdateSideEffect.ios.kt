package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.runtime.Composable

/**
 * iOS implementation of widget update side effect.
 * No-op on iOS — widget transparency and corner radius controls are Android-only.
 * iOS WidgetKit manages widget appearance at the system level.
 */
@Composable
actual fun WidgetUpdateSideEffect() {
    // Widget customization controls are Android-only.
    // iOS widgets use default WidgetKit backgrounds.
}
