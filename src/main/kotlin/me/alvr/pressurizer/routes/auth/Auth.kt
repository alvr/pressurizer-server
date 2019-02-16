package me.alvr.pressurizer.routes.auth

import io.ktor.application.call
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.request.ApplicationRequest
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.coroutines.runBlocking
import me.alvr.pressurizer.auth.AuthJWT
import me.alvr.pressurizer.auth.SteamId
import me.alvr.pressurizer.config.ServerSpec
import me.alvr.pressurizer.config.config
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.utils.OPENID
import me.alvr.pressurizer.utils.PLAYER_SUMMARY
import me.alvr.pressurizer.utils.gClient
import me.alvr.pressurizer.utils.responses.PlayerSummary

internal fun Route.auth() = get("/login/auth") {
    val auth = call.request.checkLogin()

    if (!auth.first || auth.second.id.isEmpty())
        error("Cannot verify the login")

    if (Database.getUserById(auth.second).isEmpty()) {
        val userInfo = gClient.get<PlayerSummary>(PLAYER_SUMMARY.format(config[ServerSpec.apikey], auth.second.id))
        val countryCode = userInfo.response.players.first().countryCode

        Database.insertUser(auth.second, countryCode)
    }

    val token = AuthJWT.sign(auth.second)

    call.respondRedirect("${config[ServerSpec.client]}#token=$token")
}

private fun ApplicationRequest.checkLogin(): Pair<Boolean, SteamId> {
    val params = mutableMapOf(
        "openid_assoc_handle" to this.queryParameters["openid.assoc_handle"],
        "openid_signed" to this.queryParameters["openid.signed"],
        "openid_sig" to this.queryParameters["openid.sig"],
        "openid_ns" to this.queryParameters["openid.ns"],
        "openid_mode" to "check_authentication"
    ).apply {
        this@checkLogin.queryParameters["openid.signed"]?.split(',')?.forEach {
            put("openid_$it", this@checkLogin.queryParameters["openid.$it"])
        }
    }

    val isValid = runBlocking {
        gClient.post<String>(OPENID) {
            contentType(ContentType.Application.Json)
            params.forEach { k, v ->
                parameter(k, v.orEmpty())
            }
        }
    }.contains("true")

    val user = this.queryParameters["openid.claimed_id"].orEmpty()
    val regex = Regex("^https://steamcommunity.com/openid/id/(7[0-9]{15,25}+)\$")
    val id = regex.find(user)?.groupValues?.last().orEmpty()

    return Pair(isValid, SteamId(id))
}