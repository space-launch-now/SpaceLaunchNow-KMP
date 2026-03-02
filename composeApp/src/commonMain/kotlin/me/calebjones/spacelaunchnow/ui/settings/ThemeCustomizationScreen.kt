package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.Primary
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.preferences.WidgetPreferences
import me.calebjones.spacelaunchnow.data.preferences.WidgetThemeSource
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccess
import me.calebjones.spacelaunchnow.data.storage.ThemePreferences
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.navigation.SupportUs
import me.calebjones.spacelaunchnow.ui.subscription.PremiumBadge
import me.calebjones.spacelaunchnow.util.logging.logger
import me.calebjones.spacelaunchnow.ui.subscription.PremiumPromptCard
import me.calebjones.spacelaunchnow.ui.subscription.TemporaryPremiumCard
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import org.koin.compose.viewmodel.koinViewModel

/**
 * Platform-specific widget update composable.
 * Android: Triggers widget updates when settings change.
 * iOS/Desktop: No-op.
 */
@Composable
expect fun WidgetUpdateSideEffect()

/**
 * Theme Customization Screen
 * - Free users: Can change Light/Dark/System theme
 * - Premium users: Can also customize colors and palette styles
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeCustomizationScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel = koinViewModel<ThemeCustomizationViewModel>()
    val hasCustomTheme by viewModel.hasCustomTheme.collectAsStateWithLifecycle()
    val hasWidgetCustomization by viewModel.hasWidgetCustomization.collectAsStateWithLifecycle()
    val hasPermanentPremium by viewModel.hasPermanentPremium.collectAsStateWithLifecycle()
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val selectedPrimaryColor by viewModel.selectedPrimaryColor.collectAsStateWithLifecycle()
    val selectedPaletteStyle by viewModel.selectedPaletteStyle.collectAsStateWithLifecycle()
    val applyToWidgets by viewModel.applyToWidgets.collectAsStateWithLifecycle()
    val hasUnappliedWidgetChanges by viewModel.hasUnappliedWidgetChanges.collectAsStateWithLifecycle()

    // Platform-specific widget updates (triggers when user applies changes)
    WidgetUpdateSideEffect()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Theme Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!hasCustomTheme) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Premium Feature",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Selection (FREE - Available to all users)
            item {
                SectionHeaderText("Appearance")
                SectionSubHeaderText("Choose between light, dark, or system theme")
                SettingsCardRow {
                    ThemeSettingRow(
                        selected = selectedTheme,
                        onSelected = viewModel::updateTheme,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Premium Prompt (only show if no premium access at all)
            if (!hasCustomTheme) {
                item {
                    PremiumPromptCard(
                        title = "Unlock Custom Themes",
                        description = "Customize your app's theme with custom colors and palette styles",
                        icon = Icons.Default.Palette,
                        onUpgradeClick = {
                            navController.navigate(SupportUs)
                        }
                    )
                }
            }

            // Temporary Premium Access via Rewarded Ads (always show)
            item {
                TemporaryPremiumCard(
                    temporaryPremiumAccess = viewModel.temporaryPremiumAccess,
                    features = listOf(
                        PremiumFeature.CUSTOM_THEMES,
                        PremiumFeature.ADVANCED_WIDGETS,
                        PremiumFeature.WIDGETS_CUSTOMIZATION
                    ),
                    title = "24h Premium Access",
                    description = "Watch an ad to unlock premium theme features for 24 hours",
                    icon = Icons.Default.LockClock,
                    hasPermanentPremium = hasPermanentPremium
                )
            }

            // Color Customization (PREMIUM - Disabled for free users)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeaderText("Primary Color")
                        Spacer(Modifier.weight(1f))
                        Spacer(Modifier.width(4.dp))
                        if (!hasCustomTheme) {
                            PremiumBadge()
                        }
                    }
                    SectionSubHeaderText("Choose the primary color for your theme.")
                }
                SettingsCardRow {
                    ColorPalette(
                        selectedColor = selectedPrimaryColor,
                        onColorSelected = if (hasCustomTheme) {
                            { viewModel.updatePrimaryColor(it) }
                        } else {
                            { /* Disabled */ }
                        },
                        enabled = hasCustomTheme,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                SectionHeaderText("Palette Style")
                SectionSubHeaderText(
                    "Choose how colors are generated from your primary color"
                )
                SettingsCardRow {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemePreferences.AVAILABLE_PALETTE_STYLES.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { style ->
                                    PaletteStyleItem(
                                        name = style,
                                        description = getPaletteStyleDescription(style),
                                        isSelected = selectedPaletteStyle == style,
                                        onClick = if (hasCustomTheme) {
                                            { viewModel.updatePaletteStyle(style) }
                                        } else {
                                            { /* Disabled */ }
                                        },
                                        enabled = hasCustomTheme,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // // Fill remaining space if odd number of items in last row
                                // if (rowItems.size < 2) {
                                //     Spacer(Modifier.weight(1f))
                                // }
                            }
                        }
                    }
                }
            }

            // Widget Appearance Customization (Show all controls, disable for non-premium)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeaderText("Widget Appearance")
                        Spacer(Modifier.weight(1f))
                        Spacer(Modifier.width(4.dp))
                        if (!hasWidgetCustomization) {
                            PremiumBadge()
                        }
                    }
                    SectionSubHeaderText("Customize the look of your home screen widgets")
                }
            }

            // Widget Theme Source Selection (Android only - iOS doesn't support Material 3 dynamic colors)
            if (getPlatform().type.isAndroid) {
                item {
                    SettingsCardRow {
                        val widgetThemeSource by viewModel.widgetThemeSource.collectAsStateWithLifecycle()
                        WidgetThemeSourceSelector(
                            selectedSource = widgetThemeSource,
                            onSourceSelected = {
                                if (hasCustomTheme) viewModel.updateWidgetThemeSource(
                                    it
                                )
                            },
                            enabled = hasWidgetCustomization,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Widget Background Transparency (Android only - iOS WidgetKit controls background)
            if (getPlatform().type.isAndroid) {
                item {
                    val widgetBackgroundAlpha by viewModel.widgetBackgroundAlpha.collectAsStateWithLifecycle()
                    SettingsCardRow {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Background Transparency",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (hasWidgetCustomization) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${(widgetBackgroundAlpha * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (hasWidgetCustomization) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                "0% = Fully transparent, 100% = Fully opaque",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasWidgetCustomization) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                            Slider(
                                value = widgetBackgroundAlpha,
                                onValueChange = {
                                    if (hasWidgetCustomization) viewModel.updateWidgetBackgroundAlpha(
                                        it
                                    )
                                },
                                valueRange = 0f..1f,
                                enabled = hasWidgetCustomization,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Widget Corner Radius
                item {
                    val widgetCornerRadius by viewModel.widgetCornerRadius.collectAsStateWithLifecycle()
                    SettingsCardRow {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Corner Radius",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (hasWidgetCustomization) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${widgetCornerRadius}dp",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (hasWidgetCustomization) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                "Rounded corners for widget background (0-40dp, increments of 4dp)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasWidgetCustomization) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                            Slider(
                                value = widgetCornerRadius.toFloat(),
                                onValueChange = {
                                    if (hasWidgetCustomization) {
                                        // Round to nearest multiple of 4
                                        val roundedValue = ((it / 4f).toInt() * 4).coerceIn(0, 40)
                                        viewModel.updateWidgetCornerRadius(roundedValue)
                                    }
                                },
                                valueRange = 0f..40f,
                                steps = 9, // 0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40 = 11 positions, so 9 steps between them
                                enabled = hasWidgetCustomization,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Apply to Widgets Button (Android only - iOS WidgetKit controls appearance)
            if (getPlatform().type.isAndroid) {
                item {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasUnappliedWidgetChanges && hasWidgetCustomization) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            if (hasWidgetCustomization) Icons.Default.Palette else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (hasUnappliedWidgetChanges && hasWidgetCustomization) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            },
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (hasWidgetCustomization) {
                                "Widget Appearance Changes"
                            } else {
                                "Premium Feature"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (hasWidgetCustomization) {
                                if (hasUnappliedWidgetChanges) {
                                    "You have unapplied changes. Tap below to update your widgets."
                                } else {
                                    "Your widgets are up to date."
                                }
                            } else {
                                "Upgrade to premium to customize widget appearance and apply changes."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (hasWidgetCustomization) {
                                    viewModel.applyWidgetChanges()
                                } else {
                                    navController.navigate(SupportUs)
                                }
                            },
                            enabled = if (hasWidgetCustomization) hasUnappliedWidgetChanges else true,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                if (hasWidgetCustomization) Icons.Default.Check else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (hasWidgetCustomization) {
                                    if (hasUnappliedWidgetChanges) {
                                        "Apply to Widgets"
                                    } else {
                                        "Already Applied"
                                    }
                                } else {
                                    "Upgrade to Premium"
                                }
                            )
                        }
                    }
                }
            }
            } // end isAndroid gate for widget customization controls

            // Reset Button (Show for all, but disable for non-premium)
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        if (hasCustomTheme) {
                            viewModel.resetToDefaults()
                        } else {
                            navController.navigate(SupportUs)
                        }
                    },
                    enabled = hasCustomTheme,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    if (!hasCustomTheme) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (hasCustomTheme) "Reset to Defaults" else "Reset to Defaults (Premium)")
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSettingRow(
    selected: ThemeOption,
    onSelected: (ThemeOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Theme") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ThemeOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PremiumFeaturePrompt(
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Palette,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Premium Feature",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Customize your app's theme with custom colors and palette styles",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onUpgradeClick) {
            Text("Upgrade to Premium")
        }
    }
}

@Composable
private fun ColorPalette(
    selectedColor: Color?,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = listOf(
        Primary,
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF673AB7), // Deep Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF2196F3), // Blue
        Color(0xFF00BCD4), // Cyan
        Color(0xFF009688), // Teal
        Color(0xFF4CAF50), // Green
        Color(0xFF8BC34A), // Light Green
        Color(0xFFCDDC39), // Lime
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFFFC107), // Amber
        Color(0xFFFF9800), // Orange
        Color(0xFFFF5722), // Deep Orange
        Color(0xFFF44336), // Red
    )

    Column(modifier = modifier) {
        colors.chunked(8).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { color ->
                    ColorCircle(
                        color = color,
                        isSelected = selectedColor == color,
                        onClick = { onColorSelected(color) },
                        modifier = Modifier.weight(1f),
                        enabled = enabled
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color.copy(alpha = if (enabled) 1f else 0.4f))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .then(
                if (enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PaletteStyleItem(
    name: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (enabled) 1f else 0.4f)
            else
                MaterialTheme.colorScheme.surface.copy(alpha = if (enabled) 1f else 0.6f)
        ),
        border = if (isSelected)
            BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.4f)
            )
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            val titleTextColor = if (enabled) {
                if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }

            val secondaryTextColor = if (enabled) {
                if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryFixedVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = titleTextColor,
                    maxLines = 1
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor,
                    maxLines = 1
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun getPaletteStyleDescription(style: String): String = when (style) {
    "TonalSpot" -> "Balanced and harmonious, default style"
    "Neutral" -> "Subtle and muted tones"
    "Vibrant" -> "Bold and saturated colors"
    "Expressive" -> "Creative and dynamic palette"
    "Rainbow" -> "Playful and colorful"
    "FruitSalad" -> "Fresh and varied hues"
    "Monochrome" -> "Single hue variations"
    "Fidelity" -> "True to your selected color"
    "Content" -> "Adaptive to content"
    "SchemeContent" -> "Content-based color scheme"
    else -> "Custom palette style"
}

class ThemeCustomizationViewModel(
    private val themePreferences: ThemePreferences,
    private val appPreferences: AppPreferences,
    private val widgetPreferences: WidgetPreferences,
    private val subscriptionRepository: SubscriptionRepository,
    val temporaryPremiumAccess: TemporaryPremiumAccess  // Make it public val instead of private
) : ViewModel() {
    private val log = logger()

    private val _hasCustomTheme = MutableStateFlow(false)
    val hasCustomTheme: StateFlow<Boolean> = _hasCustomTheme.asStateFlow()

    private val _hasWidgetCustomization = MutableStateFlow(false)
    val hasWidgetCustomization: StateFlow<Boolean> = _hasWidgetCustomization.asStateFlow()

    private val _hasPermanentPremium = MutableStateFlow(false)
    val hasPermanentPremium: StateFlow<Boolean> = _hasPermanentPremium.asStateFlow()

    private val _selectedTheme = MutableStateFlow(ThemeOption.System)
    val selectedTheme: StateFlow<ThemeOption> = _selectedTheme.asStateFlow()

    private val _selectedPrimaryColor = MutableStateFlow<Color?>(null)
    val selectedPrimaryColor: StateFlow<Color?> = _selectedPrimaryColor.asStateFlow()

    private val _selectedPaletteStyle = MutableStateFlow<String?>(null)
    val selectedPaletteStyle: StateFlow<String?> = _selectedPaletteStyle.asStateFlow()

    private val _applyToWidgets = MutableStateFlow(false)
    val applyToWidgets: StateFlow<Boolean> = _applyToWidgets.asStateFlow()

    // Widget appearance preferences
    private val _widgetThemeSource = MutableStateFlow(WidgetThemeSource.FOLLOW_APP_THEME)
    val widgetThemeSource: StateFlow<WidgetThemeSource> = _widgetThemeSource.asStateFlow()

    private val _widgetBackgroundAlpha = MutableStateFlow(0.95f)
    val widgetBackgroundAlpha: StateFlow<Float> = _widgetBackgroundAlpha.asStateFlow()

    private val _widgetCornerRadius = MutableStateFlow(16)
    val widgetCornerRadius: StateFlow<Int> = _widgetCornerRadius.asStateFlow()

    // Track unapplied widget changes
    private val _hasUnappliedWidgetChanges = MutableStateFlow(false)
    val hasUnappliedWidgetChanges: StateFlow<Boolean> = _hasUnappliedWidgetChanges.asStateFlow()

    // Event counter to trigger widget updates (increments each time apply is clicked)
    private val _widgetApplyTrigger = MutableStateFlow(0)
    val widgetApplyTrigger: StateFlow<Int> = _widgetApplyTrigger.asStateFlow()

    // Last applied values to track changes
    private var lastAppliedThemeSource: WidgetThemeSource? = null
    private var lastAppliedAlpha: Float? = null
    private var lastAppliedRadius: Int? = null

    init {
        viewModelScope.launch {
            // Combine subscription state and temporary access changes
            kotlinx.coroutines.flow.combine(
                subscriptionRepository.state,
                temporaryPremiumAccess.accessChangeTrigger
            ) { subscriptionState, _ ->
                val isPremium = subscriptionState.isSubscribed && !subscriptionState.isExpired()
                // IMPORTANT: Call subscriptionRepository.hasFeature() instead of state.hasFeature()
                // This ensures widget access cache gets updated properly and includes temporary access
                val hasCustomTheme = subscriptionRepository.hasFeature(PremiumFeature.CUSTOM_THEMES)
                val hasWidgetCustomization =
                    subscriptionRepository.hasFeature(PremiumFeature.ADVANCED_WIDGETS)
                log.d { "State changed - isSubscribed=${subscriptionState.isSubscribed}, isExpired=${subscriptionState.isExpired()}, isPremium=$isPremium, hasCustomTheme=$hasCustomTheme, hasWidgetCustomization=$hasWidgetCustomization" }
                _hasCustomTheme.value = hasCustomTheme
                _hasWidgetCustomization.value = hasWidgetCustomization
                _hasPermanentPremium.value =
                    isPremium  // Set permanent premium status based on subscription
            }.collect { }
        }

        viewModelScope.launch {
            // Load theme selection
            appPreferences.themeFlow.collect { theme ->
                _selectedTheme.value = theme
            }
        }

        viewModelScope.launch {
            // Load saved preferences
            themePreferences.customPrimaryColorFlow.collect { color ->
                _selectedPrimaryColor.value = color?.let { Color(it.toULong()) }
            }
        }

        viewModelScope.launch {
            themePreferences.paletteStyleFlow.collect { style ->
                _selectedPaletteStyle.value = style
            }
        }

        viewModelScope.launch {
            themePreferences.applyToWidgetsFlow.collect { apply ->
                _applyToWidgets.value = apply
            }
        }

        // Load widget preferences ONCE on init, don't continuously collect
        // This prevents DataStore updates from overwriting local state while user is editing
        viewModelScope.launch {
            val source = widgetPreferences.widgetThemeSourceFlow.first()
            _widgetThemeSource.value = source
            lastAppliedThemeSource = source
        }

        viewModelScope.launch {
            val alpha = widgetPreferences.widgetBackgroundAlphaFlow.first()
            _widgetBackgroundAlpha.value = alpha
            lastAppliedAlpha = alpha
        }

        viewModelScope.launch {
            val radius = widgetPreferences.widgetCornerRadiusFlow.first()
            _widgetCornerRadius.value = radius
            lastAppliedRadius = radius
        }
    }

    fun checkForUnappliedChanges() {
        val hasChanges =
            _widgetThemeSource.value != lastAppliedThemeSource ||
                    _widgetBackgroundAlpha.value != lastAppliedAlpha ||
                    _widgetCornerRadius.value != lastAppliedRadius

        if (_hasUnappliedWidgetChanges.value != hasChanges) {
            log.d { "checkForUnappliedChanges() - changing from ${_hasUnappliedWidgetChanges.value} to $hasChanges" }
        }

        _hasUnappliedWidgetChanges.value = hasChanges
    }

    fun updateTheme(theme: ThemeOption) {
        viewModelScope.launch {
            _selectedTheme.value = theme
            appPreferences.updateTheme(theme)
        }
    }

    fun updatePrimaryColor(color: Color) {
        viewModelScope.launch {
            _selectedPrimaryColor.value = color
            themePreferences.updateCustomPrimaryColor(color)
        }
    }

    fun updatePaletteStyle(style: String) {
        viewModelScope.launch {
            _selectedPaletteStyle.value = style
            themePreferences.updatePaletteStyle(style)
        }
    }

    fun updateWidgetThemeSource(source: WidgetThemeSource) {
        _widgetThemeSource.value = source
        checkForUnappliedChanges()
    }

    fun updateWidgetBackgroundAlpha(alpha: Float) {
        _widgetBackgroundAlpha.value = alpha
        checkForUnappliedChanges()
    }

    fun updateWidgetCornerRadius(radius: Int) {
        _widgetCornerRadius.value = radius
        checkForUnappliedChanges()
    }

    fun applyWidgetChanges() {
        viewModelScope.launch {
            log.d { "applyWidgetChanges() called - Theme Source: ${_widgetThemeSource.value}, Alpha: ${_widgetBackgroundAlpha.value}, Radius: ${_widgetCornerRadius.value}" }

            try {
                // Save to DataStore - these are suspend functions, so they execute sequentially
                // and this coroutine waits for each to complete before continuing
                log.v { "Writing theme source to DataStore..." }
                widgetPreferences.updateWidgetThemeSource(_widgetThemeSource.value)
                log.v { "Theme source written" }

                log.v { "Writing background alpha to DataStore..." }
                widgetPreferences.updateWidgetBackgroundAlpha(_widgetBackgroundAlpha.value)
                log.v { "Background alpha written" }

                log.v { "Writing corner radius to DataStore..." }
                widgetPreferences.updateWidgetCornerRadius(_widgetCornerRadius.value)
                log.v { "Corner radius written" }

                log.i { "All DataStore writes completed successfully" }

                // Update last applied values AFTER DataStore writes complete
                lastAppliedThemeSource = _widgetThemeSource.value
                lastAppliedAlpha = _widgetBackgroundAlpha.value
                lastAppliedRadius = _widgetCornerRadius.value

                log.v { "Updated last applied values" }

                // Clear unapplied changes flag
                log.v { "Setting hasUnappliedWidgetChanges = false" }
                _hasUnappliedWidgetChanges.value = false

                // Trigger widget update by incrementing counter
                // This happens AFTER all DataStore writes are confirmed complete
                val newTriggerValue = _widgetApplyTrigger.value + 1
                log.d { "Incrementing widget apply trigger: ${_widgetApplyTrigger.value} -> $newTriggerValue" }
                _widgetApplyTrigger.value = newTriggerValue
                log.i { "Widget apply trigger emitted successfully" }
            } catch (e: Exception) {
                log.e(e) { "ERROR in applyWidgetChanges(): ${e.message}" }
                e.printStackTrace()
                // Reset unapplied changes flag even on error to prevent stuck state
                _hasUnappliedWidgetChanges.value = false
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            themePreferences.resetToDefaults()
            widgetPreferences.resetToDefaults()
            _selectedPrimaryColor.value = null
            _selectedPaletteStyle.value = null
        }
    }
}

@Composable
private fun WidgetThemeSourceSelector(
    selectedSource: WidgetThemeSource,
    onSourceSelected: (WidgetThemeSource) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Follow App Theme
        ThemeSourceOption(
            title = "Follow App Theme",
            description = "Use your custom app theme colors in widgets",
            icon = "🎨",
            isSelected = selectedSource == WidgetThemeSource.FOLLOW_APP_THEME,
            onClick = { onSourceSelected(WidgetThemeSource.FOLLOW_APP_THEME) },
            enabled = enabled
        )

        Spacer(Modifier.height(8.dp))

        // Follow System Theme
        ThemeSourceOption(
            title = "Follow System Theme",
            description = "Auto light/dark based on system settings",
            icon = "⚙️",
            isSelected = selectedSource == WidgetThemeSource.FOLLOW_SYSTEM,
            onClick = { onSourceSelected(WidgetThemeSource.FOLLOW_SYSTEM) },
            enabled = enabled
        )

        Spacer(Modifier.height(8.dp))

        // Dynamic Colors (Material You)
        ThemeSourceOption(
            title = "Dynamic Colors",
            description = "Match wallpaper colors (Android 12+)",
            icon = "🌈",
            isSelected = selectedSource == WidgetThemeSource.DYNAMIC_COLORS,
            onClick = { onSourceSelected(WidgetThemeSource.DYNAMIC_COLORS) },
            enabled = enabled
        )
    }
}

@Composable
private fun ThemeSourceOption(
    title: String,
    description: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected && enabled) {
            MaterialTheme.colorScheme.primaryContainer
        } else if (enabled) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        border = if (isSelected && enabled) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        onClick = { if (enabled) onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                modifier = Modifier.size(40.dp)
            )

            Spacer(Modifier.width(16.dp))

            // Title and Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected && enabled) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected && enabled) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }

            // Checkmark or Lock
            if (isSelected && enabled) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else if (!enabled) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Premium Feature",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}