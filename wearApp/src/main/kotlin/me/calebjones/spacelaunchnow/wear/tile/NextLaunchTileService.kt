package me.calebjones.spacelaunchnow.wear.tile

import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.DimensionBuilders.wrap
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import co.touchlab.kermit.Logger
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.guava.future
import me.calebjones.spacelaunchnow.wear.data.EntitlementSyncManager
import me.calebjones.spacelaunchnow.wear.data.WatchLaunchRepository
import me.calebjones.spacelaunchnow.wear.data.model.CachedLaunch
import org.koin.android.ext.android.inject
import kotlin.time.Clock

class NextLaunchTileService : TileService() {

    private val watchLaunchRepository: WatchLaunchRepository by inject()
    private val entitlementSyncManager: EntitlementSyncManager by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val log = Logger.withTag("NextLaunchTile")

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        return serviceScope.future {
            try {
                val isPremium = entitlementSyncManager.isWearOsPremium()
                val layout = if (!isPremium) {
                    buildFreeUserLayout()
                } else {
                    val nextLaunch = watchLaunchRepository.getNextLaunch()
                    if (nextLaunch != null) {
                        buildLaunchLayout(nextLaunch)
                    } else {
                        buildNoDataLayout()
                    }
                }

                TileBuilders.Tile.Builder()
                    .setResourcesVersion(RESOURCES_VERSION)
                    .setTileTimeline(
                        TimelineBuilders.Timeline.Builder()
                            .addTimelineEntry(
                                TimelineBuilders.TimelineEntry.Builder()
                                    .setLayout(
                                        LayoutElementBuilders.Layout.Builder()
                                            .setRoot(layout)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .setFreshnessIntervalMillis(REFRESH_INTERVAL_MS)
                    .build()
            } catch (e: Exception) {
                log.e(e) { "Failed to build tile" }
                buildErrorTile()
            }
        }
    }

    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()
        )
    }

    private fun buildLaunchLayout(launch: CachedLaunch): LayoutElementBuilders.LayoutElement {
        val agencyText = launch.lspAbbrev ?: launch.lspName ?: ""
        val vehicleName = formatVehicleName(launch)
        val missionName = launch.missionName ?: launch.name
        val countdownText = formatCountdown(launch)
        val locationText = launch.padLocationName ?: ""

        return LayoutElementBuilders.Column.Builder()
            .setWidth(wrap())
            .setHeight(wrap())
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setAll(dp(12f))
                            .build()
                    )
                    .build()
            )
            .addContent(buildText(agencyText, 12f, TEXT_COLOR_SECONDARY))
            .addContent(buildSpacer(4f))
            .addContent(buildText(missionName, 16f, TEXT_COLOR_PRIMARY))
            .addContent(buildSpacer(4f))
            .addContent(buildText(vehicleName, 12f, TEXT_COLOR_SECONDARY))
            .addContent(buildSpacer(8f))
            .addContent(buildText(countdownText, 20f, TEXT_COLOR_ACCENT))
            .addContent(buildSpacer(4f))
            .addContent(buildText(locationText, 10f, TEXT_COLOR_SECONDARY))
            .build()
    }

    private fun buildFreeUserLayout(): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Column.Builder()
            .setWidth(wrap())
            .setHeight(wrap())
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setAll(dp(16f))
                            .build()
                    )
                    .build()
            )
            .addContent(buildText("Space Launch Now", 14f, TEXT_COLOR_PRIMARY))
            .addContent(buildSpacer(8f))
            .addContent(buildText("Upgrade on phone", 16f, TEXT_COLOR_ACCENT))
            .addContent(buildSpacer(4f))
            .addContent(buildText("to see launch info", 12f, TEXT_COLOR_SECONDARY))
            .build()
    }

    private fun buildNoDataLayout(): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Column.Builder()
            .setWidth(wrap())
            .setHeight(wrap())
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setAll(dp(16f))
                            .build()
                    )
                    .build()
            )
            .addContent(buildText("Space Launch Now", 14f, TEXT_COLOR_PRIMARY))
            .addContent(buildSpacer(8f))
            .addContent(buildText("No upcoming launches", 14f, TEXT_COLOR_SECONDARY))
            .build()
    }

    private fun buildText(
        text: String,
        fontSize: Float,
        color: Int,
    ): LayoutElementBuilders.Text {
        return LayoutElementBuilders.Text.Builder()
            .setText(text)
            .setFontStyle(
                LayoutElementBuilders.FontStyle.Builder()
                    .setSize(sp(fontSize))
                    .setColor(argb(color))
                    .build()
            )
            .setMaxLines(2)
            .build()
    }

    private fun buildSpacer(heightDp: Float): LayoutElementBuilders.Spacer {
        return LayoutElementBuilders.Spacer.Builder()
            .setHeight(dp(heightDp))
            .build()
    }

    private fun buildErrorTile(): TileBuilders.Tile {
        val errorLayout = LayoutElementBuilders.Column.Builder()
            .setWidth(wrap())
            .setHeight(wrap())
            .addContent(buildText("Error loading tile", 14f, TEXT_COLOR_SECONDARY))
            .build()

        return TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(errorLayout)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun formatVehicleName(launch: CachedLaunch): String {
        val lspDisplay = launch.lspName?.let { name ->
            if (name.length > 15 && !launch.lspAbbrev.isNullOrEmpty()) {
                launch.lspAbbrev
            } else {
                name
            }
        }
        return when {
            lspDisplay != null && launch.rocketConfigName != null ->
                "$lspDisplay | ${launch.rocketConfigName}"
            launch.rocketConfigName != null -> launch.rocketConfigName
            else -> launch.name
        }
    }

    private fun formatCountdown(launch: CachedLaunch): String {
        val now = Clock.System.now()
        val duration = launch.net - now
        if (duration.isNegative()) return "Launched"

        val totalMinutes = duration.inWholeMinutes
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours >= 24 -> "T-${hours / 24}d ${hours % 24}h"
            hours > 0 -> "T-${hours}h ${minutes}m"
            minutes > 0 -> "T-${minutes}m"
            else -> "T-0"
        }
    }

    companion object {
        private const val RESOURCES_VERSION = "1"
        private const val REFRESH_INTERVAL_MS = 300_000L // 5 minutes
        private const val TEXT_COLOR_PRIMARY = 0xFFFFFFFF.toInt()
        private const val TEXT_COLOR_SECONDARY = 0xB3FFFFFF.toInt()
        private const val TEXT_COLOR_ACCENT = 0xFF80CBFF.toInt()
    }
}
