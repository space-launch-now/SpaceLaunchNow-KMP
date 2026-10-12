package me.calebjones.spacelaunchnow.api.extensions.ll

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.domain.model.Launch

val LaunchNormal.launchUrl: String
    get() = "https://spacelaunchnow.app/launch/${this.slug}"

val Launch.launchUrl: String
    get() = "https://spacelaunchnow.app/launch/${this.slug}"
