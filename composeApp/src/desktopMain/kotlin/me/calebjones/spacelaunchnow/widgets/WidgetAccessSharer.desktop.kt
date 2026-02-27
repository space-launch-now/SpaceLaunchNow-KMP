package me.calebjones.spacelaunchnow.widgets

actual object WidgetAccessSharer {
    actual fun syncWidgetAccess(hasAccess: Boolean) {
        // No-op on Desktop — no widget system
    }
}
