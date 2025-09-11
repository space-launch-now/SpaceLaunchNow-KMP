package me.calebjones.spacelaunchnow.ui.theme

import androidx.compose.runtime.Composable

expect fun initPlatformSpecific(colorExtractor: ImageColorExtractor)

expect class ImageColorExtractor
