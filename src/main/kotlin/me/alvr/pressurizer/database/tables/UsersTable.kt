package me.alvr.pressurizer.database.tables

import me.alvr.pressurizer.database.instant
import org.jetbrains.exposed.sql.Table
import java.util.Date

/**
 * Database table for users.
 *
 * [steamId] identifier of the user, in Steam64Id format.
 * [country] current country of the user. Used to determine the prices of the games.
 * [shops] names of shops to search games deals.
 * [updatedAt] last time an user fetched his/her Steam library.
 */
object UsersTable : Table("users") {
    val steamId = varchar("steam_id", 20).primaryKey().uniqueIndex()
    val country = varchar("country", 2).default("US") references CountriesTable.code
    val shops = varchar("preferred_shops", 500).default("steam")
    val updatedAt = instant("updated_at").default(Date(0L).toInstant())
}