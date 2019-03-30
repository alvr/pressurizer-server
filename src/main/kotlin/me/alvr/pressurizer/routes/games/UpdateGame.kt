package me.alvr.pressurizer.routes.games

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.patch
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.Game
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.routes.GamesRoute

@KtorExperimentalLocationsAPI
internal fun Route.updateGame() = authenticate {
    patch<GamesRoute> {
        call.principal<SteamId>()?.let { user ->
            val newData = call.receive<Game>()
            Database.updateUserGame(user, newData)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}