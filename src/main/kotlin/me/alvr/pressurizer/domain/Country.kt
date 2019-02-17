package me.alvr.pressurizer.domain

data class Country(
    val code: String,
    val name: String? = null,
    val currency: Currency? = null
)