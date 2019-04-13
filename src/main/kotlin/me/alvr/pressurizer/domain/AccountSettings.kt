package me.alvr.pressurizer.domain

data class AccountSettings(
    val country: Country,
    val shops: List<String>
)