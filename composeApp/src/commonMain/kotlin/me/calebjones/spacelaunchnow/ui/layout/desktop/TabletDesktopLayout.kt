package me.calebjones.spacelaunchnow.ui.layout.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.ui.home.HomeScreen
import me.calebjones.spacelaunchnow.ui.other.OtherScreen
import me.calebjones.spacelaunchnow.ui.settings.SettingsScreen
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme

@Composable
fun TabletDesktopLayout() {
    SpaceLaunchNowTheme {
        var selectedItem by remember { mutableIntStateOf(0) }
        val items = listOf("Home", "Other", "Settings")
        val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.List, Icons.Filled.Settings)
        val unselectedIcons = listOf(Icons.Filled.Home, Icons.Filled.List, Icons.Filled.Settings)

        Scaffold {
            Row(Modifier.fillMaxSize()) {
                // Sidebar Navigation using NavigationRail
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    NavigationRail(
                        header = {
                            Icon(
                                Icons.Filled.Home,
                                contentDescription = "Home",
                                modifier = Modifier.absolutePadding(left = 8.dp, right = 8.dp, bottom = 32.dp, top = 16.dp)
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ){
                        items.forEachIndexed { index, item ->
                            NavigationRailItem(
                                modifier = Modifier.padding(8.dp),
                                icon = {
                                    Icon(
                                        if (selectedItem == index) selectedIcons[index] else unselectedIcons[index],
                                        contentDescription = item
                                    )
                                },
                                label = { Text(item, color = MaterialTheme.colorScheme.onSurface) },
                                selected = selectedItem == index,
                                onClick = { selectedItem = index }
                            )
                        }
                    }
                }
                Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
                // Main Content
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column (
                        modifier = Modifier.fillMaxSize()
                    )
                    {
                        when (selectedItem) {
                            0 -> HomeScreen()
                            1 -> OtherScreen()
                            2 -> SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}