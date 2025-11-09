package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.brands.WikipediaW
import compose.icons.fontawesomeicons.solid.InfoCircle
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyType
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Country
import me.calebjones.spacelaunchnow.ui.components.CountryChip
import me.calebjones.spacelaunchnow.ui.components.InfoTile
import me.calebjones.spacelaunchnow.ui.components.InfoTileData
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Reusable Agency Details Card for displaying comprehensive agency information
 * Used in both Launch and Event detail views
 *
 * @param agency The detailed agency data to display
 * @param openUrl Callback function to handle URL clicks
 */
@Composable
fun AgencyDetailsCard(
    agency: AgencyDetailed,
    openUrl: (String) -> Unit = { /* TODO: Implement for platform */ }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Agency logo - centered
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 200.dp),
            ) {
                SubcomposeAsyncImage(
                    model = agency.logo?.imageUrl ?: "",
                    contentDescription = "Agency logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "Agency",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                )
            }

            // Agency name
            Text(
                text = agency.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            // Grid of key details (above countries)
            val infoTiles = buildList {
                agency.type?.name?.takeIf { it.isNotBlank() }?.let {
                    add(InfoTileData(Icons.Filled.Business, "Type", it))
                }
                agency.foundingYear?.let { year ->
                    add(InfoTileData(Icons.Filled.CalendarToday, "Founded", year.toString()))
                }
                agency.administrator?.takeIf { it.isNotBlank() }?.let { admin ->
                    add(InfoTileData(Icons.Filled.Person, "Administrator", admin))
                }
                if (agency.country.isNotEmpty() && agency.country.size == 1) {
                    add(
                        InfoTileData(
                            icon = Icons.Filled.Flag,
                            label = "Country",
                            value = null,
                            customComposable = { CountryChip(agency.country.first()) }
                        ))
                }
            }
            if (infoTiles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    infoTiles.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { tile ->
                                InfoTile(
                                    icon = tile.icon,
                                    label = tile.label,
                                    value = tile.value,
                                    customComposable = tile.customComposable,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Countries as chips (moved below grid)
            if (agency.country.isNotEmpty() && agency.country.size > 1) {
                CountryInfoRow(countries = agency.country)
            }
            // Agency description
            agency.description?.let { description ->
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }


            // Info & Wiki links
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                agency.infoUrl?.let { url ->
                    Button(
                        onClick = { openUrl(url) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.InfoCircle,
                            contentDescription = "Information",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Website")
                    }
                }
                agency.wikiUrl?.let { url ->
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

@Preview
@Composable
private fun AgencyDetailsCardPreview() {
    MaterialTheme {
        Surface {
            AgencyDetailsCard(
                agency = AgencyDetailed(
                    id = 44,
                    url = "https://ll.thespacedevs.com/2.2.0/agencies/44/",
                    name = "National Aeronautics and Space Administration",
                    featured = true,
                    type = AgencyType(id = 1, name = "Government"),
                    abbrev = "NASA",
                    description = "The National Aeronautics and Space Administration is an independent agency of the executive branch of the United States federal government responsible for the civilian space program, as well as aeronautics and aerospace research.",
                    administrator = "Bill Nelson",
                    foundingYear = 1958,
                    launchers = "Space Shuttle, SLS",
                    spacecraft = "Orion, Dragon",
                    parent = null,
                    image = null,
                    logo = null,
                    socialLogo = null,
                    country = listOf(
                        Country(
                            id = 1,
                            name = "United States",
                            alpha2Code = "US",
                            alpha3Code = "USA",
                        )
                    ),
                    totalLaunchCount = 1500,
                    consecutiveSuccessfulLaunches = 50,
                    successfulLaunches = 1400,
                    failedLaunches = 100,
                    pendingLaunches = 20,
                    consecutiveSuccessfulLandings = 30,
                    successfulLandings = 200,
                    failedLandings = 10,
                    attemptedLandings = 210,
                    infoUrl = "https://www.nasa.gov",
                    responseMode = "list",
                    successfulLandingsSpacecraft = 1,
                    failedLandingsSpacecraft = 2,
                    attemptedLandingsSpacecraft = 3,
                    successfulLandingsPayload = 4,
                    failedLandingsPayload = 5,
                    attemptedLandingsPayload = 6,
                    wikiUrl = "",
                    socialMediaLinks = emptyList(),
                )
            )
        }
    }
}
