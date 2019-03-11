package me.alvr.pressurizer.server.games

import io.kotlintest.Spec
import io.kotlintest.assertSoftly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.ExpectSpec
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import me.alvr.pressurizer.utils.AuthJWT
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.database.tables.GamesTable
import me.alvr.pressurizer.database.tables.UserGamesTable
import me.alvr.pressurizer.database.tables.UsersTable
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.server.withTestPressurizer
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.temporal.ChronoUnit

@KtorExperimentalLocationsAPI
class FetchGamesTest : ExpectSpec() {
    override fun afterSpec(spec: Spec) {
        transaction {
            listOf(
                UsersTable.tableName,
                GamesTable.tableName,
                UserGamesTable.tableName
            ).forEach {
                exec("TRUNCATE TABLE $it CASCADE;")
            }
            closeExecutedStatements()
        }
    }

    init {
        context("fetch games") {
            val user = SteamId("76561198319326905")
            val token = AuthJWT.sign(user)

            Database.insertUser(user, null)

            expect("insert new games") {
                withTestPressurizer {
                    handleRequest(HttpMethod.Post, "/games.json") {
                        addHeader("Authorization", "Bearer $token")
                    }.apply {
                        response.content shouldContain "\"new\":71"
                        response.content shouldContain "\"updated\":0"
                    }
                }

                val userGames = Database.getGamesByUser(user)

                userGames.size shouldBe 71
            }

            expect("update existing games") {
                updateAt(user)

                withTestPressurizer {
                    handleRequest(HttpMethod.Post, "/games.json") {
                        addHeader("Authorization", "Bearer $token")
                    }.apply {
                        response.content shouldContain "\"new\":0"
                        response.content shouldContain "\"updated\":71"
                    }
                }

                val userGames = Database.getGamesByUser(user)

                userGames.size shouldBe 71
            }

            expect("wait 6 hours for a new fetch") {
                withTestPressurizer {
                    handleRequest(HttpMethod.Post, "/games.json") {
                        addHeader("Authorization", "Bearer $token")
                    }.apply {
                        response.content shouldContain "\"hours\":5"
                        response.content shouldContain "\"minutes\":59"
                    }
                }
            }

            expect("user without games on steam") {
                val u = SteamId("76561198926567646")
                val jwt = AuthJWT.sign(u)
                Database.insertUser(u)

                withTestPressurizer {
                    handleRequest(HttpMethod.Post, "/games.json") {
                        addHeader("Authorization", "Bearer $jwt")
                    }.apply {
                       assertSoftly {
                           response.status() shouldBe HttpStatusCode.BadRequest
                           response.content shouldContain "Can\\u0027t find any games on this account."
                       }
                    }
                }
            }
        }

        context("invalid token") {
            expect("return error 401") {
                withTestPressurizer {
                    handleRequest(HttpMethod.Post, "/games.json") {
                        addHeader("Authorization", "Bearer InvalidToken")
                    }.apply {
                        response.status() shouldBe HttpStatusCode.Unauthorized
                    }
                }
            }
        }
    }

    private fun updateAt(user: SteamId) {
        transaction {
            UsersTable.update({ UsersTable.steamId eq user.id }) {
                it[UsersTable.updatedAt] = Instant.now().minus(1L, ChronoUnit.DAYS)
            }
        }
    }
}