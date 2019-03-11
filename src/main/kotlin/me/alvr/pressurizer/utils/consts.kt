package me.alvr.pressurizer.utils

private const val STEAM_API_BASE = "http://api.steampowered.com/"

const val APPID_DETAILS = "https://store.steampowered.com/widget/%s/?cc=%s"
const val OWNED_GAMES = "${STEAM_API_BASE}IPlayerService/GetOwnedGames/v1/?key=%s&steamid=%s&include_appinfo=1&include_played_free_games=1"
const val PLAYER_SUMMARY = "${STEAM_API_BASE}ISteamUser/GetPlayerSummaries/v0002/?key=%s&steamids=%s"
const val WISHLIST = "https://store.steampowered.com/wishlist/profiles/%s"

val OPENID: String = System.getenv("OPENID")