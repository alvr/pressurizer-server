package me.alvr.pressurizer.server.games

import io.kotlintest.Spec
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
                UserGamesTable.tableName,
                CountriesTable.tableName,
                CurrenciesTable.tableName,
                VersionTable.tableName
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
                        response.content shouldBe "{\"new\":4,\"updated\":0}"
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
                        response.content shouldBe "{\"new\":0,\"updated\":4}"
                    }
                }

                val userGames = Database.getGamesByUser(user)

                userGames.size shouldBe 4
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