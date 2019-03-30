package me.alvr.pressurizer.server.users

import com.google.gson.Gson
import io.kotlintest.assertSoftly
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.ExpectSpec
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.AccountSettings
import me.alvr.pressurizer.domain.Country
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.server.withTestPressurizer
import me.alvr.pressurizer.utils.AuthJWT

@KtorExperimentalLocationsAPI
class ShopsWishlistTest : ExpectSpec({
    val user = SteamId("76561198319326905")
    val token = AuthJWT.sign(user)

    context("account routes") {
        Database.insertUser(user, "ES")

        expect("get user information") {
            withTestPressurizer {
                handleRequest(HttpMethod.Get, "/account") {
                    addHeader("Authorization", "Bearer $token")
                }.apply {
                    val res = Gson().fromJson(response.content, AccountSettings::class.java)

                    assertSoftly {
                        res.country shouldBe Country("ES", "Spain")
                        res.shops shouldBe listOf("steam")
                    }
                }
            }
        }

        expect("update user information") {
            val accountSettings = AccountSettings(
                Country("US", "United States"),
                listOf("steam", "bundlestars", "greenmangaming")
            )

            withTestPressurizer {
                handleRequest(HttpMethod.Patch, "/account") {
                    addHeader("Authorization", "Bearer $token")
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(Gson().toJson(accountSettings))
                }.apply {
                    response.status() shouldBe HttpStatusCode.NoContent
                }
            }

            withTestPressurizer {
                handleRequest(HttpMethod.Get, "/account") {
                    addHeader("Authorization", "Bearer $token")
                }.apply {
                    val res = Gson().fromJson(response.content, AccountSettings::class.java)

                    assertSoftly {
                        res.country shouldBe Country("US", "United States")
                        res.shops shouldContainAll  listOf("steam", "bundlestars", "greenmangaming")
                    }
                }
            }
        }
    }

    context("wishlist routes") {
        expect("get users preferred shops") {
            withTestPressurizer {
                handleRequest(HttpMethod.Get, "/shops") {
                    addHeader("Authorization", "Bearer $token")
                }.apply {
                    response.content?.split(",")?.shouldContainAll(listOf("steam", "bundlestars", "greenmangaming"))
                }
            }
        }
    }
})