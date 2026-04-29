package me.calebjones.spacelaunchnow.wear.tile

import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.wrap
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.types.LayoutColor
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import co.touchlab.kermit.Logger
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.guava.future
import me.calebjones.spacelaunchnow.wear.R
import me.calebjones.spacelaunchnow.wear.WearActivity
import me.calebjones.spacelaunchnow.wear.data.EntitlementSyncManager
import me.calebjones.spacelaunchnow.wear.data.WatchLaunchRepository
import me.calebjones.spacelaunchnow.wear.data.model.CachedLaunch
import org.koin.android.ext.android.inject
import kotlin.time.Clock

/**
 * Next-launch Tile rendered with ProtoLayout Material 3.
 *
 * Layout uses [primaryLayout]'s three slots directly (no card wrapper):
 * - titleSlot: mission name (small, top — like "Sugar Hill" in the M3 weather sample)
 * - mainSlot: T-minus countdown numeral + status abbreviation (color-coded by status)
 * - bottomSlot: "Details" edge button
 *
 * The whole tile is tappable (via the bottomSlot edge button) and deep-links to the
 * launch detail screen via [WearActivity.EXTRA_LAUNCH_ID].
 */
class NextLaunchTileService : TileService() {

    private val watchLaunchRepository: WatchLaunchRepository by inject()
    private val entitlementSyncManager: EntitlementSyncManager by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val log = Logger.withTag("NextLaunchTile")

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest,
    ): ListenableFuture<TileBuilders.Tile> = serviceScope.future {
        val deviceConfiguration = requestParams.deviceConfiguration

        // Resolve all suspending data fetches BEFORE entering materialScope (which is
        // not a suspend context). State drives layout selection inside the scope.
        val state: TileState = try {
            watchLaunchRepository.refreshLaunches()
            val isPremium = entitlementSyncManager.isWearOsPremium()
            if (!isPremium) {
                TileState.Free
            } else {
                val nextLaunch = watchLaunchRepository.getNextLaunch()
                if (nextLaunch != null) TileState.Launch(nextLaunch) else TileState.NoData
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to build tile" }
            TileState.Error
        }

        val rootLayout = materialScope(
            context = this@NextLaunchTileService,
            deviceConfiguration = deviceConfiguration,
        ) {
            when (state) {
                TileState.Free -> buildFreeUserLayout()
                TileState.NoData -> buildNoDataLayout()
                TileState.Error -> buildErrorLayout()
                is TileState.Launch -> buildLaunchLayout(state.launch)
            }
        }

        TileBuilders.Tile.Builder()
            .setResourcesVersion(resourcesVersionFor(state))
            .setTileTimeline(
                TimelineBuilders.Timeline.Builder()
                    .addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder()
                            .setLayout(
                                LayoutElementBuilders.Layout.Builder()
                                    .setRoot(rootLayout)
                                    .build(),
                            )
                            .build(),
                    )
                    .build(),
            )
            .setFreshnessIntervalMillis(REFRESH_INTERVAL_MS)
            .build()
    }

    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ): ListenableFuture<ResourceBuilders.Resources> = serviceScope.future {
        ResourceBuilders.Resources.Builder()
            .setVersion(requestParams.version)
            // Map-pin icon for the Location card. Static drawable — no network fetch.
            .addIdToImageMapping(
                LOCATION_ICON_ID,
                ResourceBuilders.ImageResource.Builder()
                    .setAndroidResourceByResId(
                        ResourceBuilders.AndroidImageResourceByResId.Builder()
                            .setResourceId(R.drawable.ic_location_on)
                            .build(),
                    )
                    .build(),
            )
            .build()
    }

    /**
     * Resources are versioned per-launch so the system refetches the image whenever the
     * next launch changes. For non-Launch states we use a static suffix so resources
     * stay cached.
     */
    private fun resourcesVersionFor(state: TileState): String = when (state) {
        is TileState.Launch -> "v${RESOURCES_SCHEMA_VERSION}-${state.launch.id}"
        else -> "v${RESOURCES_SCHEMA_VERSION}-static"
    }

    /**
     * Premium user with at least one upcoming launch — the happy path.
     * Two-row grid: countdown card spans the full width on top; status (small, filled
     * with status color) + location card sit side-by-side below.
     */
    private fun MaterialScope.buildLaunchLayout(launch: CachedLaunch) = primaryLayout(
        titleSlot = {
            text(
                text = formatTileTitle(launch).layoutString,
                typography = Typography.TITLE_SMALL,
                maxLines = 2,
            )
        },
        mainSlot = {
            val click = launchDetailClickable(launch.id)
            val column = LayoutElementBuilders.Column.Builder()
                .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
                .setWidth(expand())

            // Top row: countdown card, full width.
            column.addContent(countdownCard(launch, click))

            // Bottom row: small status pill + wider location card. Only render the row
            // if at least one of the two has data — otherwise the spacer is wasted.
            val abbrev = launch.statusAbbrev?.takeIf { it.isNotBlank() }
            val location = launch.padLocationName?.takeIf { it.isNotBlank() }
            if (abbrev != null || location != null) {
                column.addContent(rowSpacer())
                column.addContent(statusLocationRow(abbrev, location, click))
            }

            column.build()
        },
        bottomSlot = {
            textEdgeButton(
                onClick = launchDetailClickable(launch.id),
                labelContent = { text("Details".layoutString) },
            )
        },
    )

    private fun rowSpacer() =
        LayoutElementBuilders.Spacer.Builder().setHeight(dp(4f)).build()

    /** Card 1 — phone-style segmented `DD : HH : MM` countdown, full width. */
    private fun MaterialScope.countdownCard(
        launch: CachedLaunch,
        onClick: ModifiersBuilders.Clickable,
    ): LayoutElementBuilders.LayoutElement =
        tonalCard(onClick = onClick, content = countdownColumn(launch))

    /** Bottom row: small status pill on the left, wider location card on the right. */
    private fun MaterialScope.statusLocationRow(
        statusAbbrev: String?,
        location: String?,
        onClick: ModifiersBuilders.Clickable,
    ): LayoutElementBuilders.LayoutElement {
        val row = LayoutElementBuilders.Row.Builder()
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setWidth(expand())

        if (statusAbbrev != null) {
            row.addContent(statusCard(statusAbbrev, onClick))
        }
        if (statusAbbrev != null && location != null) {
            row.addContent(LayoutElementBuilders.Spacer.Builder().setWidth(dp(4f)).build())
        }
        if (location != null) {
            row.addContent(locationCard(location, onClick))
        }
        return row.build()
    }

    /**
     * Card 2 — small status pill, filled with the status's color role. Shows the
     * abbreviation only ("Go", "TBD", etc.) since the wider location card sits next to
     * it on the same row.
     */
    private fun MaterialScope.statusCard(
        statusAbbrev: String,
        onClick: ModifiersBuilders.Clickable,
    ): LayoutElementBuilders.LayoutElement {
        val (bgColor, textColor) = statusColors(statusAbbrev)
        return LayoutElementBuilders.Box.Builder()
            .setWidth(wrap())
            .setHeight(wrap())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(onClick)
                    .setBackground(
                        ModifiersBuilders.Background.Builder()
                            .setColor(bgColor.prop)
                            .setCorner(
                                ModifiersBuilders.Corner.Builder()
                                    .setRadius(dp(16f))
                                    .build(),
                            )
                            .build(),
                    )
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setStart(dp(12f))
                            .setEnd(dp(12f))
                            .setTop(dp(6f))
                            .setBottom(dp(6f))
                            .build(),
                    )
                    .build(),
            )
            .addContent(
                text(
                    text = statusAbbrev.layoutString,
                    typography = Typography.LABEL_SMALL,
                    color = textColor,
                    maxLines = 1,
                ),
            )
            .build()
    }

    /** Card 3 — pad location: small map-pin icon + location name (small caps body). */
    private fun MaterialScope.locationCard(
        location: String,
        onClick: ModifiersBuilders.Clickable,
    ): LayoutElementBuilders.LayoutElement =
        LayoutElementBuilders.Box.Builder()
            .setWidth(expand())
            .setHeight(wrap())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_START)
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(onClick)
                    .setBackground(
                        ModifiersBuilders.Background.Builder()
                            .setColor(colorScheme.surfaceContainer.prop)
                            .setCorner(
                                ModifiersBuilders.Corner.Builder()
                                    .setRadius(dp(16f))
                                    .build(),
                            )
                            .build(),
                    )
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setStart(dp(10f))
                            .setEnd(dp(10f))
                            .setTop(dp(6f))
                            .setBottom(dp(6f))
                            .build(),
                    )
                    .build(),
            )
            .addContent(
                LayoutElementBuilders.Row.Builder()
                    .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
                    .addContent(
                        LayoutElementBuilders.Image.Builder()
                            .setResourceId(LOCATION_ICON_ID)
                            .setWidth(dp(12f))
                            .setHeight(dp(12f))
                            .setColorFilter(
                                LayoutElementBuilders.ColorFilter.Builder()
                                    .setTint(colorScheme.onSurfaceVariant.prop)
                                    .build(),
                            )
                            .build(),
                    )
                    .addContent(
                        LayoutElementBuilders.Spacer.Builder().setWidth(dp(4f)).build(),
                    )
                    .addContent(
                        text(
                            text = location.layoutString,
                            typography = Typography.LABEL_SMALL,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        ),
                    )
                    .build(),
            )
            .build()

    /**
     * Phone-style segmented countdown row: `DD : HH : MM` with small labels under each
     * pair of digits. Status moved out to its own card.
     *
     * Seconds are intentionally dropped vs. the phone version because tiles refresh on a
     * 5-minute freshness interval and don't tick live — showing stale seconds would be
     * misleading.
     */
    private fun MaterialScope.countdownColumn(launch: CachedLaunch): LayoutElementBuilders.LayoutElement {
        val segments = computeSegments(launch)
        return LayoutElementBuilders.Row.Builder()
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .addContent(countdownSegment(twoDigit(segments.days), "DAYS"))
            .addContent(countdownColon())
            .addContent(countdownSegment(twoDigit(segments.hours), "HOURS"))
            .addContent(countdownColon())
            .addContent(countdownSegment(twoDigit(segments.minutes), "MIN"))
            .build()
    }

    /** One countdown segment: a two-digit numeral on top, a small caps label below. */
    private fun MaterialScope.countdownSegment(
        value: String,
        label: String,
    ): LayoutElementBuilders.LayoutElement =
        LayoutElementBuilders.Column.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .addContent(
                text(
                    text = value.layoutString,
                    typography = Typography.NUMERAL_EXTRA_SMALL,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                ),
            )
            .addContent(
                text(
                    text = label.layoutString,
                    typography = Typography.LABEL_SMALL,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                ),
            )
            .build()

    /** Colon separator between countdown segments. Wrapped in a Column with an invisible
     *  trailing spacer so its baseline matches the digit row in the adjacent segments
     *  (which have a label line below their digit). */
    private fun MaterialScope.countdownColon(): LayoutElementBuilders.LayoutElement =
        LayoutElementBuilders.Column.Builder()
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setStart(dp(3f))
                            .setEnd(dp(3f))
                            .build(),
                    )
                    .build(),
            )
            .addContent(
                text(
                    text = ":".layoutString,
                    typography = Typography.NUMERAL_EXTRA_SMALL,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                ),
            )
            .addContent(
                text(
                    text = " ".layoutString,
                    typography = Typography.LABEL_SMALL,
                    maxLines = 1,
                ),
            )
            .build()

    private fun twoDigit(value: Long): String = value.toString().padStart(2, '0')

    /**
     * (background, on-background) color pair for a status abbreviation, picked to match
     * the API status taxonomy in §7.4 of the findings doc.
     */
    private fun MaterialScope.statusColors(abbrev: String): Pair<LayoutColor, LayoutColor> = when (abbrev) {
        "Go", "Success", "Deployed" -> colorScheme.tertiary to colorScheme.onTertiary
        "Hold" -> colorScheme.secondary to colorScheme.onSecondary
        "Failure", "Partial Failure" -> colorScheme.error to colorScheme.onError
        "In Flight" -> colorScheme.primary to colorScheme.onPrimary
        "TBD", "TBC" -> colorScheme.surfaceContainerHigh to colorScheme.onSurface
        else -> colorScheme.surfaceContainer to colorScheme.onSurface
    }

    /**
     * Tonal card container: rounded background + padding + tappable.
     * Built as a Box rather than via [titleCard] / [textDataCard] because those M3
     * components apply opinionated text styling to their slots that doesn't compose
     * with arbitrary Column children.
     */
    private fun MaterialScope.tonalCard(
        onClick: ModifiersBuilders.Clickable,
        content: LayoutElementBuilders.LayoutElement,
    ): LayoutElementBuilders.LayoutElement =
        LayoutElementBuilders.Box.Builder()
            .setWidth(expand())
            .setHeight(wrap())
            .setHorizontalAlignment(LayoutElementBuilders.HORIZONTAL_ALIGN_CENTER)
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(onClick)
                    .setBackground(
                        ModifiersBuilders.Background.Builder()
                            .setColor(colorScheme.surfaceContainer.prop)
                            .setCorner(
                                ModifiersBuilders.Corner.Builder()
                                    .setRadius(dp(24f))
                                    .build(),
                            )
                            .build(),
                    )
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setStart(dp(10f))
                            .setEnd(dp(10f))
                            .setTop(dp(6f))
                            .setBottom(dp(6f))
                            .build(),
                    )
                    .build(),
            )
            .addContent(content)
            .build()

    /** Premium user but no upcoming launches cached. Edge button opens the app. */
    private fun MaterialScope.buildNoDataLayout() = primaryLayout(
        titleSlot = {
            text(
                text = "Space Launch Now".layoutString,
                typography = Typography.TITLE_SMALL,
                maxLines = 1,
            )
        },
        mainSlot = {
            text(
                text = "No upcoming launches".layoutString,
                typography = Typography.BODY_LARGE,
                color = colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        },
        bottomSlot = {
            textEdgeButton(
                onClick = openAppClickable(),
                labelContent = { text("Open".layoutString) },
            )
        },
    )

    /** Free (non-premium) user — the upgrade prompt. */
    private fun MaterialScope.buildFreeUserLayout() = primaryLayout(
        titleSlot = {
            text(
                text = "Space Launch Now".layoutString,
                typography = Typography.TITLE_SMALL,
                maxLines = 1,
            )
        },
        mainSlot = {
            text(
                text = "Upgrade on phone\nto see launches".layoutString,
                typography = Typography.BODY_LARGE,
                color = colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        },
        bottomSlot = {
            textEdgeButton(
                onClick = openAppClickable(),
                labelContent = { text("Open".layoutString) },
            )
        },
    )

    /** Last-resort layout shown when refresh/build threw. */
    private fun MaterialScope.buildErrorLayout() = primaryLayout(
        titleSlot = {
            text(
                text = "Couldn't load".layoutString,
                typography = Typography.TITLE_SMALL,
                maxLines = 1,
            )
        },
        mainSlot = {
            text(
                text = "Tap to retry".layoutString,
                typography = Typography.BODY_LARGE,
                color = colorScheme.onSurfaceVariant,
            )
        },
        bottomSlot = {
            textEdgeButton(
                onClick = openAppClickable(),
                labelContent = { text("Open".layoutString) },
            )
        },
    )

    /** Click action that opens the launch detail screen for [launchId]. */
    private fun launchDetailClickable(launchId: String): ModifiersBuilders.Clickable =
        ModifiersBuilders.Clickable.Builder()
            .setId("launch_detail_$launchId")
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setClassName(WearActivity::class.java.name)
                            .setPackageName(packageName)
                            .addKeyToExtraMapping(
                                WearActivity.EXTRA_LAUNCH_ID,
                                ActionBuilders.AndroidStringExtra.Builder()
                                    .setValue(launchId)
                                    .build(),
                            )
                            .build(),
                    )
                    .build(),
            )
            .build()

    /** Click action that opens the app to its default destination (list or premium gate). */
    private fun openAppClickable(): ModifiersBuilders.Clickable =
        ModifiersBuilders.Clickable.Builder()
            .setId("open_app")
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setClassName(WearActivity::class.java.name)
                            .setPackageName(packageName)
                            .build(),
                    )
                    .build(),
            )
            .build()

    /**
     * Tile title is mission name when present, otherwise the API-provided launch name
     * (which usually already encodes the mission, e.g. "Falcon 9 Block 5 | Starlink
     * Group 17-36"). Agency/vehicle are intentionally dropped — image-forward direction
     * (see docs/wear-os/CURRENT_STATE_FINDINGS.md §7.1).
     */
    private fun formatTileTitle(launch: CachedLaunch): String =
        launch.missionName?.takeIf { it.isNotBlank() } ?: launch.name

    private data class CountdownSegments(val days: Long, val hours: Long, val minutes: Long)

    private fun computeSegments(launch: CachedLaunch): CountdownSegments {
        val now = Clock.System.now()
        val duration = launch.net - now
        if (duration.isNegative()) return CountdownSegments(0, 0, 0)

        val totalMinutes = duration.inWholeMinutes
        val days = totalMinutes / (24 * 60)
        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60
        return CountdownSegments(days = days, hours = hours, minutes = minutes)
    }

    private sealed interface TileState {
        data object Free : TileState
        data object NoData : TileState
        data object Error : TileState
        data class Launch(val launch: CachedLaunch) : TileState
    }

    companion object {
        /** Bumps any time the resource schema changes (image format, ids, etc). */
        private const val RESOURCES_SCHEMA_VERSION = "6"
        private const val REFRESH_INTERVAL_MS = 300_000L // 5 minutes
        private const val LOCATION_ICON_ID = "location_icon"
    }
}
