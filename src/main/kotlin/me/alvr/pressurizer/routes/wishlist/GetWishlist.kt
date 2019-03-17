package me.alvr.pressurizer.routes.wishlist

import com.google.gson.JsonArray
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.client.request.get
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import me.alvr.pressurizer.database.Database
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.routes.WishlistRoute
import me.alvr.pressurizer.utils.ITAD_PLAIN_NAME
import me.alvr.pressurizer.utils.client
import com.google.gson.JsonParser
import me.alvr.pressurizer.config.apiKeyConfig
import me.alvr.pressurizer.domain.Shop
import me.alvr.pressurizer.utils.ITAD_PRICES
import me.alvr.pressurizer.utils.shopsName
import me.alvr.pressurizer.utils.toDataClass

@KtorExperimentalLocationsAPI
internal fun Route.getWishlist() = authenticate {
    get<WishlistRoute> {
        call.principal<SteamId>()?.let { user ->
            val userWishlistIds = Database.getWishlist(user)

            check(userWishlistIds.isNotEmpty()) { error("Empty wishlist") }

            val wishlist = userWishlistIds.joinToString(",") { "app/$it" }

            val country = Database.getCountry(user)
            val shops = Database.getShopWishlist(user)

            val plainNames = client.get<String>(ITAD_PLAIN_NAME.format(apiKeyConfig.itad(), wishlist)).getPlainNames()
            val data = client.get<String>(ITAD_PRICES.format(apiKeyConfig.itad(), plainNames, country.code, shops))

            call.respond(data.parsePrices(userWishlistIds))
        }
    }
}

private fun String.getPlainNames() = runCatching {
    JsonParser()
        .parse(this)
        .asJsonObject["data"]
        .asJsonObject
        .entrySet()
        .joinToString(",") { it.value.asString }
}.getOrElse { emptyList<String>() }

private suspend fun String.parsePrices(ids: List<String>) = JsonParser().parse(this)
    .asJsonObject["data"]
    .asJsonObject
    .entrySet()
    .withIndex()
    .map {
        mapOf(
            "appId" to ids[it.index],
            "name" to Database.getGameName(ids[it.index]),
            "shops" to it.value.value.asJsonObject["list"].asJsonArray.getShops()
        )
    }

private fun JsonArray.getShops() = this.map {
    it.asJsonObject.toDataClass<Shop>()
}.map {
    mapOf(
        "name" to shopsName.find { shop -> it.shop.id == shop.id }?.name,
        "price_new" to it.priceNew,
        "price_old" to it.priceOld,
        "price_discount" to it.priceDiscount,
        "url" to it.url
    )
}