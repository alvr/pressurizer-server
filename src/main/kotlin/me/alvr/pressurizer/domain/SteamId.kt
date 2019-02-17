package me.alvr.pressurizer.domain

import io.ktor.auth.Principal

/**
 * A SteamId used for authentication when required.
 *
 * @property id steam64id of the user
 * @constructor Creates an object with [id] as the user steam64id
 */
data class SteamId(val id: String) : Principal