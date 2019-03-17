package me.alvr.pressurizer.server.games

import io.kotlintest.Spec
import io.kotlintest.specs.ExpectSpec
import io.ktor.http.HttpMethod
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.database.tables.GamesTable
import me.alvr.pressurizer.database.tables.UserGamesTable
import me.alvr.pressurizer.database.tables.UserWishlistTable
import me.alvr.pressurizer.database.tables.UsersTable
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.server.withTestPressurizer
import me.alvr.pressurizer.utils.AuthJWT
import org.jetbrains.exposed.sql.transactions.transaction

@KtorExperimentalLocationsAPI
class WishlistTest : ExpectSpec() {
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
        context("user without wishlist") {
            expect("empty wishlist") {
                val user = SteamId("user_without_games")
                val token = AuthJWT.sign(user)

                Database.insertUser(user)

                withTestPressurizer {
                    handleRequest(HttpMethod.Post, "/wishlist.json") {
                        addHeader("Authorization", "Bearer $token")
                    }.apply {
                        println(response.content)
                    }
                }
            }
        }
    }
}