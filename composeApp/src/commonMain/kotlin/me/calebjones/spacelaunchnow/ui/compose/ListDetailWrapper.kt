package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.ui.components.PlatformBackHandler
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme

/**
 * Generic wrapper for list-detail two-pane layouts on tablets/desktop.
 *
 * On Expanded windows (≥ 840dp), shows list and detail side by side.
 * On Compact/Medium windows, shows a single pane with stack-based navigation.
 *
 * Back gestures are handled within the scaffold — pressing back navigates
 * from detail pane to list pane before popping the NavController stack.
 *
 * @param T The type of item identifier passed from list to detail (e.g., String for launch IDs)
 * @param listContent The list pane content. Receives a callback to invoke when an item is selected.
 * @param detailContent The detail pane content. Receives the selected item.
 * @param emptyDetailContent Optional content shown when no item is selected (expanded only).
 * @param initialSelectedItem Optional item to pre-select on first composition (e.g. restored from saved state).
 * @param onSelectedItemChanged Optional callback when the selected item changes, for external state tracking.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun <T : Any> ListDetailWrapper(
    listContent: @Composable (onItemSelected: (T) -> Unit) -> Unit,
    detailContent: @Composable (item: T) -> Unit,
    emptyDetailContent: @Composable () -> Unit = {},
    initialSelectedItem: T? = null,
    onSelectedItemChanged: ((T?) -> Unit)? = null
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<T>()
    val scope = rememberCoroutineScope()

    // Restore previously selected item when returning from navigation
    LaunchedEffect(initialSelectedItem) {
        if (initialSelectedItem != null && navigator.currentDestination?.contentKey == null) {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, initialSelectedItem)
        }
    }

    PlatformBackHandler(enabled = navigator.canNavigateBack()) {
        scope.launch { navigator.navigateBack() }
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            listContent { item ->
                scope.launch {
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, item)
                    onSelectedItemChanged?.invoke(item)
                }
            }
        },
        detailPane = {
            val selectedItem = navigator.currentDestination?.contentKey ?: initialSelectedItem
            if (selectedItem != null) {
                detailContent(selectedItem)
            } else {
                emptyDetailContent()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(showBackground = true)
@Composable
private fun ListDetailWrapperPreview() {
    SpaceLaunchNowPreviewTheme {
        ListDetailWrapper<String>(
            listContent = { onSelect ->
                Surface {
                    Text(
                        text = "List Pane — tap an item",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            detailContent = { item ->
                Surface {
                    Text(
                        text = "Detail Pane — $item",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            emptyDetailContent = {
                Surface {
                    Text(
                        text = "Select an item to see details",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Preview(showBackground = true)
@Composable
private fun ListDetailWrapperDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        ListDetailWrapper<String>(
            listContent = { onSelect ->
                Surface {
                    Text(
                        text = "List Pane — tap an item",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            detailContent = { item ->
                Surface {
                    Text(
                        text = "Detail Pane — $item",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            emptyDetailContent = {
                Surface {
                    Text(
                        text = "Select an item to see details",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        )
    }
}
