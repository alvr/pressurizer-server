package me.alvr.pressurizer.utils

private const val API_BASE = "http://api.steampowered.com/"

const val APPID_DETAILS = "https://store.steampowered.com/widget/%s/"
const val OPENID = "https://steamcommunity.com/openid/login"
const val OWNED_GAMES = "${API_BASE}IPlayerService/GetOwnedGames/v1/?key=%s&steamid=%s&include_appinfo=1&include_played_free_games=1"
const val PLAYER_SUMMARY = "${API_BASE}ISteamUser/GetPlayerSummaries/v0002/?key=%s&steamids=%s"