package me.alvr.pressurizer.server.auth

import com.google.gson.Gson
import io.kotlintest.assertSoftly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.ExpectSpec
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import me.alvr.pressurizer.auth.AuthJWT
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.domain.Token
import me.alvr.pressurizer.server.withTestPressurizer

class PostTokenTest : ExpectSpec({
    context("send a valid token") {
        expect("response status code is 200") {
            withTestPressurizer {
                val jwt = AuthJWT.sign(SteamId("Pressurizer"))

                handleRequest(HttpMethod.Post, "/token") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(Gson().toJson(Token(jwt)))
                }.apply {
                    assertSoftly {
                        response.status() shouldBe HttpStatusCode.OK
                        response.content shouldContain "ok"
                    }
                }
            }
        }
    }

    context("send an invalid token") {
        expect("response status code is 400") {
            withTestPressurizer {
                val jwt = "INVALID_TOKEN"

                handleRequest(HttpMethod.Post, "/token") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(Gson().toJson(Token(jwt)))
                }.apply {
                    assertSoftly {
                        response.status() shouldBe HttpStatusCode.BadRequest
                        response.content shouldContain "The token was expected to have 3 parts, but got 1."
                    }
                }
            }
        }
    }

    context("send a blank token") {
        expect("response status code is 400") {
            withTestPressurizer {
                handleRequest(HttpMethod.Post, "/token") {
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(Gson().toJson(Token("")))
                }.apply {
                    assertSoftly {
                        response.status() shouldBe HttpStatusCode.BadRequest
                        response.content shouldContain "The token was expected to have 3 parts, but got 1."
                    }
                }
            }
        }
    }
})