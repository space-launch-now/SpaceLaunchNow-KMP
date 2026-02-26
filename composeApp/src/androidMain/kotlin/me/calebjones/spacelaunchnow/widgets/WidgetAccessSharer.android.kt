package me.calebjones.spacelaunchnow.widgets

actual object WidgetAccessSharer {
    actual fun syncWidgetAccess(hasAccess: Boolean) {
        // No-op on Android — widget access is managed via Glance DataStore
    }
}
