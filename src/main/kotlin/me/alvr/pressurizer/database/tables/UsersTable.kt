package me.alvr.pressurizer.database.tables

import me.alvr.pressurizer.database.instant
import org.jetbrains.exposed.sql.Table
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * [steamId] identifier of the user, in Steam64Id format.
 * [country] current country of the user. Used to determine the prices of the games.
 * [updatedAt] last time an user fetched his/her Steam library.
 *
 */
object UsersTable : Table("users") {
    val steamId = varchar("steam_id", 20).primaryKey().uniqueIndex()
    val country = varchar("country", 2) references CountriesTable.code
    val updatedAt = instant("updated_at").default(Instant.now().minus(1L, ChronoUnit.DAYS))
}