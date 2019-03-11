package me.alvr.pressurizer.routes.wishlist

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Route

/**
 * All routes related to wishlist.
 *
 * @receiver Route
 */
@KtorExperimentalLocationsAPI
fun Route.wishlistsRoutes() {
    updateWishlist()
}