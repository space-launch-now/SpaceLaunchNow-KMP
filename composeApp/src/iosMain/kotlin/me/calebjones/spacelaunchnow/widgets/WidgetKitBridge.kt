package me.calebjones.spacelaunchnow.widgets

/**
 * Bridge interface allowing Swift to register a WidgetKit timeline reload handler.
 * This avoids importing platform.WidgetKit directly in Kotlin/Native,
 * which requires explicit cinterop configuration.
 *
 * Swift registration (AppDelegate.swift):
 *   WidgetKitBridge.shared.reloader = SwiftWidgetTimelineReloader()
 */
interface WidgetTimelineReloader {
    fun reloadAllTimelines()
}

object WidgetKitBridge {
    var reloader: WidgetTimelineReloader? = null

    fun requestReload() {
        reloader?.reloadAllTimelines()
    }
}
