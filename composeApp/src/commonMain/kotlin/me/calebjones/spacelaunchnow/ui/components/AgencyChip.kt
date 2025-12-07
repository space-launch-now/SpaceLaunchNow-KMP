package me.calebjones.spacelaunchnow.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyMini

@Composable
fun AgencyChip(agency: AgencyMini) {
    val agencyName =
        if (agency.name.length > 32 && agency.abbrev?.isNotBlank() == true) agency.abbrev else agency.name

    val chipColor = when (agency.type?.name?.lowercase()) {
        "government" -> MaterialTheme.colorScheme.tertiaryContainer
        "commercial" -> MaterialTheme.colorScheme.primaryContainer
        "educational" -> MaterialTheme.colorScheme.secondaryContainer
        "multinational" -> MaterialTheme.colorScheme.primaryFixed
        "private" -> MaterialTheme.colorScheme.primaryFixedDim
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = chipColor,
        modifier = Modifier.clip(RoundedCornerShape(16.dp))
    ) {
        Text(
            text = agencyName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}
