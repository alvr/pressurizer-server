package me.alvr.pressurizer.domain

import java.time.Instant

/**
 * @property id identifier of the user, in Steam64Id format.
 * @property country current country of the user. Used to determine the prices of the games.
 * @property updatedAt last time an user fetched his/her Steam library.
 * @constructor Creates an object of type User.
 */
data class User(
    val id: SteamId,
    val country: Country,
    val updatedAt: Instant? = null
)