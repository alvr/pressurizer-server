package me.alvr.pressurizer.routes.games

import io.ktor.routing.Route

/**
 * All routes related to games.
 *
 * @receiver Route
 */
fun Route.gamesRoutes() {
    fetchGames()
    updateGame()
}