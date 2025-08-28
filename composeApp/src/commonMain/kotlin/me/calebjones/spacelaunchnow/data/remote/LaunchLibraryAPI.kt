package me.calebjones.spacelaunchnow.data.remote

// Note: This class is currently disabled as it conflicts with the new generated API client structure
// The repository pattern now uses the generated API clients directly through Koin injection

/*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.infrastructure.ApiClient
import org.koin.core.component.KoinComponent

class LaunchLibraryAPIClient (
    baseUrl: String = "",
    apiKey: String = ""
) : KoinComponent {

    private val client: ApiClient = ApiClient(
        baseUrl = baseUrl, 
        httpClientEngine = CIO.create(),
        httpClientConfig = {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    )

    init {
        // Set authentication or other configurations here
        client.setApiKey(apiKey)
    }
}
*/