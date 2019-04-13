package me.alvr.pressurizer.server.games

import com.google.gson.Gson
import io.kotlintest.Spec
import io.kotlintest.TestCaseOrder
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.kotlintest.specs.ExpectSpec
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import me.alvr.pressurizer.utils.AuthJWT
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.database.tables.GamesTable
import me.alvr.pressurizer.database.tables.UserGamesTable
import me.alvr.pressurizer.database.tables.UserWishlistTable
import me.alvr.pressurizer.database.tables.UsersTable
import me.alvr.pressurizer.domain.Game
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.server.withTestPressurizer
import me.alvr.pressurizer.utils.round
import org.jetbrains.exposed.sql.transactions.transaction

@KtorExperimentalLocationsAPI
class UpdateGamesTest : ExpectSpec() {
    private val user = SteamId("76561198319326905")
    private val token = AuthJWT.sign(user)

    override fun testCaseOrder(): TestCaseOrder? = TestCaseOrder.Sequential

    override fun afterSpec(spec: Spec) {
        transaction {
            listOf(
                UsersTable.tableName,
                GamesTable.tableName,
                UserGamesTable.tableName,
                UserWishlistTable.tableName
            ).forEach {
                exec("TRUNCATE TABLE $it CASCADE;")
            }
            closeExecutedStatements()
        }
    }

    init {
        context("init games") {
            expect("NoContent return") {
                Database.insertUser(user, null)

                withTestPressurizer {
                    handleRequest(HttpMethod.Post, "/games") {
                        addHeader("Authorization", "Bearer $token")
                    }.apply {
                        response.status() shouldBe HttpStatusCode.OK
                    }
                }
            }
        }

        context("update games") {
            expect("change game is finished") {
                var allGames = Database.getGamesCompleteByUser(user)
                var games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                val selectedGame = games.shuffled().first()

                val updateFinished = selectedGame.copy(
                    finished = true
                )

                withTestPressurizer {
                    handleRequest(HttpMethod.Patch, "/games") {
                        addHeader("Authorization", "Bearer $token")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(Gson().toJson(updateFinished))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.NoContent
                    }
                }

                allGames = Database.getGamesCompleteByUser(user)
                games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                val gameUpdated = games.find { it.appId == updateFinished.appId }

                gameUpdated?.finished?.shouldBeTrue()
            }

            expect("change game playtime") {
                var allGames = Database.getGamesCompleteByUser(user)
                var games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                val selectedGame = games.shuffled().first()

                val updatePlaytime = selectedGame.copy(
                   timePlayed = 5520
                )

                withTestPressurizer {
                    handleRequest(HttpMethod.Patch, "/games") {
                        addHeader("Authorization", "Bearer $token")
                        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        setBody(Gson().toJson(updatePlaytime))
                    }.apply {
                        response.status() shouldBe HttpStatusCode.NoContent
                    }
                }

                allGames = Database.getGamesCompleteByUser(user)
                games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                val gameUpdated = games.find { it.appId == updatePlaytime.appId }

                gameUpdated?.timePlayed shouldBe 5520
            }

            context("update cost of game") {
                expect("valid game cost") {
                    var allGames = Database.getGamesCompleteByUser(user)
                    var games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                    val selectedGame = games.shuffled().first()

                    val updateCost = selectedGame.copy(
                        cost = 100.toBigDecimal()
                    )

                    withTestPressurizer {
                        handleRequest(HttpMethod.Patch, "/games") {
                            addHeader("Authorization", "Bearer $token")
                            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            setBody(Gson().toJson(updateCost))
                        }.apply {
                            response.status() shouldBe HttpStatusCode.NoContent
                        }
                    }

                    allGames = Database.getGamesCompleteByUser(user)
                    games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                    val gameUpdated = games.find { it.appId == updateCost.appId }

                    gameUpdated?.cost shouldBe 100.toBigDecimal().round()
                }

                expect("game cost is less than minimum") {
                    var allGames = Database.getGamesCompleteByUser(user)
                    var games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                    val selectedGame = games.shuffled().first()

                    val updateCost = selectedGame.copy(
                        cost = (-100).toBigDecimal()
                    )

                    withTestPressurizer {
                        handleRequest(HttpMethod.Patch, "/games") {
                            addHeader("Authorization", "Bearer $token")
                            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            setBody(Gson().toJson(updateCost))
                        }.apply {
                            response.status() shouldBe HttpStatusCode.NoContent
                        }
                    }

                    allGames = Database.getGamesCompleteByUser(user)
                    games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                    val gameUpdated = games.find { it.appId == updateCost.appId }

                    gameUpdated?.cost shouldBe 0.toBigDecimal().round()
                }

                expect("game cost is greater than maximum") {
                    var allGames = Database.getGamesCompleteByUser(user)
                    var games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                    val selectedGame = games.shuffled().first()

                    val updateCost = selectedGame.copy(
                        cost = (1_000_000_000).toBigDecimal()
                    )

                    withTestPressurizer {
                        handleRequest(HttpMethod.Patch, "/games") {
                            addHeader("Authorization", "Bearer $token")
                            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            setBody(Gson().toJson(updateCost))
                        }.apply {
                            response.status() shouldBe HttpStatusCode.NoContent
                        }
                    }

                    allGames = Database.getGamesCompleteByUser(user)
                    games = (allGames["games"] as List<*>).filterIsInstance<Game>()

                    val gameUpdated = games.find { it.appId == updateCost.appId }

                    gameUpdated?.cost shouldBe 999_999_999.toBigDecimal().round()
                }
            }
        }

        context("invalid token") {
            expect("return error 401") {
                withTestPressurizer {
                    handleRequest(HttpMethod.Patch, "/games") {
                        addHeader("Authorization", "Bearer InvalidToken")
                    }.apply {
                        response.status() shouldBe HttpStatusCode.Unauthorized
                    }
                }
            }
        }
    }
}