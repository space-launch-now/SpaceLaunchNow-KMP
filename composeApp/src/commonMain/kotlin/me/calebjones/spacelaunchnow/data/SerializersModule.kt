package me.calebjones.spacelaunchnow.data

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import me.calebjones.spacelaunchnow.api.client.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.client.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.client.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.client.models.PolymorphicLaunchEndpoint


val launchSerializersModule = SerializersModule {
    polymorphic(PolymorphicLaunchEndpoint::class) {
        subclass(LaunchBasic::class, LaunchBasic.serializer())
        subclass(LaunchNormal::class, LaunchNormal.serializer())
        subclass(LaunchDetailed::class, LaunchDetailed.serializer())
        // Register other subclasses here
    }
}