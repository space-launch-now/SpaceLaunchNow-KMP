package me.calebjones.spacelaunchnow

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.di.koinConfig
import me.calebjones.spacelaunchnow.ui.layout.desktop.TabletDesktopLayout
import me.calebjones.spacelaunchnow.ui.layout.phone.PhoneLayout
import org.koin.compose.KoinApplication



@Composable
fun isTabletOrDesktop(): Boolean {
    val screenWidthDp = getScreenWidth()
    val isLargeScreen = screenWidthDp >= 600.dp // Example threshold for tablets
    return isLargeScreen
//    return getPlatform().name == "Desktop"
}


@Composable
fun SpaceLaunchNowApp() {
    KoinApplication(
        application = { koinConfig() }
    ){
        val isTabletOrDesktop = isTabletOrDesktop()
        if (isTabletOrDesktop) {
            TabletDesktopLayout()
        } else {
            PhoneLayout()
        }
    }
}