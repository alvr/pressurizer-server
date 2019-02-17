package me.alvr.pressurizer.database.tables

import org.jetbrains.exposed.sql.Table

/**
 * Database table for games.
 *
 * [appId] identifier of the game.
 * [name] name of the game.
 */
object GamesTable : Table("games") {
    val appId = varchar("app_id", 8).primaryKey().uniqueIndex()
    val name = varchar("name", 120)
}