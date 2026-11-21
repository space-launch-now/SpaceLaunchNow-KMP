package me.calebjones.spacelaunchnow.ui.roadmap

import androidx.compose.runtime.Composable
import me.calebjones.spacelaunchnow.data.model.RoadmapCategory
import me.calebjones.spacelaunchnow.data.model.RoadmapData
import me.calebjones.spacelaunchnow.data.model.RoadmapItem
import me.calebjones.spacelaunchnow.data.model.RoadmapPriority
import me.calebjones.spacelaunchnow.data.model.RoadmapStatus
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item1_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item1_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item2_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item2_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item3_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item3_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item4_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item4_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item5_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item5_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item6_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item6_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item7_description
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_item7_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_last_updated
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_message
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_quarter_early_2026
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_quarter_early_dec_2025
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_quarter_feb_2026
import spacelaunchnow_kmp.composeapp.generated.resources.app_roadmap_placeholder_quarter_q1_2026

/**
 * Fallback roadmap shown when Remote Config is unavailable or returns no items.
 * Built in a composable so every text comes from a string resource; the view model
 * only says that the fallback applies (see [RoadmapUiState.usePlaceholder]).
 */
@Composable
internal fun placeholderRoadmapData(): RoadmapData {
    return RoadmapData(
        items = listOf(
            RoadmapItem(
                id = "1",
                title = stringResource(Res.string.app_roadmap_placeholder_item1_title),
                description = stringResource(Res.string.app_roadmap_placeholder_item1_description),
                status = RoadmapStatus.COMPLETED,
                quarter = stringResource(Res.string.app_roadmap_placeholder_quarter_early_dec_2025),
                category = RoadmapCategory.FEATURE,
                priority = RoadmapPriority.HIGH
            ),
            RoadmapItem(
                id = "2",
                title = stringResource(Res.string.app_roadmap_placeholder_item2_title),
                description = stringResource(Res.string.app_roadmap_placeholder_item2_description),
                status = RoadmapStatus.IN_TESTING,
                quarter = stringResource(Res.string.app_roadmap_placeholder_quarter_feb_2026),
                category = RoadmapCategory.FEATURE,
                priority = RoadmapPriority.MEDIUM
            ),
            RoadmapItem(
                id = "3",
                title = stringResource(Res.string.app_roadmap_placeholder_item3_title),
                description = stringResource(Res.string.app_roadmap_placeholder_item3_description),
                status = RoadmapStatus.IN_PROGRESS,
                quarter = stringResource(Res.string.app_roadmap_placeholder_quarter_feb_2026),
                category = RoadmapCategory.BUG_FIX,
                priority = RoadmapPriority.HIGH
            ),
            RoadmapItem(
                id = "4",
                title = stringResource(Res.string.app_roadmap_placeholder_item4_title),
                description = stringResource(Res.string.app_roadmap_placeholder_item4_description),
                status = RoadmapStatus.IN_PROGRESS,
                quarter = stringResource(Res.string.app_roadmap_placeholder_quarter_q1_2026),
                category = RoadmapCategory.FEATURE,
                priority = RoadmapPriority.HIGH
            ),
            RoadmapItem(
                id = "5",
                title = stringResource(Res.string.app_roadmap_placeholder_item5_title),
                description = stringResource(Res.string.app_roadmap_placeholder_item5_description),
                status = RoadmapStatus.BACKLOG,
                quarter = stringResource(Res.string.app_roadmap_placeholder_quarter_q1_2026),
                category = RoadmapCategory.FEATURE,
                priority = RoadmapPriority.MEDIUM
            ),
            RoadmapItem(
                id = "6",
                title = stringResource(Res.string.app_roadmap_placeholder_item6_title),
                description = stringResource(Res.string.app_roadmap_placeholder_item6_description),
                status = RoadmapStatus.BACKLOG,
                quarter = stringResource(Res.string.app_roadmap_placeholder_quarter_early_2026),
                category = RoadmapCategory.FEATURE,
                priority = RoadmapPriority.LOW
            ),
            RoadmapItem(
                id = "7",
                title = stringResource(Res.string.app_roadmap_placeholder_item7_title),
                description = stringResource(Res.string.app_roadmap_placeholder_item7_description),
                status = RoadmapStatus.BACKLOG,
                quarter = stringResource(Res.string.app_roadmap_placeholder_quarter_early_2026),
                category = RoadmapCategory.FEATURE,
                priority = RoadmapPriority.MEDIUM
            )
        ),
        lastUpdated = stringResource(Res.string.app_roadmap_placeholder_last_updated),
        message = stringResource(Res.string.app_roadmap_placeholder_message)
    )
}
