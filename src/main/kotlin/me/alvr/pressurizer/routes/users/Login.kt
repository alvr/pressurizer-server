package me.alvr.pressurizer.routes.users

import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.http.formUrlEncode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import me.alvr.pressurizer.config.ServerSpec
import me.alvr.pressurizer.config.config
import me.alvr.pressurizer.routes.LoginRoute
import me.alvr.pressurizer.utils.OPENID

@KtorExperimentalLocationsAPI
internal fun Route.login() = get<LoginRoute> {
    val scheme = call.request.origin.scheme
    val host = config[ServerSpec.publicHost]
    var port = ""

    if (config[ServerSpec.publicPort].isNotBlank())
        port = ":${config[ServerSpec.publicPort]}"

    val params = listOf(
        "openid.identity" to "http://specs.openid.net/auth/2.0/identifier_select",
        "openid.claimed_id" to "http://specs.openid.net/auth/2.0/identifier_select",
        "openid.ns" to "http://specs.openid.net/auth/2.0",
        "openid.mode" to "checkid_setup",
        "openid.realm" to "$scheme://$host$port/",
        "openid.return_to" to "$scheme://$host$port/login/auth"
    )

    call.respondRedirect("$OPENID?${params.formUrlEncode()}")
}