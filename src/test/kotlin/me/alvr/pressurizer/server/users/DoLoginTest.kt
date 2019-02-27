package me.alvr.pressurizer.server.users

import io.kotlintest.Spec
import io.kotlintest.assertSoftly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.ExpectSpec
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.formUrlEncode
import io.ktor.server.testing.handleRequest
import me.alvr.pressurizer.config.ServerSpec
import me.alvr.pressurizer.config.config
import me.alvr.pressurizer.server.withTestPressurizer
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.NottableString.not
import org.mockserver.model.NottableString.string
import org.mockserver.model.Parameter.param

class DoLoginTest : ExpectSpec() {

    override fun beforeSpec(spec: Spec) {
        val mockServer = ClientAndServer.startClientAndServer(6969)

        mockServer.`when`(
            request()
                .withMethod("POST")
                .withPath("/")
                .withQueryStringParameters(
                    param("openid_assoc_handle", "1234567890"),
                    param("openid_signed","signed,op_endpoint,claimed_id,identity,return_to,response_nonce,assoc_handle"),
                    param("openid_sig", "VALID_SIGNATURE"),
                    param("openid_ns", "http://specs.openid.net/auth/2.0"),
                    param("openid_mode", "check_authentication"),
                    param("openid_op_endpoint", "http://localhost:6969/"),
                    param("openid_claimed_id", "https://steamcommunity.com/openid/id/76561198004956893"),
                    param("openid_identity", "https://steamcommunity.com/openid/id/76561198004956893"),
                    param("openid_return_to", "http://localhost:5930/login/auth"),
                    param("openid_response_nonce", "VALID_NONCE")
                )
        ).respond(
            response()
                .withBody("{\"ok\":true}")
        )

        mockServer.`when`(
            request()
                .withMethod("POST")
                .withPath("/")
                .withQueryStringParameter(string("openid_sig"), not("VALID_SIGNATURE"))

        ).respond(
            response()
                .withBody("{\"ok\":false}")
        )

        mockServer.`when`(
            request()
                .withMethod("POST")
                .withPath("/")
                .withQueryStringParameter(string("openid_response_nonce"), not("VALID_NONCE"))
        ).respond(
            response()
                .withBody("{\"ok\":false}")
        )
    }

    init {
        context("valid login") {
            expect("return to client url") {
                val params = listOf(
                    "openid.assoc_handle" to "1234567890",
                    "openid.signed" to "signed,op_endpoint,claimed_id,identity,return_to,response_nonce,assoc_handle",
                    "openid.sig" to "VALID_SIGNATURE",
                    "openid.ns" to "http://specs.openid.net/auth/2.0",
                    "openid.mode" to "check_authentication",
                    "openid.op_endpoint" to "http://localhost:6969/",
                    "openid.claimed_id" to "https://steamcommunity.com/openid/id/76561198004956893",
                    "openid.identity" to "https://steamcommunity.com/openid/id/76561198004956893",
                    "openid.return_to" to "http://localhost:5930/login/auth",
                    "openid.response_nonce" to "VALID_NONCE"
                )

                withTestPressurizer {
                    handleRequest(HttpMethod.Get, "/login/auth?${params.formUrlEncode()}").apply {
                        response.headers["Location"] shouldContain config[ServerSpec.client].toString()
                    }
                }
            }
        }

        context("invalid login") {
            expect("invalid signature") {
                val params = listOf(
                    "openid.assoc_handle" to "1234567890",
                    "openid.signed" to "signed,op_endpoint,claimed_id,identity,return_to,response_nonce,assoc_handle",
                    "openid.sig" to "INVALID_SIGNATURE",
                    "openid.ns" to "http://specs.openid.net/auth/2.0",
                    "openid.mode" to "check_authentication",
                    "openid.op_endpoint" to "http://localhost:6969/",
                    "openid.claimed_id" to "https://steamcommunity.com/openid/id/76561198004956893",
                    "openid.identity" to "https://steamcommunity.com/openid/id/76561198004956893",
                    "openid.return_to" to "http://localhost:5930/login/auth",
                    "openid.response_nonce" to "VALID_NONCE"
                )

                withTestPressurizer {
                    handleRequest(HttpMethod.Get, "/login/auth?${params.formUrlEncode()}").apply {
                        assertSoftly {
                            response.status() shouldBe HttpStatusCode.BadRequest
                            response.content shouldContain "Cannot verify the login"
                        }
                    }
                }
            }

            expect("invalid response nonce") {
                val params = listOf(
                    "openid.assoc_handle" to "1234567890",
                    "openid.signed" to "signed,op_endpoint,claimed_id,identity,return_to,response_nonce,assoc_handle",
                    "openid.sig" to "VALID_SIGNATURE",
                    "openid.ns" to "http://specs.openid.net/auth/2.0",
                    "openid.mode" to "check_authentication",
                    "openid.op_endpoint" to "http://localhost:6969/",
                    "openid.claimed_id" to "https://steamcommunity.com/openid/id/76561198004956893",
                    "openid.identity" to "https://steamcommunity.com/openid/id/76561198004956893",
                    "openid.return_to" to "http://localhost:5930/login/auth",
                    "openid.response_nonce" to "INVALID_NONCE"
                )

                withTestPressurizer {
                    handleRequest(HttpMethod.Get, "/login/auth?${params.formUrlEncode()}").apply {
                        assertSoftly {
                            response.status() shouldBe HttpStatusCode.BadRequest
                            response.content shouldContain "Cannot verify the login"
                        }
                    }
                }
            }

            expect("invalid signature and response nonce") {
                val params = listOf(
                    "openid.assoc_handle" to "1234567890",
                    "openid.signed" to "signed,op_endpoint,claimed_id,identity,return_to,response_nonce,assoc_handle",
                    "openid.sig" to "INVALID_SIGNATURE",
                    "openid.ns" to "http://specs.openid.net/auth/2.0",
                    "openid.mode" to "check_authentication",
                    "openid.op_endpoint" to "http://localhost:6969/",
                    "openid.claimed_id" to "https://steamcommunity.com/openid/id/76561198004956893",
                    "openid.identity" to "https://steamcommunity.com/openid/id/76561198004956893",
                    "openid.return_to" to "http://localhost:5930/login/auth",
                    "openid.response_nonce" to "INVALID_NONCE"
                )

                withTestPressurizer {
                    handleRequest(HttpMethod.Get, "/login/auth?${params.formUrlEncode()}").apply {
                        assertSoftly {
                            response.status() shouldBe HttpStatusCode.BadRequest
                            response.content shouldContain "Cannot verify the login"
                        }
                    }
                }
            }
        }
    }
}