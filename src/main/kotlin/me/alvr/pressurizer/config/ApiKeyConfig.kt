package me.alvr.pressurizer.config

/**
 * [Configuration][apiKeyConfig] for database.
 *
 * [steam] Steam API key. Get yours here: https://steamcommunity.com/dev/apikey
 * [itad] IsThereAnyDeal API key. Get yours here: https://isthereanydeal.com/dev/app/
 */
interface ApiKeyConfig {
    fun steam(): String
    fun itad(): String
}