package me.alvr.pressurizer

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.gson.gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import me.alvr.pressurizer.config.serverConfig
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.routes.export.exportRoutes
import me.alvr.pressurizer.routes.games.gamesRoutes
import me.alvr.pressurizer.routes.users.usersRoutes
import me.alvr.pressurizer.routes.wishlist.wishlistsRoutes
import me.alvr.pressurizer.utils.AuthJWT
import me.alvr.pressurizer.utils.StatusPageError

/**
 * Configuration of the server itself.
 *
 * @receiver Application of type [Application]
 */
@KtorExperimentalLocationsAPI
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
        gson {}
    }
    install(CORS) {
        method(HttpMethod.Delete)
        method(HttpMethod.Get)
        method(HttpMethod.Patch)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        header(HttpHeaders.Authorization)
        host(serverConfig.client().authority, listOf(serverConfig.client().scheme))
        allowCredentials = true
    }
    install(Locations)
    install(StatusPages) {
        exception<IllegalStateException> {
            call.respond(
                HttpStatusCode.BadRequest,
                StatusPageError(it.localizedMessage)
            )
        }
        status(HttpStatusCode.Unauthorized) {
            call.respond(
                HttpStatusCode.Unauthorized,
                StatusPageError("Invalid Token")
            )
        }
        status(HttpStatusCode.NotFound) {
            call.respond(
                HttpStatusCode.NotFound,
                StatusPageError("Endpoint ${call.request.uri} not found")
            )
        }
    }
    routing {
        usersRoutes()
        gamesRoutes()
        wishlistsRoutes()
        exportRoutes()

        get("/") {
            call.respondText { "Pressurizer" }
        }

        route("{...}") {
            handle {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

/**
 * Main entry point
 */
@KtorExperimentalLocationsAPI
fun main() {
    embeddedServer(
        Netty,
        host = serverConfig.host(),
        port = serverConfig.port(),
        module = Application::pressurizer
    ).start(true)
}
