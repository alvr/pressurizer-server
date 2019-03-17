package me.alvr.pressurizer.routes.wishlist

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.client.request.get
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.AllSteamGames
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.routes.WishlistRoute
import me.alvr.pressurizer.utils.ALL_GAMES
import me.alvr.pressurizer.utils.WISHLIST
import me.alvr.pressurizer.utils.client
import me.alvr.pressurizer.utils.getWishlist

enum class WishlistStatus {
    NEW,
    REMOVE
}

@KtorExperimentalLocationsAPI
internal fun Route.updateWishlist() = authenticate {
    post<WishlistRoute> {
        call.principal<SteamId>()?.let { user ->
            val userWishlist = client.get<String>(WISHLIST.format(user.id)).getWishlist()

            val wishlist = Database.getWishlist(user)

            val new = mutableListOf<String>()
            val remove = mutableListOf<String>()

            new.addAll(userWishlist.filter { it !in wishlist })
            remove.addAll(wishlist.filter { it !in userWishlist })

            val games = mutableMapOf<WishlistStatus, List<String>>().apply {
                put(WishlistStatus.NEW, new)
                put(WishlistStatus.REMOVE, remove)
            }

            val response = client.get<AllSteamGames>(ALL_GAMES)
            val gamesName = response.applist.apps.app.filter { it.appid in new }

            gamesName.forEach {
                Database.insertGame(it.appid, it.name)
            }

            Database.updateWishlist(user, games)

            call.respond(mapOf("ok" to true))
        }
    }
}