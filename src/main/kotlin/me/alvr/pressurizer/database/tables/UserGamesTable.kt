package me.alvr.pressurizer.database.tables

import org.jetbrains.exposed.sql.Table

/**
 * Database table for countries.
 *
 * [steamId] Steam64Id of the user.
 * [appId] identification of the game.
 * [cost] price the user paid for the game.
 * [timePlayed] total minutes played.
 * [finished] true if the user finished the game.
 */
object UserGamesTable : Table("user_games") {
    val steamId = (varchar("steam_id", 20) references UsersTable.steamId).index()
    val appId = varchar("app_id", 10) references GamesTable.appId
    val cost = decimal("cost", 12, 2)
    val timePlayed = integer("time_played")
    val finished = bool("finished").default(false)
}