package me.calebjones.spacelaunchnow.data.remote

import me.calebjones.spacelaunchnow.api.client.infrastructure.ApiClient
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

class LaunchLibraryAPIClient (
    baseUrl: String = "",
    apiKey: String = ""
) : KoinComponent {

    private val client: ApiClient = ApiClient(baseUrl = baseUrl)

    init {
        // Set authentication or other configurations here
        client.setApiKey(apiKey)
    }
}