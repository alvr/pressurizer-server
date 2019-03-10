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
@Location("/token.json")
class TokenRoute

@KtorExperimentalLocationsAPI
@Location("/country.json")
class CountryRoute

//Games
@KtorExperimentalLocationsAPI
@Location("/games.json")
class GamesRoute