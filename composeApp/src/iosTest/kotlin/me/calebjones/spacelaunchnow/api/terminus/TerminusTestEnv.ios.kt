package me.calebjones.spacelaunchnow.api.terminus

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
internal actual fun terminusTestEnv(name: String): String? = getenv(name)?.toKString()
