package me.alvr.pressurizer.server.users

import io.kotlintest.shouldBe
import io.kotlintest.specs.ExpectSpec
import io.ktor.features.origin
import io.ktor.http.HttpMethod
import io.ktor.http.formUrlEncode
import io.ktor.server.testing.handleRequest
import me.alvr.pressurizer.server.withTestPressurizer
import me.alvr.pressurizer.utils.OPENID

class LoginTest : ExpectSpec({
    context("login") {
        expect("return a valid url") {
            withTestPressurizer {
                handleRequest(HttpMethod.Get, "/login").apply {
                    val scheme = request.origin.scheme
                    val host = request.origin.host
                    val port = request.origin.port

                    val params = listOf(
                        "openid.identity" to "http://specs.openid.net/auth/2.0/identifier_select",
                        "openid.claimed_id" to "http://specs.openid.net/auth/2.0/identifier_select",
                        "openid.ns" to "http://specs.openid.net/auth/2.0",
                        "openid.mode" to "checkid_setup",
                        "openid.realm" to "$scheme://$host:$port/",
                        "openid.return_to" to "$scheme://$host:$port/login/auth"
                    )

                    response.headers["Location"] shouldBe "$OPENID?${params.formUrlEncode()}"
                }
            }
        }
    }
})