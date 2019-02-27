package me.alvr.pressurizer.domain

/**
 * @property code ISO 3166-1 alpha-2 code.
 * @property name English short name officially used by the ISO 3166 Maintenance Agency (ISO 3166/MA).
 * @property currency [Currency][currency] used in the country.
 * @constructor Creates an object of type Country.
 */
data class Country(
    val code: String,
    val name: String? = null,
    val currency: Currency? = null
)