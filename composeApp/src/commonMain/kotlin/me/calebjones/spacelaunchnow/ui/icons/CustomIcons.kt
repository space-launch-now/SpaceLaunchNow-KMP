package me.calebjones.spacelaunchnow.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom rocket icon for SpaceLaunchNow
 * Original source: ic_rocket_notification.xml
 */
val CustomIcons.RocketLaunch: ImageVector
    get() {
        if (_rocketLaunch != null) {
            return _rocketLaunch!!
        }
        _rocketLaunch = ImageVector.Builder(
            name = "RocketLaunch",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                // Main path data from ic_rocket_notification.xml
                moveTo(463.6f, 119.6f)
                lineTo(376.5f, 32.4f)
                curveToRelative(54.8f, -20.6f, 94f, -18f, 99.6f, -12.4f)
                curveTo(481.6f, 25.6f, 484.2f, 64.7f, 463.6f, 119.6f)
                close()
                
                moveTo(224.2f, 467.8f)
                curveToRelative(-0.1f, 0.7f, -0.2f, 1.4f, -0.2f, 2.1f)
                curveToRelative(0f, 3.2f, 1.5f, 6.2f, 4.1f, 8.1f)
                curveToRelative(3.2f, 2.4f, 7.5f, 2.6f, 10.9f, 0.7f)
                curveToRelative(70.6f, -39.6f, 101.5f, -79.5f, 118f, -107.5f)
                curveToRelative(4.1f, -7f, 11f, -20.8f, 11f, -41.2f)
                verticalLineTo(288.3f)
                curveToRelative(-36.2f, 34.1f, -79.5f, 63.5f, -120.6f, 82.8f)
                lineTo(224.2f, 467.8f)
                close()
                
                moveTo(26.1f, 272f)
                curveToRelative(0.7f, 0f, 1.4f, -0.1f, 2.1f, -0.2f)
                lineToRelative(96.6f, -23.1f)
                curveToRelative(19.2f, -41f, 48.7f, -83.9f, 83.4f, -120.7f)
                horizontalLineToRelative(-42.2f)
                curveToRelative(-20.3f, 0f, -34.2f, 6.8f, -41.2f, 11f)
                curveToRelative(-28.1f, 16.4f, -68f, 47.4f, -107.5f, 118f)
                curveToRelative(-2f, 3.5f, -1.7f, 7.7f, 0.7f, 10.9f)
                curveTo(19.9f, 270.5f, 22.9f, 272f, 26.1f, 272f)
                close()
                
                moveTo(129.1f, 399.1f)
                curveToRelative(-9.5f, 20.8f, -25.9f, 32.5f, -38.8f, 38.7f)
                curveToRelative(-17f, 8.2f, -33.3f, 9.9f, -41.5f, 9.4f)
                curveToRelative(-0.5f, -8.2f, 1.2f, -24.5f, 9.4f, -41.5f)
                curveToRelative(6.2f, -12.9f, 17.8f, -29.3f, 38.6f, -38.8f)
                lineToRelative(-0.1f, -0.1f)
                curveToRelative(-2.8f, -2.8f, -4.8f, -6.2f, -6.4f, -9.6f)
                curveToRelative(-1.8f, 0.7f, -3.6f, 1.3f, -5.7f, 2f)
                curveToRelative(-67.9f, 22.6f, -75.8f, 105.2f, -64.5f, 116.5f)
                curveToRelative(11.3f, 11.3f, 93.9f, 3.4f, 116.5f, -64.5f)
                curveToRelative(0.7f, -2f, 1.3f, -3.7f, 2f, -5.5f)
                curveToRelative(-3.5f, -1.6f, -6.8f, -3.8f, -9.6f, -6.6f)
                lineTo(129.1f, 399.1f)
                close()
                
                moveTo(359.1f, 39.6f)
                lineToRelative(97.6f, 97.6f)
                curveToRelative(-17.2f, 38.8f, -45.7f, 83.8f, -91.6f, 129.7f)
                curveToRelative(-50.8f, 50.8f, -116.9f, 90.5f, -168.4f, 104.8f)
                lineToRelative(-72.4f, -72.5f)
                curveToRelative(14.4f, -51.4f, 54.3f, -117.3f, 105f, -168.1f)
                curveTo(275.3f, 85.3f, 320.3f, 56.8f, 359.1f, 39.6f)
                close()
                
                moveTo(343.2f, 152.8f)
                curveToRelative(-12.8f, -12.8f, -33.6f, -12.8f, -46.4f, 0f)
                curveToRelative(-12.8f, 12.8f, -12.8f, 33.6f, 0f, 46.4f)
                curveToRelative(12.8f, 12.8f, 33.6f, 12.8f, 46.4f, 0f)
                curveTo(356f, 186.4f, 356f, 165.6f, 343.2f, 152.8f)
                close()
                
                moveTo(105.7f, 339.3f)
                curveToRelative(-1.8f, 5.3f, -0.4f, 11.1f, 3.5f, 15.1f)
                lineToRelative(32.4f, 32.4f)
                curveToRelative(3.9f, 4f, 9.8f, 5.3f, 15.1f, 3.6f)
                lineToRelative(25.3f, -8.4f)
                lineToRelative(-67.9f, -67.9f)
                lineTo(105.7f, 339.3f)
                close()
            }
        }.build()
        return _rocketLaunch!!
    }

private var _rocketLaunch: ImageVector? = null

object CustomIcons
