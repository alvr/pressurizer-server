package me.alvr.pressurizer.domain

data class UserGames(
    val user: User,
    val games: List<UserGames> = emptyList()
)