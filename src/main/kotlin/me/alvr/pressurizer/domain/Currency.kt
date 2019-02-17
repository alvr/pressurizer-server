package me.alvr.pressurizer.domain

data class Currency(
    val code: String,
    val symbol: String,
    val isAfter: Boolean,
    val thousand: Char? = null,
    val hundred: Char
)