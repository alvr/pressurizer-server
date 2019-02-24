package me.alvr.pressurizer.domain.mappers

import me.alvr.pressurizer.database.tables.GamesTable
import me.alvr.pressurizer.domain.Game
import org.jetbrains.exposed.sql.ResultRow

object GameMapper : Mapper<ResultRow, Game>() {
    override fun transform(model: ResultRow): Game {
        return Game(
            appId = model[GamesTable.appId],
            title = model[GamesTable.title]
        )
    }
}