package me.alvr.pressurizer.routes.auth

import com.auth0.jwt.exceptions.JWTVerificationException
import io.ktor.application.call
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import me.alvr.pressurizer.auth.AuthJWT
import me.alvr.pressurizer.domain.Token

internal fun Route.token() = post("/token") {
    try {
        val token = call.receiveOrNull<Token>()?.token.orEmpty()
        AuthJWT.isValid(token)
    } catch (e: JWTVerificationException) {
        error(e.localizedMessage)
    }

    call.respond(mapOf("ok" to true))
}