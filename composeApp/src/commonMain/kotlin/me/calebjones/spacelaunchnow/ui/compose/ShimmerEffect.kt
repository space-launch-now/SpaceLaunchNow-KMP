package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

// ========== ISS/Space Station Shimmers ==========

/**
 * Shimmer loading state for ISS Map with position info card.
 * Shows placeholder for map and position data.
 */
@Composable
fun IssMapShimmer(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.shimmer()) {
            // Map section shimmer
            PlainShimmerCard(height = 250, modifier = Modifier.fillMaxWidth())
            
            // Position info section shimmer
            Column(modifier = Modifier.padding(16.dp)) {
                // Title
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                
                androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
                
                // Lat/Long row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
                
                androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                
                // Altitude/Velocity row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shimmer loading state for Expedition Info Card.
 * Shows placeholder for mission patch, name, dates, and crew list.
 */
@Composable
fun ExpeditionInfoShimmer(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .shimmer()
                .padding(16.dp)
        ) {
            // Mission patch shimmer (centered circle)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            
            // Expedition name
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            
            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
            
            // Date range row
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
                
                androidx.compose.foundation.layout.Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}

/**
 * Shimmer loading state for Station Specs Card.
 * Shows placeholder for statistics.
 */
@Composable
fun StationSpecsShimmer(modifier: Modifier = Modifier) {
    Column(modifier = Modifier.shimmer()) {
        // Title
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        
        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Crew/Vehicles row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
                
                androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
                
                // Dimensions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shimmer loading state for Owner Agencies Card.
 * Shows placeholder for agency list with logos.
 */
@Composable
fun OwnerAgenciesShimmer(modifier: Modifier = Modifier) {
    Column(modifier = Modifier.shimmer()) {
        // Title
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        
        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        
        // Agency items
        repeat(2) {
            Card(
                modifier = modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Agency logo circle
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    
                    androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * Shimmer loading state for Docking Locations Card.
 * Shows placeholder for docked vehicles.
 */
@Composable
fun DockingLocationsShimmer(modifier: Modifier = Modifier) {
    Column(modifier = Modifier.shimmer()) {
        // Title
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        
        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        
        // Docked vehicle items
        repeat(2) {
            Card(
                modifier = modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vehicle image
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    
                    androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * Shimmer loading state for Station Reports/News Card.
 * Shows placeholder for news articles list.
 */
@Composable
fun StationReportsShimmer(modifier: Modifier = Modifier) {
    Column(modifier = Modifier.shimmer()) {
        // Title
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        
        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                repeat(3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        // Article image
                        Box(
                            modifier = Modifier
                                .size(100.dp, 70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        
                        androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    }
                }
            }
        }
    }
}
