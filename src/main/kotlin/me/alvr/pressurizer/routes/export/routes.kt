package me.alvr.pressurizer.routes.export

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Route

/**
 * All routes related to export/import.
 *
 * @receiver Route
 */
@KtorExperimentalLocationsAPI
fun Route.exportRoutes() {
    export()
    import()
}