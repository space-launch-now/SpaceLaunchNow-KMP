package me.calebjones.spacelaunchnow.platform

import android.content.Context
import android.content.Intent

/**
 * Class name of the launcher activity. It lives in the `:androidApp` module, which depends on this
 * module, so shared code reaches it by name instead of by class reference.
 */
private const val MAIN_ACTIVITY_CLASS_NAME = "me.calebjones.spacelaunchnow.MainActivity"

/** Explicit intent for the launcher activity; same as `Intent(context, MainActivity::class.java)`. */
fun mainActivityIntent(context: Context): Intent =
    Intent().setClassName(context, MAIN_ACTIVITY_CLASS_NAME)
