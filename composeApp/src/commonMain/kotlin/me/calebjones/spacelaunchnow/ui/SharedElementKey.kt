package me.calebjones.spacelaunchnow.ui

data class SharedElementKey(val launchId: String, val type: SharedElementType)

enum class SharedElementType {
    Image,
    Title,
    Bounds,
    Background,
    Tagline
}

object FilterSharedElementKey