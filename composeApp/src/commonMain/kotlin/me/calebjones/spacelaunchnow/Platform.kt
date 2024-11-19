package me.calebjones.spacelaunchnow

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform