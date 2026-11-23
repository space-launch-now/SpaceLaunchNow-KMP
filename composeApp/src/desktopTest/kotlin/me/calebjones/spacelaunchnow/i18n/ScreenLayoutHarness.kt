package me.calebjones.spacelaunchnow.i18n

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import coil3.annotation.ExperimentalCoilApi
import coil3.ColorImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalNavAnimatedVisibilityScope
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalSharedTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runDesktopComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.toAwtImage
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.koin.compose.KoinIsolatedContext
import org.koin.core.KoinApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import java.io.File
import java.util.Locale
import javax.imageio.ImageIO
import kotlin.test.assertTrue

/**
 * Languages the dense-screen layout tests render: English is the baseline; German, Spanish and
 * French have the longest text (Spanish and French average 21 percent longer than English, German
 * 12 percent with long compound words); Ukrainian has the widest letters.
 */
enum class ScreenLanguage(val tag: String) {
    EN("en"),
    DE("de"),
    ES("es"),
    FR("fr"),
    UK("uk");

    val locale: Locale get() = Locale.forLanguageTag(tag)
}

/**
 * Renders one screen per language at phone size and compares its text-layout report with the stored
 * reference (`src/desktopTest/resources/snapshots/layout/<screen>_<lang>.txt`, refreshed with
 * `I18N_SNAPSHOT_UPDATE=true` like the text-tree snapshots).
 *
 * The report lists every text that is cut off or clipped (hidden lines, an ellipsis or a hard
 * clip), every text node wider than the screen, and every pair of texts that overlap.
 * A screen that grows a new cut-off text in any language fails until the reference is reviewed.
 * Every run also saves the rendered PNG under `build/i18n-screenshots/` for a human to open.
 *
 * Pixel references are not stored: Skia text metrics differ per operating system, so a pixel diff
 * would fail on CI for reasons unrelated to the change. The test ships Roboto and the layout
 * report is the stored reference. See the pull request for the reasoning.
 */
@OptIn(ExperimentalTestApi::class)
object ScreenLayoutHarness {
    const val PHONE_WIDTH = 360
    const val PHONE_HEIGHT = 1400
    private const val OVERLAP_PX = 2f

    private val robotoFamily: FontFamily by lazy {
        val bytes = ScreenLayoutHarness::class.java.getResourceAsStream("/fonts/Roboto-Regular.ttf")!!.readBytes()
        FontFamily(
            Font("roboto-regular", bytes, FontWeight.Normal),
            Font("roboto-regular", bytes, FontWeight.Medium),
            Font("roboto-regular", bytes, FontWeight.Bold),
        )
    }

    /** Runs [content] in every [ScreenLanguage]. [koinModules] builds fresh fakes for each render; [settle]
     * runs after the first idle (wait for async data here) and before the capture. */
    fun assertLayout(
        screen: String,
        languages: List<ScreenLanguage> = ScreenLanguage.entries,
        settle: ComposeUiTest.() -> Unit = { waitForIdle() },
        koinModules: () -> List<Module> = { emptyList() },
        content: @Composable () -> Unit,
    ) {
        val previous = Locale.getDefault()
        try {
            languages.forEach { language ->
                Locale.setDefault(language.locale)
                // One Koin per render, never the global one, so tests cannot leak definitions.
                val koin = koinApplication { modules(koinModules()) }
                try {
                    runDesktopComposeUiTest(width = PHONE_WIDTH, height = PHONE_HEIGHT) {
                        setContent {
                            SpaceLaunchNowPreviewTheme { FixedFont { SharedTransitionHost { WithKoin(koin, content) } } }
                        }
                        waitForIdle()
                        settle()
                        waitForIdle()
                        val name = "${screen}_${language.tag}"
                        saveImage(name)
                        TextTreeSnapshot.assertMatches("layout/$name", report(this))
                    }
                } finally {
                    koin.close()
                }
            }
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Composable
    private fun FixedFont(content: @Composable () -> Unit) {
        val t = MaterialTheme.typography
        val withFont = Typography(
            displayLarge = t.displayLarge.withFont(), displayMedium = t.displayMedium.withFont(),
            displaySmall = t.displaySmall.withFont(), headlineLarge = t.headlineLarge.withFont(),
            headlineMedium = t.headlineMedium.withFont(), headlineSmall = t.headlineSmall.withFont(),
            titleLarge = t.titleLarge.withFont(), titleMedium = t.titleMedium.withFont(),
            titleSmall = t.titleSmall.withFont(), bodyLarge = t.bodyLarge.withFont(),
            bodyMedium = t.bodyMedium.withFont(), bodySmall = t.bodySmall.withFont(),
            labelLarge = t.labelLarge.withFont(), labelMedium = t.labelMedium.withFont(),
            labelSmall = t.labelSmall.withFont(),
        )
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme,
            shapes = MaterialTheme.shapes,
            typography = withFont,
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.withFont(),
                // Inspection mode plus a preview handler shows a flat colour for every remote image:
                // no network, and the loading shimmer cannot draw on this Skiko build.
                LocalInspectionMode provides true,
                LocalAsyncImagePreviewHandler provides offlineImageHandler,
                content = content,
            )
        }
    }

