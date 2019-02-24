package me.alvr.pressurizer.server.games

import io.kotlintest.Spec
import io.kotlintest.assertSoftly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.ExpectSpec
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import me.alvr.pressurizer.auth.AuthJWT
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.database.tables.CountriesTable
import me.alvr.pressurizer.database.tables.CurrenciesTable
import me.alvr.pressurizer.database.tables.GamesTable
import me.alvr.pressurizer.database.tables.UserGamesTable
import me.alvr.pressurizer.database.tables.UsersTable
import me.alvr.pressurizer.database.tables.VersionTable
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.server.withTestPressurizer
import org.jetbrains.exposed.sql.transactions.transaction

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
            val user = SteamId("76561198044411569")
            val token = AuthJWT.sign(user)

            Database.insertUser(user, null)

            expect("insert new games") {
                withTestPressurizer {
                    handleRequest(HttpMethod.Post, "/fetchGames") {
                        addHeader("Authorization", "Bearer $token")
                    }.apply {
                        response.content shouldContain "\"new\":4"
                        response.content shouldContain "\"updated\":0"
                    }
                }

                val userGames = Database.getGamesByUser(user)

                userGames.size shouldBe 4
            }

            expect("update existing games") {
                withTestPressurizer {
                    handleRequest(HttpMethod.Post, "/fetchGames") {
                        addHeader("Authorization", "Bearer $token")
                    }.apply {
                        response.content shouldContain "\"new\":0"
                        response.content shouldContain "\"updated\":4"
                    }
                }

                val userGames = Database.getGamesByUser(user)

                userGames.size shouldBe 4
            }

            expect("user without games on steam") {
                val jwt = AuthJWT.sign(SteamId("76561198926567646"))

                withTestPressurizer {
                    handleRequest(HttpMethod.Post, "/fetchGames") {
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
                    handleRequest(HttpMethod.Post, "/fetchGames") {
                        addHeader("Authorization", "Bearer InvalidToken")
                    }.apply {
                        response.status() shouldBe HttpStatusCode.Unauthorized
                    }
                }
            }
        }
    }
}