package me.alvr.pressurizer.domain.mappers

import me.alvr.pressurizer.database.tables.GamesTable
import me.alvr.pressurizer.database.tables.UserGamesTable
import me.alvr.pressurizer.domain.Game
import me.alvr.pressurizer.utils.round
import org.jetbrains.exposed.sql.ResultRow
import java.math.RoundingMode

object UserGameMapper : Mapper<ResultRow, Game>() {
    override fun transform(model: ResultRow): Game {
        val cost = model[UserGamesTable.cost]
        val timePlayed = model[UserGamesTable.timePlayed]

        val costHours = if (timePlayed < 60)
            cost
        else
            cost / (timePlayed.toBigDecimal().divide(60.toBigDecimal(), RoundingMode.HALF_UP))

        return Game(
            appId = model[UserGamesTable.appId],
            title = model[GamesTable.title],
            cost = cost,
            timePlayed = timePlayed,
            costHours = costHours,
            finished = model[UserGamesTable.finished]
        )
    }

}