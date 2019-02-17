package me.alvr.pressurizer.unit

import com.auth0.jwt.exceptions.InvalidClaimException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import io.kotlintest.assertSoftly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.ExpectSpec
import me.alvr.pressurizer.auth.AuthJWT
import me.alvr.pressurizer.domain.SteamId

class AuthTest : ExpectSpec({
    context("a valid token") {
        val jwt = AuthJWT.sign(SteamId("76561197960287930"))

        expect("must be divided in three parts") {
            jwt.split('.').size shouldBe 3
        }

        expect("it is a instance of DecodedJWT") {
            AuthJWT.isValid(jwt).shouldBeInstanceOf<DecodedJWT>()
        }

        expect("it must return its original content") {
            val decodedId = AuthJWT.isValid(jwt).getClaim("id").asString()
            val steam = SteamId(decodedId)

            assertSoftly {
                decodedId shouldBe "76561197960287930"
                steam shouldBe SteamId("76561197960287930")
                steam.id shouldBe "76561197960287930"
            }
        }

        expect("each part of the decoded token is the same as original") {
            val parts = jwt.split('.')
            val decoded = AuthJWT.isValid(jwt)

            assertSoftly {
                decoded.header shouldBe parts[0]
                decoded.payload shouldBe parts[1]
                decoded.signature shouldBe parts[2]
            }
        }
    }

    context("a invalid token") {
        expect("a JWTVerificationException should be thrown") {
            val exception = shouldThrow<JWTVerificationException> {
                AuthJWT.isValid("THIS_IS_NOT_A_VALID_TOKEN")
            }

            exception.localizedMessage shouldBe "The token was expected to have 3 parts, but got 1."
        }

        expect("when expired, a TokenExpiredException should be thrown") {
            val exception = shouldThrow<TokenExpiredException> {
                AuthJWT.isValid(
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9" +
                            ".eyJpZCI6Ijc2NTYxMTk3OTYwMjg3OTMwIiwiZXhwIjoxNTQ2Mjk3MjAwfQ" +
                            ".farSnxWyp1fBbS56-xX220ywaV9RFeEk5rg3D8ZfVGlKp8SqrR0mu2G73m7JRQZHKu2cbI1dOERAggWJgTXGLQ"
                )
            }

            exception.localizedMessage shouldContain "The Token has expired"
        }

        expect("when used before a date, an InvalidClaimException should be thrown") {
            val exception = shouldThrow<InvalidClaimException> {
                AuthJWT.isValid(
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9" +
                            ".eyJuYmYiOjQxMDI0NDEyMDAsImlkIjoiNzY1NjExOTc5NjAyODc5MzAifQ" +
                            ".rK31Hly4hHDGaWJmxi3eSKmoFhML3bfaJZW7_hqINfmdpzPGWkorzPJgupKXto-i_vMbQbfPwoDzf6zPUSEmTg"
                )
            }

            exception.localizedMessage shouldContain "The Token can't be used before"
        }

        expect("when signed with a different hash, a SignatureVerificationException should be thrown") {
            val exception = shouldThrow<SignatureVerificationException> {
                AuthJWT.isValid(
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9" +
                            ".eyJuYmYiOjE1NDc2NTQzMjYsImlkIjoiNzY1NjExOTc5NjAyODc5MzAiLCJleHAiOjE1NDc5MTM1MjZ9" +
                            ".H7aMRTbYTABgPoeNt-c2xjWg1f-ntbS8J2cvoK6UY8vpjCMfHpGbu9LUSuJnkCLQU81b6DX10aVHQw8dEn9Z5w"
                )
            }

            exception.localizedMessage shouldContain "The Token's Signature resulted invalid when verified using the Algorithm"
        }
    }
})