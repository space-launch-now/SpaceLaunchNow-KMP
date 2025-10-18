package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal

val LaunchNormal.launchUrl: String
    get() = "https://spacelaunchnow.app/launch/${this.slug}"
