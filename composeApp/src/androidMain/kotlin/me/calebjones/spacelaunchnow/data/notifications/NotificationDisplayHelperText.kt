package me.calebjones.spacelaunchnow.data.notifications

import androidx.annotation.StringRes
import me.calebjones.spacelaunchnow.shared.R

/**
 * Body text the app builds itself for a launch push (the legacy V4 payload).
 *
 * Every text comes from a string resource, so it follows the device language. [resName] is the
 * name in `strings.xml`; the unit test uses it to read each translation file.
 */
internal enum class LaunchBodyText(
    val resName: String,
    @StringRes val resId: Int,
    val takesTime: Boolean,
) {
    SCHEDULE_CHANGED("notification_schedule_changed", R.string.notification_schedule_changed, true),
    SUCCESS("notification_launch_successful", R.string.notification_launch_successful, false),
    FAILURE("notification_launch_failure", R.string.notification_launch_failure, false),
    PARTIAL_FAILURE("notification_launch_partial_failure", R.string.notification_launch_partial_failure, false),
    LIFTOFF("notification_launch_liftoff", R.string.notification_launch_liftoff, false),
    ONE_MINUTE("notification_one_minute", R.string.notification_one_minute, true),
    TEN_MINUTES("notification_ten_minutes", R.string.notification_ten_minutes, true),
    ONE_HOUR("notification_one_hour", R.string.notification_one_hour, true),
    TWENTY_FOUR_HOURS("notification_twenty_four_hours", R.string.notification_twenty_four_hours, true),
    LAUNCH_FROM("notification_fallback_launch_from", R.string.notification_fallback_launch_from, false);

    companion object {
        /** The text for a payload `notification_type`, or null for a type the app does not know. */
        fun forType(notificationType: String): LaunchBodyText? = when (notificationType.lowercase()) {
            "netstampchanged" -> SCHEDULE_CHANGED
            "success" -> SUCCESS
            "failure" -> FAILURE
            "partialfailure" -> PARTIAL_FAILURE
            "inflight" -> LIFTOFF
            "oneminute" -> ONE_MINUTE
            "tenminutes" -> TEN_MINUTES
            "onehour" -> ONE_HOUR
            "twentyfourhour" -> TWENTY_FOUR_HOURS
            else -> null
        }
    }
}

/**
 * Builds the launch body from the payload fields. A known type uses its string resource; an
 * unknown type shows the server text, and only when that is blank the "Launch from" text.
 *
 * @param formattedTime the launch time, already formatted for the user (a lazy lambda: only the
 *   types that show a time pay for the parse)
 * @param getString resolves a string resource with its format arguments
 */
internal fun buildLaunchBody(
    notificationType: String,
    launchLocation: String,
    serverBody: String?,
    formattedTime: () -> String,
    getString: (resId: Int, args: List<Any>) -> String,
): String {
    val text = LaunchBodyText.forType(notificationType)
        ?: return serverBody?.takeIf { it.isNotBlank() }
            ?: getString(LaunchBodyText.LAUNCH_FROM.resId, listOf(launchLocation))
    return getString(text.resId, if (text.takesTime) listOf(formattedTime()) else emptyList())
}
