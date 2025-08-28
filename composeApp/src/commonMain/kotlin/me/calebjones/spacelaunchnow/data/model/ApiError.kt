package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val detail: String? = null,
    val error: String? = null,
    val message: String? = null
) {
    fun getErrorMessage(): String {
        return detail ?: error ?: message ?: "Unknown API error"
    }
}
