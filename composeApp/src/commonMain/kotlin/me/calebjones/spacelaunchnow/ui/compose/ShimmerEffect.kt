package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer


/**
 * Shimmering box that mimics the size and shape of a NextUpView
 * Using compose-shimmer library
 */
@Composable
fun NextUpShimmerBox() {
    // Create shimmer effect

    Column(
        modifier = Modifier
            .shimmer()
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {


        // Countdown card shimmer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .clip(RoundedCornerShape(24.dp)),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            )
        }
    }
}

@Composable
fun LaunchListShimmer(cardCount: Int = 5) {
    // Create shimmer effect for a row of n items using LazyRow
    LazyRow(
        modifier = Modifier
            .shimmer()
            .fillMaxWidth()
            .height(240.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false // Disable scrolling
    ) {
        // Generate n card shimmers using items
        items(cardCount) { _ ->
            Card(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(340.dp)
                    .clip(RoundedCornerShape(16.dp)),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun UpdatesShimmer(cardCount: Int = 5) {
    // Create shimmer effect for a row of n update items using LazyRow
    LazyRow(
        modifier = Modifier
            .shimmer()
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false // Disable scrolling
    ) {
        // Generate n card shimmers using items
        items(cardCount) { _ ->
            Card(
                modifier = Modifier
                    .height(280.dp)
                    .width(150.dp)
                    .clip(RoundedCornerShape(16.dp)),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun PlainShimmerCard(
    height: Int = 280,
    width: Int? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(height.dp)
            .let { if (width != null) it.width(width.dp) else it.fillMaxWidth() }
            .shimmer()
            .clip(RoundedCornerShape(16.dp)),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        )
    }
}

// ========== Starship Dashboard Shimmers ==========

/**
 * Shimmer loading state for the Starship Overview tab.
 * Shows placeholders for program info, livestream, next launch, and updates.
 */
@Composable
fun StarshipOverviewShimmer() {
    Column(
        modifier = Modifier
            .shimmer()
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Program info card shimmer
        PlainShimmerCard(height = 180)
        
        // Livestream/video placeholder shimmer
        PlainShimmerCard(height = 200)
        
        // Next launch card shimmer
        PlainShimmerCard(height = 160)
        
        // Updates section shimmer (horizontal scroll)
        UpdatesShimmer(cardCount = 3)
    }
}

/**
 * Shimmer loading state for the Starship Events tab.
 * Shows placeholders for events list, updates, and news.
 */
@Composable
fun StarshipEventsShimmer() {
    Column(
        modifier = Modifier
            .shimmer()
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Events list shimmer - vertical list of cards
        repeat(4) {
            PlainShimmerCard(height = 100)
        }
        
        // Updates section
        PlainShimmerCard(height = 24, width = 100) // Section header
        UpdatesShimmer(cardCount = 3)
        
        // News section  
        PlainShimmerCard(height = 24, width = 80) // Section header
        LaunchListShimmer(cardCount = 2)
    }
}

/**
 * Shimmer loading state for the Starship Vehicles tab.
 * Shows placeholders for spacecraft grid.
 */
@Composable
fun StarshipVehiclesShimmer() {
    Column(
        modifier = Modifier
            .shimmer()
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Grid-like layout for vehicles (2 columns simulated)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlainShimmerCard(
                height = 200,
                modifier = Modifier.weight(1f)
            )
            PlainShimmerCard(
                height = 200,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlainShimmerCard(
                height = 200,
                modifier = Modifier.weight(1f)
            )
            PlainShimmerCard(
                height = 200,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlainShimmerCard(
                height = 200,
                modifier = Modifier.weight(1f)
            )
            PlainShimmerCard(
                height = 200,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
