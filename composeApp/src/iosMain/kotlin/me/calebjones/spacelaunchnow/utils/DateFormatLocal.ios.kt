import kotlinx.datetime.Instant
import platform.Foundation.*

actual fun formatTimeForUser(instant: Instant): String {
    val formatter = NSDateFormatter().apply {
        dateStyle = NSDateFormatterNoStyle
        timeStyle = NSDateFormatterShortStyle
        locale = NSLocale.currentLocale
        timeZone = NSTimeZone.systemTimeZone
    }

    val date = NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
    return formatter.stringFromDate(date)
}
