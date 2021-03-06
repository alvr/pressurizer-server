package me.alvr.pressurizer.server.games

import com.google.gson.JsonParser
import io.kotlintest.shouldBe
import io.kotlintest.specs.ExpectSpec
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.server.withTestPressurizer
import me.alvr.pressurizer.utils.AuthJWT

@KtorExperimentalLocationsAPI
class AllGamesTest : ExpectSpec({
    context("get all games") {
        expect("all games by user") {
            withTestPressurizer {
                handleRequest(HttpMethod.Get, "/games") {
                    addHeader("Authorization", "Bearer InvalidToken")
                }.apply {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                }
            }
        }

        expect("user with no games") {
            val user = SteamId("user_without_games")
            val token = AuthJWT.sign(user)

            Database.insertUser(user)

            withTestPressurizer {
                handleRequest(HttpMethod.Get, "/games") {
                    addHeader("Authorization", "Bearer $token")
                }.apply {
                    val res = JsonParser().parse(response.content).asJsonObject

                    res["games"].asJsonArray.toList() shouldBe emptyList()
                }
            }
        }
    }

    context("invalid token") {
        expect("return error 401") {
            withTestPressurizer {
                handleRequest(HttpMethod.Get, "/games") {
                    addHeader("Authorization", "Bearer InvalidToken")
                }.apply {
                    response.status() shouldBe HttpStatusCode.Unauthorized
                }
            }
        }
    }
})