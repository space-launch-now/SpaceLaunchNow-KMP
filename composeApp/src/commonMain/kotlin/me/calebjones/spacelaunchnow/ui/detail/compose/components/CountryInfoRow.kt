package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.domain.model.Country
import me.calebjones.spacelaunchnow.ui.components.CountryChip
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Country info row with chips displaying a list of countries
 *
 * @param countries List of countries to display
 */
@Composable
fun CountryInfoRow(countries: List<Country>) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header row with icon and label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Flag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = if (countries.size == 1) "Country" else "Countries",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // List of countries below the header
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(countries) { country ->
                CountryChip(country = country)
            }
        }
    }
}

@Preview
@Composable
private fun CountryInfoRowPreview() {
    MaterialTheme {
        Surface {
            CountryInfoRow(
                countries = listOf(
                    Country(
                        id = 1,
                        name = "United States",
                        alpha2Code = "US",
                        alpha3Code = "USA",
                        nationalityName = "American",
                        nationalityNameComposed = "American"
                    ),
                    Country(
                        id = 2,
                        name = "Russia",
                        alpha2Code = "RU",
                        alpha3Code = "RUS",
                        nationalityName = "Russian",
                        nationalityNameComposed = "Russian"
                    ),
                    Country(
                        id = 3,
                        name = "France",
                        alpha2Code = "FR",
                        alpha3Code = "FRA",
                        nationalityName = "French",
                        nationalityNameComposed = "French"
                    )
                )
            )
        }
    }
}
