package me.alvr.pressurizer.routes.games

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Route

/**
 * All routes related to games.
 *
 * @receiver Route
 */
@KtorExperimentalLocationsAPI
fun Route.gamesRoutes() {
    allGames()
    fetchGames()
    updateGame()
}