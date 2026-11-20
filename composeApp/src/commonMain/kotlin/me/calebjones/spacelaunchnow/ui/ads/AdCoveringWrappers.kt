@file:OptIn(ExperimentalMaterial3Api::class)

package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties

/*
 * Same parameters as the Material 3 originals. Each one raises the ad cover (see AdCover.kt)
 * while it is on screen, so banner ads leave composition and are never partly covered.
 */

@Composable
fun AdCoveringModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = 0.dp,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    content: @Composable ColumnScope.() -> Unit,
) {
    RaiseAdCover()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = sheetMaxWidth,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        contentWindowInsets = contentWindowInsets,
        properties = properties,
        content = content,
    )
}

@Composable
fun AdCoveringAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
) {
    RaiseAdCover()
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties,
    )
}

@Composable
fun AdCoveringDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    RaiseAdCover()
    Dialog(onDismissRequest = onDismissRequest, properties = properties, content = content)
}

/** Raises the cover only while [expanded]. */
@Composable
fun AdCoveringDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    shape: Shape = MenuDefaults.shape,
    containerColor: Color = MenuDefaults.containerColor,
    tonalElevation: Dp = MenuDefaults.TonalElevation,
    shadowElevation: Dp = MenuDefaults.ShadowElevation,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    RaiseAdCover(active = expanded)
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        scrollState = scrollState,
        properties = properties,
        shape = shape,
        containerColor = containerColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        content = content,
    )
}

/** Raises the cover only while [expanded]. */
@Composable
fun ExposedDropdownMenuBoxScope.AdCoveringExposedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    matchAnchorWidth: Boolean = true,
    shape: Shape = MenuDefaults.shape,
    containerColor: Color = MenuDefaults.containerColor,
    tonalElevation: Dp = MenuDefaults.TonalElevation,
    shadowElevation: Dp = MenuDefaults.ShadowElevation,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    RaiseAdCover(active = expanded)
    ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        scrollState = scrollState,
        matchAnchorWidth = matchAnchorWidth,
        shape = shape,
        containerColor = containerColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        content = content,
    )
}
