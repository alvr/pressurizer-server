package me.alvr.pressurizer.routes.users

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Route

/**
 * All routes related to authentication.
 *
 * @receiver Route
 */
@KtorExperimentalLocationsAPI
fun Route.usersRoutes() {
    login()
    auth()
    token()
    countries()
    updateCountry()
}