package me.calebjones.spacelaunchnow.util

// Function to parse ISO 8601 duration to human readable format
fun parseIsoDurationToHumanReadable(isoDuration: String): String {
    // Pattern: P[n]Y[n]M[n]DT[n]H[n]M[n]S
    // Example: P59DT12H59M = 59 days, 12 hours, 59 minutes

    val regex =
        Regex("""P(?:(\d+)Y)?(?:(\d+)M)?(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?)?""")
    val matchResult = regex.find(isoDuration) ?: return isoDuration

    val years = matchResult.groupValues[1].toIntOrNull() ?: 0
    val months = matchResult.groupValues[2].toIntOrNull() ?: 0
    val days = matchResult.groupValues[3].toIntOrNull() ?: 0
    val hours = matchResult.groupValues[4].toIntOrNull() ?: 0
    val minutes = matchResult.groupValues[5].toIntOrNull() ?: 0
    val seconds = matchResult.groupValues[6].toIntOrNull() ?: 0

    val parts = mutableListOf<String>()

    if (years > 0) parts.add("${years}y")
    if (months > 0) parts.add("${months}mo")
    if (days > 0) parts.add("${days}d")
    if (hours > 0) parts.add("${hours}h")
    if (minutes > 0) parts.add("${minutes}m")
    if (seconds > 0) parts.add("${seconds}s")

    return if (parts.isEmpty()) "0" else parts.joinToString(" ")
}