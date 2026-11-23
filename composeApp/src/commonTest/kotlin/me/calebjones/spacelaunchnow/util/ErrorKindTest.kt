package me.calebjones.spacelaunchnow.util

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorKindTest {

    @Test
    fun ioFailureIsNoConnection() {
        assertEquals(ErrorKind.NO_CONNECTION, IOException("Unable to resolve host").toErrorKind())
    }

    @Test
    fun platformNetworkExceptionIsNoConnection() {
        class UnknownHostException(message: String) : Exception(message)
        assertEquals(ErrorKind.NO_CONNECTION, UnknownHostException("api.example.com").toErrorKind())
    }

    @Test
    fun requestTimeoutIsTimeout() {
        assertEquals(ErrorKind.TIMEOUT, HttpRequestTimeoutException("https://example.com", 1000L).toErrorKind())
    }

    @Test
    fun connectTimeoutIsTimeout() {
        assertEquals(ErrorKind.TIMEOUT, ConnectTimeoutException("connect timed out").toErrorKind())
    }

    @Test
    fun status429IsRateLimit() {
        assertEquals(ErrorKind.RATE_LIMIT, Exception("API Error 429: slow down").toErrorKind())
    }

    @Test
    fun throttledMessageIsRateLimit() {
        assertEquals(ErrorKind.RATE_LIMIT, Exception("Request was throttled. Expected available in 30 seconds.").toErrorKind())
    }

    @Test
    fun status5xxIsServerError() {
        assertEquals(ErrorKind.SERVER_ERROR, Exception("API Error 503: unavailable").toErrorKind())
        assertEquals(ErrorKind.SERVER_ERROR, Exception("API Error 500: boom").toErrorKind())
    }

    @Test
    fun status404IsNotFound() {
        assertEquals(ErrorKind.NOT_FOUND, Exception("API Error 404: Not found.").toErrorKind())
    }

    @Test
    fun anythingElseIsUnknown() {
        assertEquals(ErrorKind.UNKNOWN, Exception("NullPointerException at Foo.kt:12").toErrorKind())
        assertEquals(ErrorKind.UNKNOWN, Exception("API Error 400: bad filter").toErrorKind())
        assertEquals(ErrorKind.UNKNOWN, Exception().toErrorKind())
    }

    @Test
    fun causeChainIsChecked() {
        val wrapped = IllegalStateException("load failed", IOException("offline"))
        assertEquals(ErrorKind.NO_CONNECTION, wrapped.toErrorKind())
    }
}
