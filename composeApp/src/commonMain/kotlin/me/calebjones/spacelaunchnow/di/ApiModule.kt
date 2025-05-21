package me.calebjones.spacelaunchnow.di

import me.calebjones.spacelaunchnow.api.client.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.client.apis.LaunchersApi
import me.calebjones.spacelaunchnow.api.client.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.client.apis.AgenciesApi
import org.koin.core.qualifier.named
import org.koin.dsl.module

val apiModule = module {    
    single<LaunchesApi> {
        val api = LaunchesApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClient = get()
        )
        // Set API key using the proper method
        api.setApiKey(get<String>(named("API_KEY")))
        // Set the API key prefix to "Bearer" as required by the API
        api.setApiKeyPrefix("Bearer")
        api 
    }    
    single<LauncherConfigurationsApi> {
        val api = LauncherConfigurationsApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClient = get()
        )
        // Set API key using the proper method
        api.setApiKey(get<String>(named("API_KEY")))
        // Set the API key prefix to "Bearer" as required by the API
        api.setApiKeyPrefix("Bearer")
        api
    }    
    single<LaunchersApi> {
        val api = LaunchersApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClient = get()
        )
        // Set API key using the proper method
        api.setApiKey(get<String>(named("API_KEY")))
        // Set the API key prefix to "Bearer" as required by the API
        api.setApiKeyPrefix("Bearer")
        api
    }    
    single<AgenciesApi> {
        val api = AgenciesApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClient = get()
        )
        // Set API key using the proper method
        api.setApiKey(get<String>(named("API_KEY")))
        // Set the API key prefix to "Bearer" as required by the API
        api.setApiKeyPrefix("Bearer")
        api
    }

}