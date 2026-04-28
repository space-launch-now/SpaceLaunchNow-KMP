package me.calebjones.spacelaunchnow.ui.agencies.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.Country
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyListView(
    agencies: List<Agency>,
    isLoading: Boolean,
    error: String?,
    onAgencyClick: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agencies") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                error != null -> {
                    ErrorContent(
                        errorMessage = error,
                        onRetry = onRetry
                    )
                }

                isLoading && agencies.isEmpty() -> {
                    LoadingContent()
                }

                agencies.isEmpty() -> {
                    EmptyContent()
                }

                else -> {
                    AgencyList(
                        agencies = agencies,
                        onAgencyClick = onAgencyClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AgencyList(
    agencies: List<Agency>,
    onAgencyClick: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(agencies) { agency ->
            AgencyListItem(
                agency = agency,
                onClick = { onAgencyClick(agency.id) }
            )
        }
    }
}

@Composable
fun AgencyListItem(
    agency: Agency,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Agency logo
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val image = agency.socialLogoUrl ?: agency.logoUrl

                image?.let { logoUrl ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    ) {
                        AsyncImage(
                            model = logoUrl,
                            contentDescription = "Agency logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                        )
                    }
                } ?: run {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Agency info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = agency.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                agency.typeName?.let { type ->
                    Text(
                        text = type,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                // Push chips to bottom
                if (agency.countries.isNotEmpty()) {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Country chips
                if (agency.countries.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),

                    ) {
                        items(agency.countries) { country ->
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = country.name ?: "Unknown",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error loading agencies",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Business,
            contentDescription = "No agencies",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No agencies found",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AgencyListItemPreview() {
    MaterialTheme {
        val sampleAgency = Agency(
            id = 1,
            name = "SpaceX",
            abbrev = "SpX",
            typeName = "Commercial",
            countries = listOf(
                Country(
                    id = 1,
                    name = "USA",
                    alpha2Code = "US",
                    alpha3Code = "USA",
                    nationalityName = "American",
                    nationalityNameComposed = "American"
                )
            ),
            imageUrl = null,
            logoUrl = null,
            socialLogoUrl = null,
            description = null,
            administrator = null,
            foundingYear = null,
            featured = true
        )

        AgencyListItem(
            agency = sampleAgency,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AgencyListItemMultiNationalPreview() {
    MaterialTheme {
        val sampleAgency = Agency(
            id = 2,
            name = "European Space Agency",
            abbrev = "ESA",
            typeName = "Multinational",
            countries = listOf(
                Country(
                    id = 2,
                    name = "France",
                    alpha2Code = "FR",
                    alpha3Code = "FRA",
                    nationalityName = null,
                    nationalityNameComposed = null
                ),
                Country(
                    id = 3,
                    name = "Germany",
                    alpha2Code = "DE",
                    alpha3Code = "DEU",
                    nationalityName = null,
                    nationalityNameComposed = null
                ),
                Country(
                    id = 4,
                    name = "Italy",
                    alpha2Code = "IT",
                    alpha3Code = "ITA",
                    nationalityName = null,
                    nationalityNameComposed = null
                )
            ),
            imageUrl = null,
            logoUrl = null,
            socialLogoUrl = "https://thespacedevs-prod.nyc3.digitaloceanspaces.com/media/images/china2520national2520space2520administration_nation_20190602114400.png",
            description = null,
            administrator = null,
            foundingYear = null,
            featured = true
        )

        AgencyListItem(
            agency = sampleAgency,
            onClick = {}
        )
    }
}
