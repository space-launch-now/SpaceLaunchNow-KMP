package me.calebjones.spacelaunchnow.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import me.calebjones.spacelaunchnow.UserViewModel
import me.calebjones.spacelaunchnow.data.UserRepository
import me.calebjones.spacelaunchnow.data.UserRepositoryImpl
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.NextUpViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.HomeViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.UpdatesViewModel
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepositoryImpl
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.EventsRepositoryImpl

expect fun nativeConfig() : KoinAppDeclaration

val koinConfig = koinConfiguration {
    includes(nativeConfig())
    modules(networkModule, apiModule, appModule)
}

val appModule = module {
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
    viewModelOf(::UserViewModel)
    singleOf(::LaunchRepositoryImpl) { bind<LaunchRepository>() }
    viewModelOf(::LaunchViewModel)
    viewModelOf(::NextUpViewModel)
    viewModelOf(::HomeViewModel)
    singleOf(::UpdatesRepositoryImpl) { bind<UpdatesRepository>() }
    viewModelOf(::UpdatesViewModel)
    singleOf(::ArticlesRepositoryImpl) { bind<ArticlesRepository>() }
    singleOf(::EventsRepositoryImpl) { bind<EventsRepository>() }
}