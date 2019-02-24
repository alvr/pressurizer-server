package me.alvr.pressurizer.domain.mappers

import me.alvr.pressurizer.database.tables.CurrenciesTable
import me.alvr.pressurizer.domain.Currency
import org.jetbrains.exposed.sql.ResultRow

object CurrencyMapper : Mapper<ResultRow, Currency>() {
    override fun transform(model: ResultRow): Currency {
        return Currency(
            code = model[CurrenciesTable.code],
            symbol = model[CurrenciesTable.symbol],
            thousand = model[CurrenciesTable.thousand],
            decimal = model[CurrenciesTable.decimal]
        )
    }
}
