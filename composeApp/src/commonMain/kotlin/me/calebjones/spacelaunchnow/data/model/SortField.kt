package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import spacelaunchnow_kmp.composeapp.generated.resources.*

/**
 * Enum representing sort options for rocket lists.
 * Each enum value maps to a specific API ordering parameter.
 * 
 * @property apiParam The parameter value sent to Launch Library API
 * @property label String resource for the name shown in the UI
 */
@Serializable
enum class SortField(
    val apiParam: String,
    val label: StringResource
) {
    NAME_ASC(
        apiParam = "name",
        label = Res.string.common_sort_name_asc
    ),
    NAME_DESC(
        apiParam = "-name",
        label = Res.string.common_sort_name_desc
    ),
    TOTAL_LAUNCHES_DESC(
        apiParam = "-total_launch_count",
        label = Res.string.rockets_sort_field_total_launches_desc
    ),
    TOTAL_LAUNCHES_ASC(
        apiParam = "total_launch_count",
        label = Res.string.rockets_sort_field_total_launches_asc
    ),
    FIRST_FLIGHT_DESC(
        apiParam = "-maiden_flight",
        label = Res.string.rockets_sort_field_first_flight_desc
    ),
    FIRST_FLIGHT_ASC(
        apiParam = "maiden_flight",
        label = Res.string.rockets_sort_field_first_flight_asc
    );
    
    companion object {
        /**
         * Default sort order for initial load.
         */
        val DEFAULT = TOTAL_LAUNCHES_DESC
        
        /**
         * Find sort field by API parameter value.
         */
        fun fromApiParam(param: String): SortField? = 
            entries.firstOrNull { it.apiParam == param }
    }
}
