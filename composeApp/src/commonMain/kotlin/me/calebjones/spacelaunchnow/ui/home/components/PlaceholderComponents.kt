package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.ui.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HorizontalScrollableList() {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(5) { PlaceholderCard() }
    }
}

@Composable
fun UpcomingHorizontalScrollableList(navController: androidx.navigation.NavController) {
    val homeViewModel = koinViewModel<HomeViewModel>()
    LaunchListView(viewModel = homeViewModel, navController = navController)
}

@Composable
fun PlaceholderCard() {
    Card(
        modifier = Modifier.size(150.dp, 150.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Content", color = Color.Gray)
        }
    }
}

@Composable
fun LargePlaceholderCard() {
    Card(
        modifier = Modifier.size(360.dp, 240.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Content", color = Color.Gray)
        }
    }
}
