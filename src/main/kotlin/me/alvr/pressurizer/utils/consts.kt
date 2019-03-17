package me.alvr.pressurizer.utils

private const val STEAM_API_BASE = "http://api.steampowered.com/"

const val APPID_DETAILS = "https://store.steampowered.com/widget/%s/?cc=%s"
const val OWNED_GAMES = "${STEAM_API_BASE}IPlayerService/GetOwnedGames/v1/?key=%s&steamid=%s&include_appinfo=1&include_played_free_games=1"
const val PLAYER_SUMMARY = "${STEAM_API_BASE}ISteamUser/GetPlayerSummaries/v0002/?key=%s&steamids=%s"
const val ALL_GAMES = "${STEAM_API_BASE}ISteamApps/GetAppList/v0001"
const val WISHLIST = "https://store.steampowered.com/wishlist/profiles/%s"

private const val ITAD_API_BASE = "https://api.isthereanydeal.com/v01/game/"

const val ITAD_PLAIN_NAME = "${ITAD_API_BASE}plain/id/?key=%s&shop=steam&ids=%s"
const val ITAD_PRICES = "${ITAD_API_BASE}prices/?key=%s&plains=%s&country=%s&shops=%s&added=0"

val OPENID: String = System.getenv("OPENID")