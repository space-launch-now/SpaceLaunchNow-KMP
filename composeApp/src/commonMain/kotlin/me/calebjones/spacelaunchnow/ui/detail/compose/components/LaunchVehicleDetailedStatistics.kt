package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.WikipediaW
import kotlinx.coroutines.delay
import me.calebjones.spacelaunchnow.domain.model.RocketConfig
import me.calebjones.spacelaunchnow.ui.components.InfoTile
import me.calebjones.spacelaunchnow.ui.components.SpecCard
import me.calebjones.spacelaunchnow.ui.components.StatCard
import me.calebjones.spacelaunchnow.ui.components.StatusChip
import me.calebjones.spacelaunchnow.ui.icons.CustomIcons
import me.calebjones.spacelaunchnow.ui.icons.RocketLaunch
import me.calebjones.spacelaunchnow.util.NumberFormatUtil
import me.calebjones.spacelaunchnow.util.parseIsoDurationToHumanReadable

@Composable
fun LaunchVehicleDetailsCard(
    rocketConfig: RocketConfig,
    openUrl: (String) -> Unit = { /* TODO: Implement for platform */ }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Rocket thumbnail image on the left
            rocketConfig.imageUrl?.let { imageUrl ->
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = "Rocket image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .shimmer(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CustomIcons.RocketLaunch,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CustomIcons.RocketLaunch,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    )
                }
            }

            // Rocket details on the right
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = rocketConfig.fullName ?: "Unknown Rocket",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                // Status indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (rocketConfig.active == true) {
                        StatusChip(text = "Active", color = Color(0xFF4CAF50))
                    } else {
                        StatusChip(text = "Inactive", color = MaterialTheme.colorScheme.error)
                    }

                    if (rocketConfig.reusable == true) {
                        StatusChip(text = "Reusable", color = Color(0xFF2196F3))
                    } else {
                        StatusChip(text = "Expendable", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Description (overflow-aware)
            rocketConfig.description?.takeIf { it.isNotBlank() }?.let { desc ->
                var expanded by remember { mutableStateOf(false) }
                var hasOverflow by remember { mutableStateOf(false) }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (expanded) Int.MAX_VALUE else 5,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                        onTextLayout = { result ->
                            if (!expanded) hasOverflow = result.hasVisualOverflow
                        }
                    )
                    if (hasOverflow || expanded) {
                        TextButton(onClick = { expanded = !expanded }) {
                            Text(if (expanded) "Show less" else "Show more")
                        }
                    }
                }
            }

            // Build info tiles and render as two-column grid
            val infoTiles = buildList {
                rocketConfig.manufacturer?.name?.takeIf { it.isNotBlank() }?.let {
                    add(Triple(Icons.Filled.Factory, "Manufacturer", it))
                }
                rocketConfig.variant?.takeIf { it.isNotBlank() }?.let {
                    add(Triple(Icons.Filled.Badge, "Variant", it))
                }
                rocketConfig.alias?.takeIf { it.isNotBlank() }?.let {
                    add(Triple(Icons.Filled.Label, "Alias", it))
                }
                if (!rocketConfig.families.isNullOrEmpty()) {
                    val familyNames = rocketConfig.families.joinToString(", ") { it.name }
                    if (familyNames.isNotBlank()) {
                        add(
                            Triple(
                                Icons.Filled.Category,
                                if (rocketConfig.families.size == 1) "Family" else "Families",
                                familyNames
                            )
                        )
                    }
                }
                val stages = listOfNotNull(rocketConfig.minStage, rocketConfig.maxStage)
                if (stages.isNotEmpty()) {
                    val stageText =
                        if (rocketConfig.minStage != null && rocketConfig.maxStage != null && rocketConfig.minStage != rocketConfig.maxStage) "${rocketConfig.minStage}-${rocketConfig.maxStage}" else (rocketConfig.minStage
                            ?: rocketConfig.maxStage).toString()
                    add(Triple(Icons.Filled.Stairs, "Stages", stageText))
                }
                rocketConfig.maidenFlight?.let {
                    add(
                        Triple(
                            Icons.Filled.CalendarMonth,
                            "Maiden Flight",
                            it.toString()
                        )
                    )
                }
                rocketConfig.fastestTurnaround?.takeIf { it.isNotBlank() }
                    ?.let {
                        add(
                            Triple(
                                Icons.Filled.Timelapse,
                                "Fastest Turnaround",
                                parseIsoDurationToHumanReadable(it)
                            )
                        )
                    }
            }
            if (infoTiles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    infoTiles.chunked(2).forEachIndexed { rowIndex, row ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(rowIndex * 150L)
                            visible = true
                        }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(300)) + slideInVertically(
                                animationSpec = tween(300),
                                initialOffsetY = { it / 3 }
                            )
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { (icon, label, value) ->
                                    InfoTile(
                                        icon = icon,
                                        label = label,
                                        value = value,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    Box {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            var specificationsExpanded by remember { mutableStateOf(false) }

                            // Specifications section header with expand/collapse
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { specificationsExpanded = !specificationsExpanded }
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Specifications",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = if (specificationsExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = if (specificationsExpanded) "Collapse" else "Expand",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Build all specification items in a single list
                            val allSpecs = buildList {
                                rocketConfig.length?.let { length ->
                                    add(Triple("Length", "${length}m", Icons.Filled.Height))
                                }
                                rocketConfig.diameter?.let { diameter ->
                                    add(Triple("Diameter", "${diameter}m", Icons.Filled.ViewColumn))
                                }
                                rocketConfig.launchMass?.let { mass ->
                                    add(
                                        Triple(
                                            "Launch Mass",
                                            NumberFormatUtil.formatNumberWithUnit(mass, "t"),
                                            Icons.Filled.Scale
                                        )
                                    )
                                }
                                rocketConfig.launchCost?.let { cost ->
                                    // Dollar on the front for USD
                                    add(
                                        Triple(
                                            "Launch Cost",
                                            "$" + NumberFormatUtil.formatNumberWithUnit(cost, ""),
                                            Icons.Filled.AttachMoney
                                        )
                                    )
                                }
                                rocketConfig.leoCapacity?.let { leo ->
                                    add(
                                        Triple(
                                            "LEO",
                                            NumberFormatUtil.formatNumberWithUnit(leo, "kg"),
                                            Icons.Filled.Public
                                        )
                                    )
                                }
                                rocketConfig.gtoCapacity?.let { gto ->
                                    add(
                                        Triple(
                                            "GTO",
                                            NumberFormatUtil.formatNumberWithUnit(gto, "kg"),
                                            Icons.Filled.Satellite
                                        )
                                    )
                                }
                                rocketConfig.geoCapacity?.let { geo ->
                                    add(
                                        Triple(
                                            "GEO",
                                            NumberFormatUtil.formatNumberWithUnit(geo, "kg"),
                                            Icons.Filled.SatelliteAlt
                                        )
                                    )
                                }
                                rocketConfig.ssoCapacity?.let { sso ->
                                    add(
                                        Triple(
                                            "SSO",
                                            NumberFormatUtil.formatNumberWithUnit(sso, "kg"),
                                            Icons.AutoMirrored.Filled.AltRoute
                                        )
                                    )
                                }
                                rocketConfig.toThrust?.let { thrust ->
                                    add(Triple("Thrust", "$thrust to", Icons.Filled.Speed))
                                }
                                rocketConfig.apogee?.let { apogee ->
                                    add(
                                        Triple(
                                            "Apogee",
                                            NumberFormatUtil.formatNumberWithUnit(apogee, "km"),
                                            Icons.AutoMirrored.Filled.TrendingUp
                                        )
                                    )
                                }
                            }

                            // Display specifications in a 3-column grid with animation
                            AnimatedVisibility(
                                visible = specificationsExpanded,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 4 }),
                                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 4 })
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (allSpecs.isNotEmpty()) {
                                        allSpecs.chunked(3).forEach { rowSpecs ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                rowSpecs.forEach { (label, value, icon) ->
                                                    SpecCard(
                                                        label = label,
                                                        value = value,
                                                        icon = icon,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                // Fill remaining slots with spacers if needed
                                                repeat(3 - rowSpecs.size) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                            if (rowSpecs != allSpecs.chunked(3).last()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Links (Info / Wiki)
            if (!rocketConfig.infoUrl.isNullOrBlank() || !rocketConfig.wikiUrl.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rocketConfig.infoUrl?.let { url ->
                        Button(
                            onClick = { openUrl(url) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Information",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Website")
                        }
                    }
                    rocketConfig.wikiUrl?.let { url ->
                        Button(
                            onClick = { openUrl(url) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Brands.WikipediaW,
                                contentDescription = "Wikipedia",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wikipedia")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LaunchVehicleDetailedStatistics(rocketConfig: RocketConfig) {

    Column(
//        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Launch Statistics
        val totalLaunches = rocketConfig.totalLaunchCount ?: 0
        val successfulLaunches = rocketConfig.successfulLaunches ?: 0
        val failedLaunches = rocketConfig.failedLaunches ?: 0
        val pendingLaunches = rocketConfig.pendingLaunches ?: 0
        val consecutiveSuccessful = rocketConfig.consecutiveSuccessfulLaunches ?: 0
        if (totalLaunches > 0) {
            Text(
                text = "Launch Vehicle Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            val successRate = (successfulLaunches * 100.0 / totalLaunches)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.TrendingUp,
                    value = "${successRate.toInt()}%",
                    label = "Success\nRate",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.BarChart,
                    value = "$totalLaunches",
                    label = "Total\nLaunches",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.CheckCircle,
                    value = "$successfulLaunches",
                    label = "Successful\nLaunches",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Cancel,
                    value = "$failedLaunches",
                    label = "Failed\nLaunches",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pendingLaunches > 0) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Pending,
                        value = "$pendingLaunches",
                        label = "Pending\nLaunches",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                if (consecutiveSuccessful > 0) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.ThumbUp,
                        value = "$consecutiveSuccessful",
                        label = "Consecutive\nSuccess",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Landing Statistics
        val attemptedLandings = rocketConfig.attemptedLandings ?: 0
        val successfulLandings = rocketConfig.successfulLandings ?: 0
        val failedLandings = rocketConfig.failedLandings ?: 0
        val consecutiveSuccessfulLandings = rocketConfig.consecutiveSuccessfulLandings ?: 0
        if (attemptedLandings + successfulLandings + failedLandings + consecutiveSuccessfulLandings > 0) {
            Text(
                text = "Landing Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (attemptedLandings > 0) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Assessment,
                        value = "$attemptedLandings",
                        label = "Attempted\nLandings",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                if (successfulLandings > 0) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Check,
                        value = "$successfulLandings",
                        label = "Successful\nLandings",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (failedLandings > 0) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Close,
                        value = "$failedLandings",
                        label = "Failed\nLandings",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                if (consecutiveSuccessfulLandings > 0) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.ThumbUp,
                        value = "$consecutiveSuccessfulLandings",
                        label = "Consecutive\nSuccess",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}