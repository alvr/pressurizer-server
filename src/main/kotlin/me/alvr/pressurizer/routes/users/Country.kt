package me.alvr.pressurizer.routes.users

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.patch
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.Country
import me.alvr.pressurizer.domain.SteamId
import org.postgresql.util.PSQLException

internal fun Route.countries() = authenticate {
    get("/countries") {
        call.principal<SteamId>()?.let { user ->
            val userCountry = Database.getCountry(user)
            val countries = Database.getCountries()
            call.respond(
                mapOf(
                    "country" to userCountry,
                    "countries" to countries
                )
            )
        }
    }
}

internal fun Route.updateCountry() = authenticate {
    patch("/updateCountry") {
        call.principal<SteamId>()?.let { user ->
            val newData = call.receive<Country>()

            Database.updateCountry(user, newData)
            call.respond(mapOf("ok" to true))
        }
    }
}