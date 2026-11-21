package me.calebjones.spacelaunchnow.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * Gives a clickable card a spoken description and the button role.
 * Takes the text as a String so callers resolve it with stringResource in composition.
 */
fun Modifier.buttonContentDescription(description: String): Modifier =
    semantics {
        contentDescription = description
        role = Role.Button
    }
