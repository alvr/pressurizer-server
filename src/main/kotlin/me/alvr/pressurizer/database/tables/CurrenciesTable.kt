package me.alvr.pressurizer.database.tables

import org.jetbrains.exposed.sql.Table

/**
 * Database table for currencies.
 *
 * [code] currency code, using ISO4217 format.
 * [symbol] symbol used to identify the currency, like '$', '€' or '£'.
 * [isAfter] true if the symbol is placed after the value or false if placed before.
 * [thousand] thousand separator: '.', ',' or nothing.
 * [decimal] decimal separator: '.' or ','.
 */
object CurrenciesTable : Table("currencies") {
    val code = varchar("currency_code", 3).primaryKey().uniqueIndex()
    val symbol = varchar("currency_symbol", 5)
    val isAfter = bool("symbol_after")
    val thousand = char("thousand_separator").nullable()
    val decimal = char("decimal_separator")
}