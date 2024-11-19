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
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepositoryImpl

expect fun nativeConfig() : KoinAppDeclaration

val koinConfig = koinConfiguration {
    includes(nativeConfig())
    modules(appModule, networkModule, apiModule)
}

val appModule = module {
    singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
    viewModelOf(::UserViewModel)
    singleOf(::LaunchRepositoryImpl) { bind<LaunchRepository>() }
    viewModelOf(::LaunchViewModel)
}