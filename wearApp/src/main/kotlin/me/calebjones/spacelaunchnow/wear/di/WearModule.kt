package me.calebjones.spacelaunchnow.wear.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import me.calebjones.spacelaunchnow.wear.data.EntitlementSyncManager
import me.calebjones.spacelaunchnow.wear.data.EntitlementSyncManagerImpl
import me.calebjones.spacelaunchnow.wear.data.WatchLaunchRepository
import me.calebjones.spacelaunchnow.wear.data.WatchLaunchRepositoryImpl
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchDetailViewModel
import me.calebjones.spacelaunchnow.wear.viewmodel.LaunchListViewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModelOf
import java.io.File

val wearModule = module {
    single<DataStore<Preferences>>(named("WearLaunchCacheDataStore")) {
        PreferenceDataStoreFactory.create {
            File(get<Context>().filesDir, "datastore/wear_launch_cache.preferences_pb")
        }
    }

    single<DataStore<Preferences>>(named("WearEntitlementDataStore")) {
        PreferenceDataStoreFactory.create {
            File(get<Context>().filesDir, "datastore/wear_entitlement.preferences_pb")
        }
    }

    single {
        EntitlementSyncManagerImpl(
            dataStore = get(named("WearEntitlementDataStore")),
            context = get(),
        )
    } bind EntitlementSyncManager::class

    single<WatchLaunchRepository> {
        WatchLaunchRepositoryImpl(
            context = get(),
            dataStore = get(named("WearLaunchCacheDataStore")),
        )
    }

    viewModelOf(::LaunchListViewModel)
    viewModelOf(::LaunchDetailViewModel)
}
