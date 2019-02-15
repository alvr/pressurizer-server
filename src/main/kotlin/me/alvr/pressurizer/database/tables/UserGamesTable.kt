package me.alvr.pressurizer.database.tables

import org.jetbrains.exposed.sql.Table

/**
 * [user] Steam64Id of the user.
 * [game] identification of the game.
 * [cost] price the user paid for the game.
 * [timePlayed] total minutes played.
 * [finished] true if the user finished the game.
 *
 */
object UserGamesTable : Table("user_games") {
    val user = (varchar("owner", 20) references UsersTable.steamId).index()
    val game = varchar("game", 8) references GamesTable.appId
    val cost = decimal("cost", 6, 2)
    val timePlayed = integer("time_played")
    val finished = bool("finished").default(false)
}