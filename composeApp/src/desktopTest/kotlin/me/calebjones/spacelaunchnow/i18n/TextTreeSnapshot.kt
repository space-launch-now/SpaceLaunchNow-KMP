package me.calebjones.spacelaunchnow.i18n

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Text-tree snapshot (spec 017, section 6.2). Dumps the text and content descriptions of a
 * semantics tree to a stable string and compares it with a stored file.
 *
 * Stored files: `composeApp/src/desktopTest/resources/snapshots/<name>.txt`.
 * To write or refresh them run `:composeApp:desktopTest with I18N_SNAPSHOT_UPDATE=true`.
 */
object TextTreeSnapshot {
    private const val UPDATE_ENV = "I18N_SNAPSHOT_UPDATE"
    private const val RESOURCE_DIR = "snapshots"

    @OptIn(ExperimentalTestApi::class)
    fun dump(test: ComposeUiTest): String =
        buildString { dumpNode(test.onRoot(useUnmergedTree = true).fetchSemanticsNode(), 0) }

    fun assertMatches(name: String, actual: String) {
        val file = File("src/desktopTest/resources/$RESOURCE_DIR/$name.txt")
        if (System.getenv(UPDATE_ENV) == "true") {
            file.parentFile.mkdirs()
            file.writeText(actual)
            return
        }
        val stored = TextTreeSnapshot::class.java.getResource("/$RESOURCE_DIR/$name.txt")
            ?.readText()
            ?: fail("No stored snapshot '$name'. Run :composeApp:desktopTest with I18N_SNAPSHOT_UPDATE=true")
        assertEquals(stored.normalize(), actual.normalize(), "Text tree of '$name' changed")
    }

    private fun String.normalize() = replace("\r\n", "\n").trimEnd()

    private fun StringBuilder.dumpNode(node: SemanticsNode, depth: Int) {
        val config = node.config
        val text = config.getOrNull(SemanticsProperties.Text)?.joinToString(" | ") { escape(it.text) }
        val description = config.getOrNull(SemanticsProperties.ContentDescription)
            ?.joinToString(" | ") { escape(it) }
        val clickable = config.getOrNull(SemanticsActions.OnClick) != null
        val parts = buildList {
            text?.let { add("text=\"$it\"") }
            description?.let { add("description=\"$it\"") }
            if (clickable) add("click")
        }
        if (parts.isNotEmpty()) {
            append("  ".repeat(depth)).append(parts.joinToString(" ")).append('\n')
        }
        val childDepth = if (parts.isEmpty()) depth else depth + 1
        node.children.forEach { dumpNode(it, childDepth) }
    }

    private fun escape(value: String) = value.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"")
}
