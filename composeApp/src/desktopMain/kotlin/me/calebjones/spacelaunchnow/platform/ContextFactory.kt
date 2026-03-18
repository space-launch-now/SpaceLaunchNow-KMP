package me.calebjones.spacelaunchnow.platform

/**
 * Desktop implementation of ContextFactory (returns empty objects)
 */
actual class ContextFactory {
    actual fun getContext(): Any = ""
    actual fun getApplication(): Any = ""
    actual fun getActivity(): Any = ""
}