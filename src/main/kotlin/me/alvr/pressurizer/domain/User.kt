package me.alvr.pressurizer.domain

import java.time.Instant

data class User(
    val id: SteamId,
    val country: Country? = null,
    val updatedAt: Instant? = null
)