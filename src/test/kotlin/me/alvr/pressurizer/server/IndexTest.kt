package me.alvr.pressurizer.server

import io.kotlintest.assertSoftly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.ExpectSpec
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest

class IndexTest : ExpectSpec({
    context("a running server") {
        expect("/ is a valid route") {
            withTestPressurizer {
                handleRequest(HttpMethod.Get, "/").apply {
                    assertSoftly {
                        response.status() shouldBe HttpStatusCode.OK
                        response.content shouldBe "Pressurizer"
                    }
                }
            }
        }

        expect("/invalid_route is an invalid route") {
            withTestPressurizer {
                handleRequest(HttpMethod.Get, "/invalid_route").apply {
                    response.status() shouldBe HttpStatusCode.NotFound
                    response.content shouldContain "Endpoint /invalid_route not found"
                }
            }
        }
    }
})