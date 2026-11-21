package me.calebjones.spacelaunchnow.ui.detail.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.util.SharingFormatUtil
import me.calebjones.spacelaunchnow.util.sharePlainText
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_share_description
import spacelaunchnow_kmp.composeapp.generated.resources.launch_detail_share_title

/**
 * Share button for the launch detail header, used by the phone and tablet layouts.
 * [compact] matches the smaller tablet back button style.
 */
@Composable
internal fun LaunchShareButton(launch: Launch, compact: Boolean = false) {
    val useUtc = LocalUseUtc.current
    val analyticsManager: AnalyticsManager = koinInject()
    val shareTitle = stringResource(Res.string.launch_detail_share_title)
    IconButton(
        onClick = {
            analyticsManager.track(AnalyticsEvent.LaunchShared(launch.id.toString(), "share_sheet"))
            sharePlainText(
                SharingFormatUtil.formatLaunchShareText(launch, useUtc),
                shareTitle
            )
        },
        modifier = Modifier
            .size(if (compact) 36.dp else 48.dp)
            .clip(CircleShape)
            .background(
                color = if (compact) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                } else {
                    Color(0xff121212).copy(alpha = 0.32f)
                },
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = stringResource(Res.string.launch_detail_share_description),
            tint = if (compact) MaterialTheme.colorScheme.onSurface else Color.White,
            modifier = Modifier.size(if (compact) 20.dp else 24.dp)
        )
    }
}
