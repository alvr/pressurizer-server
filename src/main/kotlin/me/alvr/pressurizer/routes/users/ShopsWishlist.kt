package me.alvr.pressurizer.routes.users

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.routes.ShopsRoute

@KtorExperimentalLocationsAPI
internal fun Route.getShopsWishlist() = authenticate {
    get<ShopsRoute> {
        call.principal<SteamId>()?.let { user ->
            call.respond(Database.getShopWishlist(user))
        }
    }
}