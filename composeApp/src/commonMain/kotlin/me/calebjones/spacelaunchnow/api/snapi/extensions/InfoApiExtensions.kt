package me.calebjones.spacelaunchnow.api.snapi.extensions

import me.calebjones.spacelaunchnow.api.snapi.apis.InfoApi
import me.calebjones.spacelaunchnow.api.snapi.models.Info

/**
 * Extension functions for InfoApi to provide clean interfaces
 */

/**
 * Get SNAPI service info including the list of available news sites
 */
suspend fun InfoApi.getInfo(): Info {
    return infoRetrieve().body()
}

/**
 * Get the list of available news sites for filtering
 */
suspend fun InfoApi.getNewsSites(): List<String> {
    return getInfo().newsSites
}
