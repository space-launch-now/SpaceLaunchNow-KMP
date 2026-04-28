package me.calebjones.spacelaunchnow.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.domain.model.Country
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Country chip with flag image
 *
 * @param country The country data containing name and alpha2Code for flag
 */
@Composable
fun CountryChip(country: Country) {
    val label = country.name ?: country.alpha2Code ?: "Unknown"
    val flagUrl = country.alpha2Code?.let { code ->
        "https://flagcdn.com/w40/${code.lowercase()}.png"
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        },
        leadingIcon = flagUrl?.let { url ->
            {
                AsyncImage(
                    model = url,
                    contentDescription = "Flag of ${country.name}",
                    modifier = Modifier
                        .width(18.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

@Preview
@Composable
private fun CountryChipPreview() {
    MaterialTheme {
        Surface {
            CountryChip(
                country = Country(
                    id = 1,
                    name = "United States",
                    alpha2Code = "US",
                    alpha3Code = "USA",
                    nationalityName = "American",
                    nationalityNameComposed = "American"
                )
            )
        }
    }
}
