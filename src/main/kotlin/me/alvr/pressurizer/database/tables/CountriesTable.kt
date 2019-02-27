package me.alvr.pressurizer.database.tables

import org.jetbrains.exposed.sql.Table

/**
 * Database table for countries.
 *
 * [code] ISO 3166-1 alpha-2 code.
 * [name] English short name officially used by the ISO 3166 Maintenance Agency (ISO 3166/MA).
 * [currency] [CurrenciesTable][currency] used in the country.
 */
object CountriesTable : Table("countries") {
    val code = varchar("country_code", 2).primaryKey().uniqueIndex()
    val name = varchar("country_name", 50)
    val currency = varchar("currency", 3).default("USD") references CurrenciesTable.code
}