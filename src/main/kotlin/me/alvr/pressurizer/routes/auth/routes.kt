package me.alvr.pressurizer.routes.auth

import io.ktor.routing.Route

fun Route.authRoutes() {
    login()
    auth()
    token()
}