package me.alvr.pressurizer.routes.users

import com.auth0.jwt.exceptions.JWTVerificationException
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.post
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import me.alvr.pressurizer.utils.AuthJWT
import me.alvr.pressurizer.domain.Token
import me.alvr.pressurizer.routes.TokenRoute

@KtorExperimentalLocationsAPI
internal fun Route.token() = post<TokenRoute> {
    try {
        val token = call.receiveOrNull<Token>()?.token.orEmpty()
        AuthJWT.isValid(token)
    } catch (e: JWTVerificationException) {
        error(e.localizedMessage)
    }

    call.respond(mapOf("ok" to true))
}