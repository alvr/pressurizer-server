package me.alvr.pressurizer.routes.users

import io.ktor.application.call
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.request.ApplicationRequest
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import kotlinx.coroutines.runBlocking
import me.alvr.pressurizer.utils.AuthJWT
import me.alvr.pressurizer.config.ServerSpec
import me.alvr.pressurizer.config.config
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.PlayerSummary
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.routes.LoginAuthRoute
import me.alvr.pressurizer.utils.OPENID
import me.alvr.pressurizer.utils.PLAYER_SUMMARY
import me.alvr.pressurizer.utils.client

@KtorExperimentalLocationsAPI
internal fun Route.auth() = get<LoginAuthRoute> {
    val auth = call.request.checkLogin()

    if (!auth.first || auth.second.id.isEmpty())
        error("Cannot verify the login")

    try {
        Database.getUserById(auth.second)
    } catch (_: NoSuchElementException) {
        val userInfo = client.get<PlayerSummary>(PLAYER_SUMMARY.format(config[ServerSpec.apikey], auth.second.id))
        val countryCode = userInfo.response.players.first().countryCode

        Database.insertUser(auth.second, countryCode)
    }

    val token = AuthJWT.sign(auth.second)

    call.respondRedirect("${config[ServerSpec.client]}#token=$token")
}

private fun ApplicationRequest.checkLogin(): Pair<Boolean, SteamId> {
    val params = mutableMapOf(
        "openid_assoc_handle" to queryParameters["openid.assoc_handle"],
        "openid_signed" to queryParameters["openid.signed"],
        "openid_sig" to queryParameters["openid.sig"],
        "openid_ns" to queryParameters["openid.ns"],
        "openid_mode" to "check_authentication"
    ).apply {
        queryParameters["openid.signed"]?.split(',')?.forEach {
            put("openid_$it", queryParameters["openid.$it"])
        }
    }

    val isValid = runBlocking {
        client.post<String>(OPENID) {
            contentType(ContentType.Application.Json)
            params.forEach { k, v ->
                parameter(k, v.orEmpty())
            }
        }
    }.contains("true")

    val user = queryParameters["openid.claimed_id"].orEmpty()
    val regex = Regex("^https://steamcommunity.com/openid/id/(7[0-9]{15,25}+)\$")
    val id = regex.find(user)?.groupValues?.last().orEmpty()

    return Pair(isValid, SteamId(id))
}