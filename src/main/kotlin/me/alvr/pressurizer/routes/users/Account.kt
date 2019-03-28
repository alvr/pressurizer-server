package me.alvr.pressurizer.routes.users

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.patch
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.AccountSettings
import me.alvr.pressurizer.domain.Country
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.routes.AccountRoute

@KtorExperimentalLocationsAPI
internal fun Route.countries() = authenticate {
    get<AccountRoute> {
        call.principal<SteamId>()?.let { user ->
            val userCountry = Database.getCountry(user)
            val userWishlist = Database.getShopWishlist(user).split(",")
            call.respond(
                mapOf(
                    "country" to userCountry,
                    "shops" to userWishlist
                )
            )
        }
    }
}

@KtorExperimentalLocationsAPI
internal fun Route.updateCountry() = authenticate {
    patch<AccountRoute> {
        call.principal<SteamId>()?.let { user ->
            val newData = call.receive<AccountSettings>()

            Database.updateCountry(user, newData.country)
            Database.updateShopWishlist(user, newData.shops.joinToString(","))
            call.respond(HttpStatusCode.NoContent)
        }
    }
}