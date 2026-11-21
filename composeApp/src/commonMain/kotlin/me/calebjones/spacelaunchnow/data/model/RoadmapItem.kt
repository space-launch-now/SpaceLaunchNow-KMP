package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import spacelaunchnow_kmp.composeapp.generated.resources.*

/**
 * Represents a single roadmap item/milestone
 * 
 * This model can be populated from:
 * - Hardcoded data in the ViewModel (current approach)
 * - Firebase Remote Config (future enhancement)
 * - Backend API endpoint
 */
@Serializable
data class RoadmapItem(
    val id: String,
    val title: String,
    val description: String,
    val status: RoadmapStatus,
    val quarter: String, // e.g., "Q1 2025", "Q2 2025"
    val category: RoadmapCategory,
    val priority: RoadmapPriority = RoadmapPriority.MEDIUM
)

/**
 * Status of a roadmap item
 */
@Serializable
enum class RoadmapStatus(val label: StringResource) {
    COMPLETED(Res.string.app_roadmap_status_completed),
    IN_TESTING(Res.string.app_roadmap_status_in_testing),
    IN_PROGRESS(Res.string.app_roadmap_status_in_progress),
    PLANNED(Res.string.app_roadmap_status_planned),
    BACKLOG(Res.string.app_roadmap_status_backlog)
}

/**
 * Category/type of roadmap item
 */
@Serializable
enum class RoadmapCategory(val label: StringResource) {
    FEATURE(Res.string.app_roadmap_category_feature),
    ENHANCEMENT(Res.string.app_roadmap_category_enhancement),
    BUG_FIX(Res.string.app_roadmap_category_bug_fix),
    INFRASTRUCTURE(Res.string.app_roadmap_category_infrastructure),
    DESIGN(Res.string.app_roadmap_category_design)
}

/**
 * Priority level for roadmap items
 */
@Serializable
enum class RoadmapPriority {
    HIGH,
    MEDIUM,
    LOW
}

/**
 * Container for all roadmap data
 * Can be serialized from Firebase Remote Config JSON
 */
@Serializable
data class RoadmapData(
    val items: List<RoadmapItem> = emptyList(),
    val lastUpdated: String? = null,
    val message: String? = null // Optional header message from Firebase
)
