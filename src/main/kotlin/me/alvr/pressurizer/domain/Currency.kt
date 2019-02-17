package me.alvr.pressurizer.domain

/**
 * @property code currency code, using ISO4217 format.
 * @property symbol symbol used to identify the currency, like '$', '€' or '£'.
 * @property isAfter true if the symbol is placed after the value or false if placed before.
 * @property thousand thousand separator: '.', ',' or nothing.
 * @property decimal decimal separator: '.' or ','.
 * @constructor Creates an object of type Currency.
 */
data class Currency(
    val code: String,
    val symbol: String,
    val isAfter: Boolean,
    val thousand: Char? = null,
    val decimal: Char
)