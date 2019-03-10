package me.alvr.pressurizer.routes.games

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.routes.GamesRoute

@KtorExperimentalLocationsAPI
internal fun Route.allGames() = authenticate {
    get<GamesRoute> {
        call.principal<SteamId>()?.let { user ->
            call.respond(Database.getGamesCompleteByUser(user))
        }
    }
}