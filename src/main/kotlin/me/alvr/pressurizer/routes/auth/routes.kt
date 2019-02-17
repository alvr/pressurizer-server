package me.alvr.pressurizer.routes.auth

import io.ktor.routing.Route

/**
 * All routes related to authentication.
 *
 * @receiver Route
 */
fun Route.authRoutes() {
    login()
    auth()
    token()
}