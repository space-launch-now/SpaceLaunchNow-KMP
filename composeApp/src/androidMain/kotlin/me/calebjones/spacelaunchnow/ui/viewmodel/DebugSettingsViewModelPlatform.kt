package me.calebjones.spacelaunchnow.ui.viewmodel

import android.content.Context
import androidx.core.content.edit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun resetNotificationPermissionAskedFlag() {
    val context = object : KoinComponent {
        val context: Context by inject()
    }.context

    context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
        .edit { putBoolean("hasAskedForNotificationPermission", false) }
}