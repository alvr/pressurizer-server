package me.alvr.pressurizer.server.games

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.kotlintest.Spec
import io.kotlintest.assertSoftly
import io.kotlintest.inspectors.forAny
import io.kotlintest.inspectors.forAtLeastOne
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeGreaterThanOrEqual
import io.kotlintest.shouldBe
import io.kotlintest.specs.ExpectSpec
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.database.tables.GamesTable
import me.alvr.pressurizer.database.tables.UserGamesTable
import me.alvr.pressurizer.database.tables.UserWishlistTable
import me.alvr.pressurizer.database.tables.UsersTable
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.server.ApiResponseWishlist
import me.alvr.pressurizer.server.withTestPressurizer
import me.alvr.pressurizer.utils.AuthJWT
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

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
        val user = SteamId("76561198004956893")
        val token = AuthJWT.sign(user)

        context("update wishlist") {
            expect("empty wishlist") {
                val userNoGames = SteamId("user_without_games")
                val tokenNoGames = AuthJWT.sign(userNoGames)

                Database.insertUser(userNoGames)

                withTestPressurizer {
                    handleRequest(HttpMethod.Post, "/wishlist") {
                        addHeader("Authorization", "Bearer $tokenNoGames")
                    }.apply {
                        response.status() shouldBe HttpStatusCode.NoContent
                    }
                }
            }

            expect("user with wishlist") {
                Database.insertUser(user)

                withTestPressurizer {
                    handleRequest(HttpMethod.Post, "/wishlist") {
                        addHeader("Authorization", "Bearer $token")
                    }.apply {
                        response.status() shouldBe HttpStatusCode.NoContent
                    }
                }
            }
        }

        context("user wishlist") {
            expect("get user wishlist") {
                withTestPressurizer {
                    handleRequest(HttpMethod.Get, "/wishlist") {
                        addHeader("Authorization", "Bearer $token")
                    }.apply {
                        val res = Gson().fromJson<List<ApiResponseWishlist>>(response.content,
                            object : TypeToken<List<ApiResponseWishlist>>(){}.type)

                        val randomGame = res[Random.nextInt(res.size)]

                        assertSoftly {
                            res.size shouldBeGreaterThan 10
                            randomGame.shops.size shouldBeGreaterThanOrEqual 0

                            res.forAny {
                                it.shops.forAtLeastOne { shop ->
                                    shop.name shouldBe "Steam"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}