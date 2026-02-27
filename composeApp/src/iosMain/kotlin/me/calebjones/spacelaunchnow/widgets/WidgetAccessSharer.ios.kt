package me.calebjones.spacelaunchnow.widgets

import platform.Foundation.NSUserDefaults

actual object WidgetAccessSharer {
    actual fun syncWidgetAccess(hasAccess: Boolean) {
        val defaults = NSUserDefaults(suiteName = "group.me.calebjones.spacelaunchnow")
        defaults?.setBool(hasAccess, forKey = "widget_has_access")
    }
}
