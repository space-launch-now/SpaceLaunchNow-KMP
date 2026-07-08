package me.calebjones.spacelaunchnow.util

/**
 * Share arbitrary plain text via the platform share sheet.
 * iOS: existing Swift ShareHelper via NSNotificationCenter (iPad popover safe).
 * Android: ACTION_SEND chooser. Desktop: clipboard.
 *
 * [subject] is used on Android only (EXTRA_SUBJECT + chooser title); iOS and desktop ignore it.
 */
expect fun sharePlainText(text: String, subject: String)
