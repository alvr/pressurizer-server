package me.alvr.pressurizer.routes.export

import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respondText
import io.ktor.routing.Route
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.routes.ExportRoute

@KtorExperimentalLocationsAPI
internal fun Route.export() = authenticate {
    get<ExportRoute> {
        call.principal<SteamId>()?.let { user ->
            val data = Database.exportData(user)

            val json = Gson().toJson(data)

            call.respondText(json)
        }
    }
}