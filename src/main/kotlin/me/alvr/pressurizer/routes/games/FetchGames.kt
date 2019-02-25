package me.alvr.pressurizer.routes.games

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.client.request.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import me.alvr.pressurizer.config.ServerSpec
import me.alvr.pressurizer.config.config
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.Game
import me.alvr.pressurizer.domain.OwnedGames
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.utils.APPID_DETAILS
import me.alvr.pressurizer.utils.OWNED_GAMES
import me.alvr.pressurizer.utils.client
import me.alvr.pressurizer.utils.getGameCost
import java.time.Instant
import java.time.temporal.ChronoUnit

internal fun Route.fetchGames() = authenticate {
    post("/fetchGames") {
        call.principal<SteamId>()?.let { user ->
            val updatedAt = Database.getUserById(user).updatedAt

            val sixHours = Instant.now().minus(6L, ChronoUnit.HOURS)
            val diff = ChronoUnit.MINUTES.between(sixHours, updatedAt)

            if (diff <= 0) {
                val ownedGames = client.get<OwnedGames>(OWNED_GAMES.format(config[ServerSpec.apikey], user.id)).response.games

                val inDatabase = Database.getGamesByUser(user)

                val chunks: List<List<OwnedGames.Response.Game>>
                try {
                    chunks = ownedGames.chunked(ownedGames.size / 4)
                } catch (_: NullPointerException) {
                    error("Can't find any games on this account.")
                }

                val newGames = ownedGames.size - inDatabase.size
                val updatedGames = ownedGames.size - newGames

                val country = Database.getUserById(user).country.code
                val currency = Database.getCurrencyInfo(country)

                chunks.map { chunk ->
                    launch {
                        chunk.forEach { game ->
                            if (game.appId in inDatabase) {
                                Database.updateUserGame(user, Game(game.appId, timePlayed = game.playtime))
                            } else {
                                Database.insertGame(game.appId, game.title)
                                val cost = client.get<String>(APPID_DETAILS.format(game.appId, country)).getGameCost(currency)
                                Database.insertUserGame(user, game.appId, cost, game.playtime)
                            }
                        }
                    }
                }.joinAll()

                Database.newUpdateAt(user)

                call.respond(
                    mapOf(
                        "new" to newGames,
                        "updated" to updatedGames
                    )
                )
            } else {
                call.respond(
                    mapOf(
                        "hours" to diff / 60,
                        "minutes" to diff % 60
                    )
                )
            }
        }
    }
}

