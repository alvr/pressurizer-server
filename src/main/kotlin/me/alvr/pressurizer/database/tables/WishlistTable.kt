package me.alvr.pressurizer.database.tables

import org.jetbrains.exposed.sql.Table

object WishlistTable : Table("user_games_wishlist") {
    val steamId = (varchar("steam_id", 20) references UsersTable.steamId).index()
    val appId = varchar("app_id", 10)
}