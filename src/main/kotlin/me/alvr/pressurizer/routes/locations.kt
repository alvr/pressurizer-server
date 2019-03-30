package me.alvr.pressurizer.routes

import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location

//Users
@KtorExperimentalLocationsAPI
@Location("/login")
class LoginRoute

@KtorExperimentalLocationsAPI
@Location("/login/auth")
class LoginAuthRoute

@KtorExperimentalLocationsAPI
@Location("/token")
class TokenRoute

@KtorExperimentalLocationsAPI
@Location("/account")
class AccountRoute

//Games
@KtorExperimentalLocationsAPI
@Location("/games")
class GamesRoute

//Wishlist
@KtorExperimentalLocationsAPI
@Location("/wishlist")
class WishlistRoute

@KtorExperimentalLocationsAPI
@Location("/shops")
class ShopsRoute