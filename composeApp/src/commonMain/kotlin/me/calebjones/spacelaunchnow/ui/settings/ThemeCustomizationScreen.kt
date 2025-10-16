package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import me.calebjones.spacelaunchnow.data.preferences.WidgetPreferences
import me.calebjones.spacelaunchnow.data.preferences.WidgetThemeSource
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.data.storage.ThemePreferences
import me.calebjones.spacelaunchnow.getPlatform
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
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
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
                        text = "Theme Customization",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!isPremium) {
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Selection (FREE - Available to all users)
            item {
                Text(
                    "Appearance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Choose between light, dark, or system theme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                ThemeSettingRow(
                    selected = selectedTheme,
                    onSelected = viewModel::updateTheme
                )
            }

            // Premium Features Section
            item {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
            }

            // Premium Prompt or Premium Features
            if (!isPremium) {
                item {
                    PremiumPromptCard(
                        onUpgradeClick = { /* Navigate to premium upgrade */ }
                    )
                }
            }

            // Color Customization (PREMIUM - Disabled for free users)
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Primary Color",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isPremium) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Premium",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (!isPremium) {
                    Text(
                        "Available with Premium",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                ColorPalette(
                    selectedColor = selectedPrimaryColor,
                    onColorSelected = if (isPremium) {
                        { viewModel.updatePrimaryColor(it) }
                    } else {
                        { /* Disabled */ }
                    },
                    enabled = isPremium
                )
            }

            // Palette Style Selection (PREMIUM - Disabled for free users)
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Palette Style",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Choose how colors are generated from your primary color",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!isPremium) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Premium",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            items(ThemePreferences.AVAILABLE_PALETTE_STYLES) { style ->
                PaletteStyleItem(
                    name = style,
                    description = getPaletteStyleDescription(style),
                    isSelected = selectedPaletteStyle == style,
                    onClick = if (isPremium) {
                        { viewModel.updatePaletteStyle(style) }
                    } else {
                        { /* Disabled */ }
                    },
                    enabled = isPremium
                )
            }

            // Widget Appearance Customization (PREMIUM only)
            if (isPremium) {
                item {
                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    Text(
                        "Widget Appearance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Customize the look of your home screen widgets",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Widget Theme Source Selection (Android only - iOS doesn't support Material 3 dynamic colors)
                if (getPlatform().name.contains("Android")) {
                    item {
                        Text(
                            "Widget Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Choose where widget colors come from",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    item {
                        val widgetThemeSource by viewModel.widgetThemeSource.collectAsStateWithLifecycle()
                        WidgetThemeSourceSelector(
                            selectedSource = widgetThemeSource,
                            onSourceSelected = { viewModel.updateWidgetThemeSource(it) }
                        )
                    }
                }

                // Widget Background Transparency
                item {
                    Spacer(Modifier.height(16.dp))
                    val widgetBackgroundAlpha by viewModel.widgetBackgroundAlpha.collectAsStateWithLifecycle()
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Background Transparency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${(widgetBackgroundAlpha * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "0% = Fully transparent, 100% = Fully opaque",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = widgetBackgroundAlpha,
                            onValueChange = { viewModel.updateWidgetBackgroundAlpha(it) },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Widget Corner Radius
                item {
                    Spacer(Modifier.height(16.dp))
                    val widgetCornerRadius by viewModel.widgetCornerRadius.collectAsStateWithLifecycle()
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Corner Radius",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${widgetCornerRadius}dp",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "Rounded corners for widget background (0-40dp, increments of 4dp)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = widgetCornerRadius.toFloat(),
                            onValueChange = {
                                // Round to nearest multiple of 4
                                val roundedValue = ((it / 4f).toInt() * 4).coerceIn(0, 40)
                                viewModel.updateWidgetCornerRadius(roundedValue)
                            },
                            valueRange = 0f..40f,
                            steps = 9, // 0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40 = 11 positions, so 9 steps between them
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Apply to Widgets Button (PREMIUM only)
            if (isPremium) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasUnappliedWidgetChanges) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
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
                                Icons.Default.Palette,
                                contentDescription = null,
                                tint = if (hasUnappliedWidgetChanges) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Widget Appearance Changes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (hasUnappliedWidgetChanges) {
                                    "You have unapplied changes. Tap below to update your widgets."
                                } else {
                                    "Your widgets are up to date."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.applyWidgetChanges() },
                                enabled = hasUnappliedWidgetChanges,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (hasUnappliedWidgetChanges) {
                                        "Apply to Widgets"
                                    } else {
                                        "Already Applied"
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Reset Button (PREMIUM only)
            if (isPremium) {
                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.resetToDefaults() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset to Defaults")
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumPromptCard(
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Palette,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Unlock Premium Customization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Customize colors and palette styles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Button(
            onClick = onUpgradeClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Text("Upgrade to Premium")
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
                    color = titleTextColor
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor
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
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

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
            // Check premium status
            subscriptionRepository.state.collect { state ->
                val isPremium = state.isSubscribed && !state.isExpired()
                println("ThemeCustomizationViewModel: Subscription state changed - isSubscribed=${state.isSubscribed}, isExpired=${state.isExpired()}, isPremium=$isPremium")
                _isPremium.value = isPremium
            }
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

    private fun checkForUnappliedChanges() {
        val hasChanges =
            _widgetThemeSource.value != lastAppliedThemeSource ||
                    _widgetBackgroundAlpha.value != lastAppliedAlpha ||
                    _widgetCornerRadius.value != lastAppliedRadius

        if (_hasUnappliedWidgetChanges.value != hasChanges) {
            println("ThemeCustomizationViewModel: checkForUnappliedChanges() - changing from ${_hasUnappliedWidgetChanges.value} to $hasChanges")
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
            println("ThemeCustomizationViewModel: applyWidgetChanges() called")
            println("  Theme Source: ${_widgetThemeSource.value}")
            println("  Alpha: ${_widgetBackgroundAlpha.value}")
            println("  Radius: ${_widgetCornerRadius.value}")

            try {
                // Save to DataStore - these are suspend functions, so they execute sequentially
                // and this coroutine waits for each to complete before continuing
                println("  Writing theme source to DataStore...")
                widgetPreferences.updateWidgetThemeSource(_widgetThemeSource.value)
                println("  Theme source written")

                println("  Writing background alpha to DataStore...")
                widgetPreferences.updateWidgetBackgroundAlpha(_widgetBackgroundAlpha.value)
                println("  Background alpha written")

                println("  Writing corner radius to DataStore...")
                widgetPreferences.updateWidgetCornerRadius(_widgetCornerRadius.value)
                println("  Corner radius written")

                println("  All DataStore writes completed successfully")

                // Update last applied values AFTER DataStore writes complete
                lastAppliedThemeSource = _widgetThemeSource.value
                lastAppliedAlpha = _widgetBackgroundAlpha.value
                lastAppliedRadius = _widgetCornerRadius.value

                println("  Updated last applied values")

                // Clear unapplied changes flag
                println("  Setting hasUnappliedWidgetChanges = false")
                _hasUnappliedWidgetChanges.value = false

                // Trigger widget update by incrementing counter
                // This happens AFTER all DataStore writes are confirmed complete
                val newTriggerValue = _widgetApplyTrigger.value + 1
                println("  Incrementing widget apply trigger: ${_widgetApplyTrigger.value} -> $newTriggerValue")
                _widgetApplyTrigger.value = newTriggerValue
                println("  Widget apply trigger emitted successfully")
            } catch (e: Exception) {
                println("ThemeCustomizationViewModel: ERROR in applyWidgetChanges(): ${e.message}")
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Follow App Theme
        ThemeSourceOption(
            title = "Follow App Theme",
            description = "Use your custom app theme colors in widgets",
            icon = "🎨",
            isSelected = selectedSource == WidgetThemeSource.FOLLOW_APP_THEME,
            onClick = { onSourceSelected(WidgetThemeSource.FOLLOW_APP_THEME) }
        )

        Spacer(Modifier.height(8.dp))

        // Follow System Theme
        ThemeSourceOption(
            title = "Follow System Theme",
            description = "Auto light/dark based on system settings",
            icon = "⚙️",
            isSelected = selectedSource == WidgetThemeSource.FOLLOW_SYSTEM,
            onClick = { onSourceSelected(WidgetThemeSource.FOLLOW_SYSTEM) }
        )

        Spacer(Modifier.height(8.dp))

        // Dynamic Colors (Material You)
        ThemeSourceOption(
            title = "Dynamic Colors",
            description = "Match wallpaper colors (Android 12+)",
            icon = "🌈",
            isSelected = selectedSource == WidgetThemeSource.DYNAMIC_COLORS,
            onClick = { onSourceSelected(WidgetThemeSource.DYNAMIC_COLORS) }
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        onClick = onClick
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
                modifier = Modifier.size(40.dp)
            )

            Spacer(Modifier.width(16.dp))

            // Title and Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Checkmark
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
