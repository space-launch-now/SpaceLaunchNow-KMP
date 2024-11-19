package me.calebjones.spacelaunchnow.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.calebjones.spacelaunchnow.ui.compose.LaunchListView
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun HomeScreen() {
    Scaffold(
        topBar = { HomeTopBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Search Section
            item { SearchBar() }

            // Upcoming Launches Section
            item { SectionTitle(title = "Upcoming Launches", hasAction = true) }
            item { UpcomingHorizontalScrollableList() }

            // This Week in Spaceflight
            item { SectionTitle(title = "This Week in Spaceflight", hasAction = true) }
            item { HorizontalScrollableList() }

            // Latest Updates
            item { SectionTitle(title = "Latest Updates", hasAction = true) }
            item { HorizontalScrollableList() }

            // News and Events
            item { SectionTitle(title = "News and Events", hasAction = false) }
            items(2) { NewsItem() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    TopAppBar(
        title = { Text(text = "Home", fontWeight = FontWeight.Bold, fontSize = 50.sp) },
        actions = {
            IconButton(onClick = { /* Handle click */ }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
            }
            IconButton(onClick = { /* Handle click */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More Options")
            }
        }
    )
}

@Composable
fun SearchBar() {
    val searchQuery = remember { TextFieldValue("") }
    BasicTextField(
        value = searchQuery,
        onValueChange = { /* Handle text change */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 8.dp),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (searchQuery.text.isEmpty()) {
                    Text(text = "Search", color = Color.Gray)
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun SectionTitle(title: String, hasAction: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        if (hasAction) {
            TextButton(onClick = { /* Handle action click */ }) {
                Text(text = "See All", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

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
fun UpcomingHorizontalScrollableList() {
    val launchViewModel = koinViewModel<LaunchViewModel>()

    LaunchListView(launchViewModel)
}

@Composable
fun PlaceholderCard() {
    Card(
        modifier = Modifier.size(150.dp, 150.dp),
        shape = MaterialTheme.shapes.medium
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
        shape = MaterialTheme.shapes.large
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
fun NewsItem() {
    val launchViewModel = koinViewModel<LaunchViewModel>()

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = Color.LightGray
        ) { }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Headline", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Description goes here in a short sentence.", fontSize = 14.sp, color = Color.Gray)
        }
    }
}