    /** Screens that inject services read the private Koin that holds the test's fakes. */
    @OptIn(KoinExperimentalAPI::class)
    @Composable
    private fun WithKoin(app: KoinApplication, content: @Composable () -> Unit) {
        KoinIsolatedContext(context = app, content = content)
    }

    /** Screens with shared-element transitions read these two locals from the nav host. */
    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    private fun SharedTransitionHost(content: @Composable () -> Unit) {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                AnimatedVisibility(visibleState = remember { MutableTransitionState(true) }) {
                    CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this, content = content)
                }
            }
        }
    }

    @OptIn(ExperimentalCoilApi::class)
    private val offlineImageHandler = AsyncImagePreviewHandler { ColorImage(0xFF607D8B.toInt(), 64, 64) }

    private fun TextStyle.withFont(): TextStyle = copy(fontFamily = robotoFamily)

    private fun ComposeUiTest.saveImage(name: String) {
        val image = onRoot().captureToImage().toAwtImage()
        // A blank capture means the screen never drew; fail loudly instead of storing a clean report.
        val first = image.getRGB(0, 0)
        var varied = false
        loop@ for (y in 0 until image.height step 8) {
            for (x in 0 until image.width step 8) {
                if (image.getRGB(x, y) != first) {
                    varied = true
                    break@loop
                }
            }
        }
        assertTrue(varied, "Screen '$name' rendered as one flat colour")
        val file = File("build/i18n-screenshots/$name.png")
        file.parentFile.mkdirs()
        ImageIO.write(image, "png", file)
    }

    private fun report(test: ComposeUiTest): String {
        val root = test.onRoot(useUnmergedTree = true).fetchSemanticsNode()
        val rootWidth = root.boundsInRoot.width
        val lines = sortedSetOf<String>()
        val boxes = mutableListOf<Pair<String, Rect>>()
        var textNodes = 0
        fun visit(node: SemanticsNode) {
            val text = node.config.getOrNull(SemanticsProperties.Text)?.joinToString(" | ") { it.text }
            if (text != null) {
                textNodes++
                boxes += text to node.boundsInRoot
                val layout = textLayout(node)
                layout?.cutOffReason()?.let { lines += "$it \"${escape(text)}\"" }
                if (node.boundsInRoot.width > rootWidth + 0.5f) lines += "wider-than-screen \"${escape(text)}\""
            }
            node.children.forEach(::visit)
        }
        visit(root)
        // Two texts that share screen area push into each other, even when neither one is cut off.
        for (i in boxes.indices) for (j in i + 1 until boxes.size) {
            val overlap = boxes[i].second.intersect(boxes[j].second)
            if (overlap.width > OVERLAP_PX && overlap.height > OVERLAP_PX) {
                lines += "overlap \"${escape(boxes[i].first)}\" with \"${escape(boxes[j].first)}\""
            }
        }
        return buildString {
            append("texts=").append(textNodes).append('\n')
            append("findings=").append(lines.size).append('\n')
            lines.forEach { append(it).append('\n') }
        }
    }

    /**
     * Why this text does not show in full, or null when it does. `TextLayoutResult.hasVisualOverflow`
     * is not used: it flags texts that fit, so each cause is checked on its own.
     */
    private fun TextLayoutResult.cutOffReason(): String? {
        val lineWidth = (0 until lineCount).maxOfOrNull { getLineRight(it) - getLineLeft(it) } ?: 0f
        return when {
            multiParagraph.didExceedMaxLines -> "hidden-lines"
            (0 until lineCount).any { isLineEllipsized(it) } -> "ellipsis"
            size.height + 1 < multiParagraph.height -> "clipped-height"
            lineWidth > size.width + 1f -> "clipped-width"
            else -> null
        }
    }

    private fun textLayout(node: SemanticsNode): TextLayoutResult? {
        val action = node.config.getOrNull(SemanticsActions.GetTextLayoutResult) ?: return null
        val results = mutableListOf<TextLayoutResult>()
        action.action?.invoke(results)
        return results.firstOrNull()
    }

    /** Digits become `#` so a countdown or a date does not change the stored report from day to day. */
    private fun escape(value: String) = value.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"")
        .replace(Regex("\\d+"), "#")
}
