package me.calebjones.spacelaunchnow.di

import me.calebjones.spacelaunchnow.api.client.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.client.apis.LaunchersApi
import me.calebjones.spacelaunchnow.api.client.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.client.apis.AgenciesApi
import org.koin.core.qualifier.named
import org.koin.dsl.module

val apiModule = module {
    single<LaunchesApi> {
        LaunchesApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClient = get()
        )
    }

    single<LauncherConfigurationsApi> {
        LauncherConfigurationsApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClient = get()
        )
    }

    single<LaunchersApi> {
        LaunchersApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClient = get()
        )
    }

    single<AgenciesApi> {
        AgenciesApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClient = get()
        )
    }

}