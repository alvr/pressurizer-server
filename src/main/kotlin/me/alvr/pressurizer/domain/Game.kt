package me.alvr.pressurizer.domain

import java.math.BigDecimal

data class Game(
    val appId: String,
    val title: String? = null,
    val cost: BigDecimal? = BigDecimal.ZERO,
    val timePlayed: Int? = null,
    val costHours: BigDecimal? = BigDecimal.ZERO,
    val finished: Boolean? = null
)