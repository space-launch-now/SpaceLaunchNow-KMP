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
import org.jetbrains.compose.ui.tooling.preview.Preview
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.launcher

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
                            text = "About",
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
                                    contentDescription = "Back"
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
                            title = "About Space Launch Now",
                            description = "Space Launch Now started as a weekend project in 2016 and has grown into the best space launch schedule for keeping up to date on rocket launches from around the world. Get updates and notifications for launches from SpaceX, NASA, ULA, Roscosmos, ISRO, and many more! ",
                            linkText = "Learn More",
                            linkUrl = "https://spacelaunchnow.app/about/"
                        )
                    }

                    item {
                        // About The Space Devs Section
                        AboutSection(
                            title = "About The Space Devs",
                            description = "The Space Devs is an alliance of spaceflight enthusiast developers and librarians united with the shared objective of providing free access to spaceflight data. Our REST APIs span the world of spaceflight—from rocket launches to events and news.\n\nAs Space Launch Now's developer, I am a Board Member and Proud Supporter of The Space Devs!",
                            linkText = "Visit The Space Devs",
                            linkUrl = "https://thespacedevs.com/about"
                        )
                    }

                    item {
                        Text(
                            "Open Source Libraries",
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
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Inside
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Space Launch Now")
        Text("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { uriHandler.openUri("https://github.com/ItsCalebJones/SpaceLaunchNow-KMP") }) {
                    Icon(
                        imageVector = FontAwesomeIcons.Brands.Github,
                        contentDescription = "GitHub",
                        modifier = Modifier.size(40.dp).padding(4.dp)
                    )
                }
                Text("GitHub", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { uriHandler.openUri("https://github.com/ItsCalebJones/SpaceLaunchNow-KMP/releases") }) {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = "Changelog",
                        modifier = Modifier.size(40.dp).padding(4.dp)
                    )
                }
                Text("Changelog", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.size(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { uriHandler.openUri("https://discord.gg/WVfzEDW") }) {
                    Icon(
                        imageVector = FontAwesomeIcons.Brands.Discord,
                        contentDescription = "Discord",
                        modifier = Modifier.size(40.dp).padding(4.dp)
                    )
                }
                Text("Discord", fontSize = 12.sp)
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
            title = "About Space Launch Now",
            description = "Space Launch Now started as a weekend project in 2016 and has grown into the best space launch schedule for keeping up to date on rocket launches from around the world.",
            linkText = "Learn More",
            linkUrl = "https://spacelaunchnow.app/about/"
        )
    }
}

@Preview
@Composable
private fun AboutSectionDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        AboutSection(
            title = "About The Space Devs",
            description = "The Space Devs is an alliance of spaceflight enthusiast developers and librarians united with the shared objective of providing free access to spaceflight data.",
            linkText = "Visit The Space Devs",
            linkUrl = "https://thespacedevs.com/about"
        )
    }
}
