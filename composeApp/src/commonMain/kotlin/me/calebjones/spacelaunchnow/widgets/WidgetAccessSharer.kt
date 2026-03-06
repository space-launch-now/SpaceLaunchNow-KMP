package me.calebjones.spacelaunchnow.widgets

expect object WidgetAccessSharer {
    fun syncWidgetAccess(hasAccess: Boolean)
    fun syncWidgetAccessCache(cache: WidgetAccessCache)
}
