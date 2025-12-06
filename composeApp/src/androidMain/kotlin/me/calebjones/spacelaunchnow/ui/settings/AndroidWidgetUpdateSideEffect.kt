package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import me.calebjones.spacelaunchnow.widgets.WidgetUpdater
import org.koin.compose.viewmodel.koinViewModel

private val log by lazy { SpaceLogger.getLogger("AndroidWidgetUpdateSideEffect") }

/**
 * Android implementation of widget update side effect.
 * Triggers widget updates ONLY when user applies changes (not on every slider move).
 * Uses an event counter (increments on each apply) to reliably detect when apply button is clicked.
 */
@Composable
actual fun WidgetUpdateSideEffect() {
    val context = LocalContext.current
    val viewModel = koinViewModel<ThemeCustomizationViewModel>()

    val applyTrigger by viewModel.widgetApplyTrigger.collectAsStateWithLifecycle()

    LaunchedEffect(applyTrigger) {
        // Skip initial load (counter == 0)
        if (applyTrigger > 0) {

            try {
                // No delay needed! Widget reads from Glance state that we write directly
                // This eliminates cross-process DataStore timing issues
                WidgetUpdater.updateAllWidgets(context)
            } catch (e: Exception) {
                log.e(e) { "ERROR during widget update" }
            }
        }
    }
}
