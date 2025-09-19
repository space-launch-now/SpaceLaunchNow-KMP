package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.api.launchlibrary.models.*
import me.calebjones.spacelaunchnow.util.LaunchFormatUtil
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import me.calebjones.spacelaunchnow.util.StatusColorUtil

/**
 * Common interface to extract launch data needed for the card header
 */
sealed interface LaunchCardData {
    val name: String?
    val status: LaunchStatus?
    val net: Instant?
    val image: Image?
    val locationName: String?
    val agencyLogoUrl: String?
    
    fun getFormattedTitle(): String
}

/**
 * Wrapper for LaunchBasic
 */
data class BasicLaunchCardData(val launch: LaunchBasic) : LaunchCardData {
    override val name: String? = launch.name
    override val status: LaunchStatus? = launch.status
    override val net: Instant? = launch.net
    override val image: Image? = launch.image
    override val locationName: String? = null // LaunchBasic doesn't have pad/location
    override val agencyLogoUrl: String? = null // LaunchBasic doesnt have a agencyLogo
    
    override fun getFormattedTitle(): String = LaunchFormatUtil.formatLaunchTitle(launch)
}

/**
 * Wrapper for LaunchNormal
 */
data class NormalLaunchCardData(val launch: LaunchNormal) : LaunchCardData {
    override val name: String? = launch.name
    override val status: LaunchStatus? = launch.status
    override val net: Instant? = launch.net
    override val image: Image? = launch.image
    override val locationName: String? = launch.pad?.location?.name
    override val agencyLogoUrl: String? = launch.launchServiceProvider.socialLogo?.imageUrl 
        ?: launch.launchServiceProvider.logo?.imageUrl
    
    override fun getFormattedTitle(): String = LaunchFormatUtil.formatLaunchTitle(launch)
}

/**
 * Wrapper for LaunchDetailed
 */
data class DetailedLaunchCardData(val launch: LaunchDetailed) : LaunchCardData {
    override val name: String? = launch.name
    override val status: LaunchStatus? = launch.status
    override val net: Instant? = launch.net
    override val image: Image? = launch.image
    override val locationName: String? = launch.pad?.location?.name
    override val agencyLogoUrl: String? = launch.launchServiceProvider.socialLogo?.imageUrl 
        ?: launch.launchServiceProvider.logo?.imageUrl
    
    override fun getFormattedTitle(): String = LaunchFormatUtil.formatLaunchTitle(launch)
}

/**
 * Common composable for the top portion of launch cards that handles all launch types
 * 
 * @param launchData The launch data wrapped in LaunchCardData interface
 * @param showAgencyLogo Whether to show the circular agency logo on the left
 * @param logoSize Size of the agency logo
 * @param contentPadding Padding around the content
 */
@Composable
fun LaunchCardHeader(
    launchData: LaunchCardData,
    showAgencyLogo: Boolean = true,
    logoSize: androidx.compose.ui.unit.Dp = 56.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    modifier: Modifier = Modifier
) {
    // Compute formatted values
    val title by remember(launchData) {
        mutableStateOf(launchData.getFormattedTitle())
    }
    
    val formattedDate by remember(launchData.net) {
        mutableStateOf(
            launchData.net?.let { DateTimeUtil.formatLaunchDateTime(it) } ?: "TBD"
        )
    }

    Row(
        modifier = modifier.padding(contentPadding)
    ) {
        // Circular Agency Logo (if enabled and available)
        if (showAgencyLogo && launchData.agencyLogoUrl != null) {
            AsyncImage(
                model = launchData.agencyLogoUrl,
                contentDescription = "Agency Logo",
                modifier = Modifier
                    .size(logoSize)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        CircleShape
                    ),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
        }

        // Launch Information Column
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Standardized Launch Title (LSP | Rocket Configuration)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Launch Location (if available)
            launchData.locationName?.let { locationName ->
                Text(
                    text = locationName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Human Readable Date
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Specialized version of LaunchCardHeader for overlay on images with drop shadows
 * Used primarily in NextUpView where text is overlaid on background images
 * 
 * @param launchData The launch data wrapped in LaunchCardData interface
 * @param showAgencyLogo Whether to show the circular agency logo on the left
 * @param logoSize Size of the agency logo
 * @param useRelativeTime Whether to use relative time formatting (e.g., "in 2 days") vs absolute time
 * @param contentPadding Padding around the content
 */
@Composable
fun LaunchCardHeaderOverlay(
    launchData: LaunchCardData,
    showAgencyLogo: Boolean = true,
    logoSize: androidx.compose.ui.unit.Dp = 56.dp,
    useRelativeTime: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    modifier: Modifier = Modifier
) {
    // Compute formatted values
    val title by remember(launchData) {
        mutableStateOf(launchData.getFormattedTitle())
    }
    
    val formattedDate by remember(launchData.net, useRelativeTime) {
        mutableStateOf(
            launchData.net?.let { 
                if (useRelativeTime) {
                    DateTimeUtil.formatLaunchDateTimeRelative(it)
                } else {
                    DateTimeUtil.formatLaunchDateTime(it)
                }
            } ?: "TBD"
        )
    }

    Row(
        modifier = modifier.padding(contentPadding),
        verticalAlignment = Alignment.Top,
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Agency Logo (if enabled and available) with drop shadow effect
            if (showAgencyLogo && launchData.agencyLogoUrl != null) {
                AsyncImage(
                    model = launchData.agencyLogoUrl,
                    contentDescription = "Agency Logo",
                    modifier = Modifier
                        .size(logoSize)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            StatusColorUtil.getLaunchStatusColor(launchData.status?.id),
                            CircleShape
                        ),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))
            }

            // Launch Information Column with drop shadows
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Standardized Launch Title with drop shadow for overlay visibility
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f),
                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                            blurRadius = 5f
                        )
                    ),
                    color = androidx.compose.ui.graphics.Color.White
                )

                // Launch Location (if available) with drop shadow
                launchData.locationName?.let { locationName ->
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f),
                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                blurRadius = 2f
                            )
                        ),
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                    )
                }

                // Human Readable Date with drop shadow
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f),
                            offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                            blurRadius = 2f
                        )
                    ),
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Extension functions to easily convert launch objects to LaunchCardData
 */
fun LaunchBasic.toLaunchCardData(): LaunchCardData = BasicLaunchCardData(this)
fun LaunchNormal.toLaunchCardData(): LaunchCardData = NormalLaunchCardData(this)
fun LaunchDetailed.toLaunchCardData(): LaunchCardData = DetailedLaunchCardData(this)
