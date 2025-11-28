package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable

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
enum class RoadmapStatus(val displayName: String) {
    COMPLETED("Completed"),
    IN_PROGRESS("In Progress"),
    PLANNED("Planned"),
    BACKLOG("Backlog")
}

/**
 * Category/type of roadmap item
 */
@Serializable
enum class RoadmapCategory(val displayName: String) {
    FEATURE("Feature"),
    ENHANCEMENT("Enhancement"),
    BUG_FIX("Bug Fix"),
    INFRASTRUCTURE("Infrastructure"),
    DESIGN("Design")
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
