package me.alvr.pressurizer.domain

import java.math.BigDecimal

data class ImportGame(
    val appId: String,
    val cost: BigDecimal = BigDecimal.ZERO,
    val finished: Boolean = false
) : JSONConvertable