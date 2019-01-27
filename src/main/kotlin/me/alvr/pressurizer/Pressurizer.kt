package me.alvr.pressurizer

import com.auth0.jwt.exceptions.JWTVerificationException
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.ContentNegotiation
import io.ktor.features.ForwardedHeaderSupport
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import me.alvr.pressurizer.auth.AuthJWT
import me.alvr.pressurizer.auth.SteamId
import me.alvr.pressurizer.config.ServerSpec
import me.alvr.pressurizer.config.config
import me.alvr.pressurizer.routes.auth.authRoutes

/**
 * Configuration of the server itself.
 *
 * @receiver Application of type [Application]
 */
fun Application.pressurizer() {
    install(Authentication) {
        jwt {
            verifier(AuthJWT.verifier)
            validate {
                SteamId(it.payload.getClaim("id").asString())
            }
        }
    }
    install(ContentNegotiation) {
        gson {

        }
    }
    install(ForwardedHeaderSupport)
    install(StatusPages) {
        exception<IllegalStateException> {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to it.message))
        }
        exception<JWTVerificationException> {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Cannot verify token"))
        }
        status(HttpStatusCode.Unauthorized) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Token"))
        }
        status(HttpStatusCode.NotFound) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "API endpoint not found."))
        }
    }
    routing {
        authRoutes()

        get("/") {
            call.respondText { "Pressurizer" }
        }
    }
}

/**
 * Main entry point
 */
fun main() {
    embeddedServer(
        Netty,
        host = config[ServerSpec.host],
        port = config[ServerSpec.port],
        module = Application::pressurizer
    ).start(true)
}
