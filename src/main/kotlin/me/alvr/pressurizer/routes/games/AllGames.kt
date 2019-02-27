package me.alvr.pressurizer.routes.games

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.SteamId

internal fun Route.allGames() = authenticate {
    get("/allGames") {
        call.principal<SteamId>()?.let { user ->
            call.respond(Database.getGamesCompleteByUser(user))
        }
    }
}