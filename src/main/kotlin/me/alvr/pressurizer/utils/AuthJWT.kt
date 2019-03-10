package me.alvr.pressurizer.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import me.alvr.pressurizer.config.ServerSpec
import me.alvr.pressurizer.config.config
import me.alvr.pressurizer.domain.SteamId
import java.time.Instant
import java.time.Period
import java.util.Date

/**
 * Singleton for creating and validating auth tokens.
 */
object AuthJWT {
    private val algorithm = Algorithm.HMAC512(config[ServerSpec.salt])

    val verifier: JWTVerifier by lazy { JWT.require(algorithm).build() }

    /**
     * Create an auth token based on the user's [SteamId].
     *
     * A token is valid for three days and can't be used before its creation date.
     *
     * @param steamId of the user
     * @return a valid token signed with [algorithm]
     */
    fun sign(steamId: SteamId): String {
        val now = Instant.now()

        return JWT.create()
            .withClaim("id", steamId.id)
            .withExpiresAt(Date.from(now.plus(Period.ofDays(3))))
            .withNotBefore(Date.from(now))
            .sign(algorithm)
    }

    /**
     * Check if [token] is valid.
     *
     * A [token] is valid when is used before its expiration date (creation date + 3 days),
     * is not used before its creation date and
     * is signed with the same [algorithm] when it was created.
     *
     * @param token to verify
     * @return a [DecodedJWT]
     * @throws JWTVerificationException if [token] is not valid
     */
    fun isValid(token: String): DecodedJWT = verifier.verify(token)
}