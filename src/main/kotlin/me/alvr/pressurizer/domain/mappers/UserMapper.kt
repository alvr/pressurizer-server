package me.alvr.pressurizer.domain.mappers

import me.alvr.pressurizer.database.tables.UsersTable
import me.alvr.pressurizer.domain.Country
import me.alvr.pressurizer.domain.SteamId
import me.alvr.pressurizer.domain.User
import org.jetbrains.exposed.sql.ResultRow

class UserMapper : Mapper<ResultRow, User>() {
    override fun transform(model: ResultRow): User {
        return User(
            id = SteamId(model[UsersTable.steamId]),
            country = model[UsersTable.country]?.let { c -> Country(c) },
            updatedAt = model[UsersTable.updatedAt]
        )
    }
}