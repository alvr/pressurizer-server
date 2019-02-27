package me.alvr.pressurizer.domain.mappers

import me.alvr.pressurizer.database.tables.CountriesTable
import me.alvr.pressurizer.domain.Country
import org.jetbrains.exposed.sql.ResultRow

object CountryMapper : Mapper<ResultRow, Country>() {
    override fun transform(model: ResultRow): Country {
        return Country(
            code = model[CountriesTable.code],
            name = model[CountriesTable.name]
        )
    }
}