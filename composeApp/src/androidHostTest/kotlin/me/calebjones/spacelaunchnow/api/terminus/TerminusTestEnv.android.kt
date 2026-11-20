package me.calebjones.spacelaunchnow.api.terminus

internal actual fun terminusTestEnv(name: String): String? = System.getenv(name)
