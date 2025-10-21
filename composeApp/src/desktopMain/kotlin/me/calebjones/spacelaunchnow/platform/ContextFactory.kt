package me.calebjones.spacelaunchnow.platform

/**
 * Desktop implementation of ContextFactory (returns empty objects)
 */
actual class ContextFactory actual constructor(context: Any?) {
    actual fun getContext(): Any = ""
    actual fun getApplication(): Any = ""
    actual fun getActivity(): Any = ""
}