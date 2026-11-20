package me.calebjones.spacelaunchnow.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.Discord
import compose.icons.fontawesomeicons.brands.Github
import me.calebjones.spacelaunchnow.util.BuildConfig
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.launcher
import spacelaunchnow_kmp.composeapp.generated.resources.about_header_app_name
import spacelaunchnow_kmp.composeapp.generated.resources.about_header_logo_description
import spacelaunchnow_kmp.composeapp.generated.resources.about_header_version
import spacelaunchnow_kmp.composeapp.generated.resources.about_libraries_heading
import spacelaunchnow_kmp.composeapp.generated.resources.about_link_changelog
import spacelaunchnow_kmp.composeapp.generated.resources.about_link_discord
import spacelaunchnow_kmp.composeapp.generated.resources.about_link_github
import spacelaunchnow_kmp.composeapp.generated.resources.about_sln_description
import spacelaunchnow_kmp.composeapp.generated.resources.about_sln_link
import spacelaunchnow_kmp.composeapp.generated.resources.about_sln_title
import spacelaunchnow_kmp.composeapp.generated.resources.about_title
import spacelaunchnow_kmp.composeapp.generated.resources.about_tsd_description
import spacelaunchnow_kmp.composeapp.generated.resources.about_tsd_link
import spacelaunchnow_kmp.composeapp.generated.resources.about_tsd_title
import spacelaunchnow_kmp.composeapp.generated.resources.common_back

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutLibrariesScreen(onNavigateBack: (() -> Unit)? = null) {
    var libsJson by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        libsJson = Res.readBytes("files/aboutlibraries.json").decodeToString()
    }
    libsJson?.let { json ->
        val libs = Libs.Builder().withJson(json).build()
        Scaffold(
            topBar = {
                // We use TopAppBar from accompanist-insets-ui which allows us to provide
                // content padding matching the system bars insets.
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.about_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        )
                    },
                    navigationIcon = {
                        if (onNavigateBack != null) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = stringResource(Res.string.common_back)
                                )
                            }
                        }
                    }
                )
            }
        ) { contentPadding ->
            LibrariesContainer(
                libraries = libs,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(contentPadding),
                showAuthor = true,
                showDescription = true,
                showVersion = true,
                showLicenseBadges = true,
                header = {
                    item {
                        AboutHeader()
                    }

                    item {
                        // About Space Launch Now Section
                        AboutSection(
                            title = stringResource(Res.string.about_sln_title),
                            description = stringResource(Res.string.about_sln_description),
                            linkText = stringResource(Res.string.about_sln_link),
                            linkUrl = "https://spacelaunchnow.app/about/"
                        )
                    }

                    item {
                        // About The Space Devs Section
                        AboutSection(
                            title = stringResource(Res.string.about_tsd_title),
                            description = stringResource(Res.string.about_tsd_description),
                            linkText = stringResource(Res.string.about_tsd_link),
                            linkUrl = "https://thespacedevs.com/about"
                        )
                    }

                    item {
                        Text(
                            stringResource(Res.string.about_libraries_heading),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun AboutHeader(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .border(4.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                .background(Color(0xFF1565C0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val painter = painterResource(Res.drawable.launcher)
            Image(
                painter = painter,
                contentDescription = stringResource(Res.string.about_header_logo_description),
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Inside
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(Res.string.about_header_app_name))
        Text(stringResource(Res.string.about_header_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { uriHandler.openUri("https://github.com/ItsCalebJones/SpaceLaunchNow-KMP") }) {
                    Icon(
                        imageVector = FontAwesomeIcons.Brands.Github,
                        contentDescription = stringResource(Res.string.about_link_github),
                        modifier = Modifier.size(40.dp).padding(4.dp)
                    )
                }
                Text(stringResource(Res.string.about_link_github), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { uriHandler.openUri("https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/releases") }) {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = stringResource(Res.string.about_link_changelog),
                        modifier = Modifier.size(40.dp).padding(4.dp)
                    )
                }
                Text(stringResource(Res.string.about_link_changelog), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { uriHandler.openUri("https://discord.gg/WVfzEDW") }) {
                    Icon(
                        imageVector = FontAwesomeIcons.Brands.Discord,
                        contentDescription = stringResource(Res.string.about_link_discord),
                        modifier = Modifier.size(40.dp).padding(4.dp)
                    )
                }
                Text(stringResource(Res.string.about_link_discord), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun AboutSection(
    title: String,
    description: String,
    linkText: String,
    linkUrl: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.material3.TextButton(
            onClick = { uriHandler.openUri(linkUrl) }
        ) {
            Text(linkText)
        }
    }
}

@Preview
@Composable
private fun AboutSectionPreview() {
    SpaceLaunchNowPreviewTheme {
        AboutSection(
            title = stringResource(Res.string.about_sln_title),
            description = stringResource(Res.string.about_sln_description),
            linkText = stringResource(Res.string.about_sln_link),
            linkUrl = "https://spacelaunchnow.app/about/"
        )
    }
}

@Preview
@Composable
private fun AboutSectionDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        AboutSection(
            title = stringResource(Res.string.about_tsd_title),
            description = stringResource(Res.string.about_tsd_description),
            linkText = stringResource(Res.string.about_tsd_link),
            linkUrl = "https://thespacedevs.com/about"
        )
    }
}
