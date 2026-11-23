package me.calebjones.spacelaunchnow.util

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.io.IOException
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.common_error_no_connection
import spacelaunchnow_kmp.composeapp.generated.resources.common_error_not_found
import spacelaunchnow_kmp.composeapp.generated.resources.common_error_rate_limit
import spacelaunchnow_kmp.composeapp.generated.resources.common_error_server
import spacelaunchnow_kmp.composeapp.generated.resources.common_error_timeout
import spacelaunchnow_kmp.composeapp.generated.resources.common_unknown_error

/**
 * The fixed list of errors a user can see. A view model maps a [Throwable] to one kind and shows
 * the kind's own text; the raw exception text never reaches the screen (log it instead).
 */
enum class ErrorKind(val text: StringResource) {
    NO_CONNECTION(Res.string.common_error_no_connection),
    TIMEOUT(Res.string.common_error_timeout),
    RATE_LIMIT(Res.string.common_error_rate_limit),
    SERVER_ERROR(Res.string.common_error_server),
    NOT_FOUND(Res.string.common_error_not_found),
    UNKNOWN(Res.string.common_unknown_error),
}

/** Platform network exceptions that common code cannot name (JVM, Android and Darwin). */
private val noConnectionClassNames = setOf(
    "UnknownHostException",
    "UnresolvedAddressException",
    "ConnectException",
    "NoRouteToHostException",
    "SocketException",
)

private val statusInMessage = Regex("""API Error (\d{3})""")

/** Maps this exception, or the first cause that fits, to an [ErrorKind]. */
fun Throwable.toErrorKind(): ErrorKind {
    var current: Throwable? = this
    var depth = 0
    while (current != null && depth < 8) {
        current.directKind()?.let { return it }
        current = current.cause
        depth++
    }
    return ErrorKind.UNKNOWN
}

private fun Throwable.directKind(): ErrorKind? = when {
    this is HttpRequestTimeoutException ||
        this is ConnectTimeoutException ||
        this is SocketTimeoutException -> ErrorKind.TIMEOUT
    this is ResponseException -> statusKind(response.status.value)
    this::class.simpleName in noConnectionClassNames -> ErrorKind.NO_CONNECTION
    this is IOException -> ErrorKind.NO_CONNECTION
    else -> messageKind(message)
}

/** The repositories wrap some failures as `Exception("API Error 429: ...")`; read the status back. */
private fun messageKind(message: String?): ErrorKind? {
    if (message == null) return null
    if (message.contains("throttled", ignoreCase = true)) return ErrorKind.RATE_LIMIT
    val status = statusInMessage.find(message)?.groupValues?.get(1)?.toIntOrNull() ?: return null
    return statusKind(status)
}

private fun statusKind(status: Int): ErrorKind? = when {
    status == 429 -> ErrorKind.RATE_LIMIT
    status == 404 -> ErrorKind.NOT_FOUND
    status in 500..599 -> ErrorKind.SERVER_ERROR
    else -> null
}

/** The translated text for this error, in the current app language. */
suspend fun Throwable.toUserMessage(): String = getString(toErrorKind().text)
