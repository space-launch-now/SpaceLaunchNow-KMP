package me.calebjones.spacelaunchnow.util.logging

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import me.calebjones.spacelaunchnow.data.repository.PermissionHelper

actual fun checkPlayServicesAvailability(): PlayServicesAvailability = try {
    when (GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(PermissionHelper.context)) {
        ConnectionResult.SUCCESS -> PlayServicesAvailability.AVAILABLE
        ConnectionResult.SERVICE_MISSING,
        ConnectionResult.SERVICE_DISABLED,
        ConnectionResult.SERVICE_INVALID -> PlayServicesAvailability.MISSING
        ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
        ConnectionResult.SERVICE_UPDATING -> PlayServicesAvailability.UPDATE_REQUIRED
        else -> PlayServicesAvailability.UNKNOWN
    }
} catch (e: Exception) {
    PlayServicesAvailability.UNKNOWN
}
