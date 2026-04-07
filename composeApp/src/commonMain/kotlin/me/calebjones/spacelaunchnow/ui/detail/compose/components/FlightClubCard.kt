package me.calebjones.spacelaunchnow.ui.detail.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ExternalLinkAlt
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.flightclub

// Flight Club brand colors
private val FlightClubBackground = Color(0xFF203956)
private val FlightClubLogo = Color(0xFFF5B65B)

/**
 * A card that displays a link to Flight Club for flight trajectory analysis.
 *
 * @param flightClubUrl The URL to the Flight Club page for this launch
 * @param modifier Optional modifier for the card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightClubCard(
    flightClubUrl: String,
    onReferralTracked: (url: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val urlWithReferrer = if (flightClubUrl.contains("?")) {
        "$flightClubUrl&ref=spacelaunchnow"
    } else {
        "$flightClubUrl?ref=spacelaunchnow"
    }

    Card(
        onClick = {
            onReferralTracked(urlWithReferrer)
            uriHandler.openUri(urlWithReferrer)
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FlightClubBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(FlightClubBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flight Club logo
                    Image(
                        painter = painterResource(Res.drawable.flightclub),
                        contentDescription = "Flight Club",
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Text content
                    Text(
                        text = "View Telemetry on Flight Club",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // External link icon
                Icon(
                    imageVector = FontAwesomeIcons.Solid.ExternalLinkAlt,
                    contentDescription = "Open external link",
                    tint = FlightClubLogo,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// region Previews

@Preview
@Composable
private fun FlightClubCardPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            FlightClubCard(
                flightClubUrl = "https://flightclub.io/result/3d?llId=abc123",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
