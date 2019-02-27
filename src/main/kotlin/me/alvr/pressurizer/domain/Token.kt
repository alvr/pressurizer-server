package me.alvr.pressurizer.domain

/**
 * A Token used to verify authentication when required.
 *
 * @property token with JWT format
 * @constructor Creates an object with [token] in JWT format
 */
data class Token(val token: String)