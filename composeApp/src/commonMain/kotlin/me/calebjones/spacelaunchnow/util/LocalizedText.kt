package me.calebjones.spacelaunchnow.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Text that still waits for the device language. Compose string resources load only inside a
 * composable ([resolve]) or a coroutine ([load]), so the pure formatting code builds this tree
 * and the caller picks how to read it.
 */
sealed interface LocalizedText {
    /** Text that needs no translation, such as a date the platform already formatted. */
    data class Plain(val value: String) : LocalizedText

    /** A string resource. An argument may itself be a [LocalizedText]. */
    data class Str(val res: StringResource, val args: List<Any> = emptyList()) : LocalizedText

    /** Parts joined by [separator]. */
    data class Joined(val parts: List<LocalizedText>, val separator: String = " ") : LocalizedText
}

@Composable
fun LocalizedText.resolve(): String = when (this) {
    is LocalizedText.Plain -> value
    is LocalizedText.Str -> stringResource(res, *args.map { if (it is LocalizedText) it.resolve() else it }.toTypedArray())
    is LocalizedText.Joined -> parts.map { it.resolve() }.joinToString(separator)
}

suspend fun LocalizedText.load(): String = when (this) {
    is LocalizedText.Plain -> value
    is LocalizedText.Str -> getString(res, *args.map { if (it is LocalizedText) it.load() else it }.toTypedArray())
    is LocalizedText.Joined -> parts.map { it.load() }.joinToString(separator)
}
