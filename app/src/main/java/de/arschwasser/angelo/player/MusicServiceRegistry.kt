package de.arschwasser.angelo.player

import android.content.Context

enum class ServiceType {
    YOUTUBE
//    ,SPOTIFY_PREMIUM
//    ,SPOTIFY_FREE
//    ,AMAZON
}

object MusicServiceRegistry {
    private val services = mapOf(
        //ServiceType.SPOTIFY_PREMIUM to SpotifyPremiumService(),
        //ServiceType.SPOTIFY_FREE to SpotifyFreeService(),
        ServiceType.YOUTUBE to YouTubeMusicService(),
    )

    fun get(type: ServiceType) = services[type]

    fun availableServices(context: Context) = services.values.filter { it.isAvailable(context) }
}
