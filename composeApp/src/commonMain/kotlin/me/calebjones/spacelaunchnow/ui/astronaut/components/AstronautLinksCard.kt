package me.calebjones.spacelaunchnow.ui.astronaut.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.LocalDate
import me.calebjones.spacelaunchnow.domain.model.AstronautDetail
import me.calebjones.spacelaunchnow.domain.model.SocialMediaLink
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Card displaying astronaut links including Wikipedia and social media accounts.
 */
@Composable
fun AstronautLinksCard(
    astronaut: AstronautDetail,
    modifier: Modifier = Modifier
) {
    val hasWiki = !astronaut.wikiUrl.isNullOrBlank()
    val hasSocialMedia = !astronaut.socialMediaLinks.isNullOrEmpty()

    if (!hasWiki && !hasSocialMedia) return

    val uriHandler = LocalUriHandler.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Links",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Wikipedia Link
            if (hasWiki) {
                LinkItem(
                    icon = Icons.Default.Language,
                    label = "Wikipedia",
                    url = astronaut.wikiUrl!!,
                    onClick = { uriHandler.openUri(astronaut.wikiUrl!!) }
                )
            }

            // Social Media Links
            if (hasSocialMedia) {
                if (hasWiki) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
                
                astronaut.socialMediaLinks.forEach { link ->
                    link.url?.let { url ->
                        val platformName = link.platformName ?: "Social Media"
                        val logoUrl = link.platformLogoUrl
                        
                        SocialMediaLinkItem(
                            platformName = platformName,
                            url = url,
                            logoUrl = logoUrl,
                            onClick = { uriHandler.openUri(url) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    url: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "Open link",
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SocialMediaLinkItem(
    platformName: String,
    url: String,
    logoUrl: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (logoUrl != null) {
            AsyncImage(
                model = logoUrl,
                contentDescription = "$platformName logo",
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = platformName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Icon(
            imageVector = Icons.Default.OpenInNew,
            contentDescription = "Open link",
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Preview
@Composable
private fun AstronautLinksCardPreview() {
    SpaceLaunchNowPreviewTheme {
        AstronautLinksCard(
            astronaut = AstronautDetail(
                id = 1,
                name = "Neil Armstrong",
                statusName = "Deceased",
                statusId = 11,
                agencyName = "NASA",
                agencyAbbrev = "NASA",
                agencyId = 44,
                imageUrl = null,
                thumbnailUrl = null,
                age = 82,
                bio = "First person to walk on the Moon",
                typeName = "Government",
                nationality = emptyList(),
                dateOfBirth = LocalDate.parse("1930-08-05"),
                dateOfDeath = LocalDate.parse("2012-08-25"),
                wikiUrl = "https://en.wikipedia.org/wiki/Neil_Armstrong",
                socialMediaLinks = listOf(
                    SocialMediaLink(
                        id = 1,
                        url = "https://twitter.com/neilarmstrong",
                        platformName = "Twitter",
                        platformLogoUrl = "https://example.com/twitter.png"
                    ),
                    SocialMediaLink(
                        id = 2,
                        url = "https://instagram.com/neilarmstrong",
                        platformName = "Instagram",
                        platformLogoUrl = null
                    )
                ),
                inSpace = false,
                timeInSpace = null,
                evaTime = null,
                lastFlight = null,
                firstFlight = null,
                flightsCount = 2,
                landingsCount = 2,
                spacewalksCount = 1,
                flights = emptyList(),
                landings = emptyList(),
                spacewalks = emptyList()
            )
        )
    }
}
