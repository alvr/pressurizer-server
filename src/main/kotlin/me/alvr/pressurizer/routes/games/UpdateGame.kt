package me.alvr.pressurizer.routes.games

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.patch
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.Game
import me.alvr.pressurizer.domain.SteamId

internal fun Route.updateGame() = authenticate {
    patch("/updateGame") {
        call.principal<SteamId>()?.let { user ->
            val newData = call.receive<Game>()
            Database.updateUserGame(user, newData)
            call.respond(mapOf("ok" to true))
        }
    }
}