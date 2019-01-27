package me.alvr.pressurizer

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import me.alvr.pressurizer.auth.AuthJWT
import me.alvr.pressurizer.auth.SteamId
import me.alvr.pressurizer.config.ServerSpec
import me.alvr.pressurizer.config.config

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
    routing {
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
