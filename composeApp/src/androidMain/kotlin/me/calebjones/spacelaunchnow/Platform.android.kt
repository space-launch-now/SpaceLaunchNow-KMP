package me.calebjones.spacelaunchnow

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun setOrientationLandscape() {
    // This version cannot access Activity context - use Composable version instead
}

actual fun setOrientationPortrait() {
    // This version cannot access Activity context - use Composable version instead
}

actual fun setOrientationSensor() {
    // This version cannot access Activity context - use Composable version instead
}

@Composable
actual fun setOrientationLandscapeFromComposable() {
    val context = LocalContext.current
    context.findActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

@Composable
actual fun setOrientationPortraitFromComposable() {
    val context = LocalContext.current
    context.findActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}

@Composable
actual fun setOrientationSensorFromComposable() {
    val context = LocalContext.current
    context.findActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
}

@Composable
actual fun getScreenWidth() = LocalConfiguration.current
    .screenWidthDp
    .dp

@Composable
actual fun getScreenHeight() = LocalConfiguration.current
    .screenHeightDp
    .dp

// Helper function to get Activity from Context
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}