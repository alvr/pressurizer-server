package me.alvr.pressurizer.routes.export

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.post
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import me.alvr.pressurizer.config.apiKeyConfig
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.AllSteamGames
import me.alvr.pressurizer.domain.ImportGame
import me.alvr.pressurizer.domain.OwnedGames
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.routes.ImportRoute
import me.alvr.pressurizer.utils.ALL_GAMES
import me.alvr.pressurizer.utils.OWNED_GAMES
import me.alvr.pressurizer.utils.client
import me.alvr.pressurizer.utils.toDataClass

@Suppress("UselessCallOnNotNull")
@KtorExperimentalLocationsAPI
internal fun Route.import() = authenticate {
    post<ImportRoute> {
        call.principal<SteamId>()?.let { user ->
            val multipart = call.receiveMultipart()

            val import = mutableListOf<ImportGame>()

            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    part.streamProvider().use { its ->
                        val data = runCatching {
                            String(its.readBytes()).toDataClass<List<ImportGame>>()
                        }.onFailure {
                            error("The file has an invalid format.")
                        }.getOrElse { emptyList() }

                        import.addAll(data.filterNot { it.appId.isNullOrBlank() })
                    }
                }
                part.dispose()
            }

            println(import)

            val userGames = Database.getGamesByUser(user)
            val importIds = import.map { it.appId }

            val gamesToImport = importIds.filter { it !in userGames }

            val gamesNames = mutableListOf<AllSteamGames.Applist.Apps.App>()
            if (gamesToImport.isNotEmpty()) {
                val response = client.get<AllSteamGames>(ALL_GAMES)
                val ownedGames = client.get<OwnedGames>(OWNED_GAMES.format(apiKeyConfig.steam(), user.id)).response.games
                gamesNames.addAll(response.applist.apps.app.filter { it.appid in gamesToImport })

                val chunks = runCatching {
                    val numOfChunks = if (gamesToImport.size / 4 < 1)
                        1
                    else
                        gamesToImport.size / 4
                    gamesToImport.chunked(numOfChunks)
                }.onFailure {
                    error("Can't find any games on this account.")
                }.getOrElse {
                    emptyList()
                }

                chunks.map { chunk ->
                    launch {
                        chunk.forEach { gameId ->
                            if (gameId in ownedGames.map { it.appId }) {
                                val gameName = gamesNames.find { it.appid == gameId }

                                gameName?.let { gName ->
                                    Database.insertGame(gameId, gName.name)
                                    val game = import.find { it.appId == gameId }

                                    game?.let { gameToImport ->
                                        val time = ownedGames.find { userGame -> userGame.appId == gameToImport.appId }?.playtime ?: 0
                                        Database.importUserGame(user, game, time)
                                    }
                                }
                            }
                        }
                    }
                }.joinAll()
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}