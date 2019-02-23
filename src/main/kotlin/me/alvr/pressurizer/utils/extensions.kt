package me.alvr.pressurizer.utils

import org.jsoup.Jsoup
import java.math.BigDecimal

fun String.getGameCost(): BigDecimal {
    val document = Jsoup.parse(this)

    val costWithComma = document.select("div.game_purchase_price").first()?.text()?.trim()
        ?: document.select("div.discount_original_price").first()?.text()?.trim()
        ?: "0"

    return costWithComma.replace(',', '.')
        .replace(Regex("[^\\d.]+"), "")
        .toBigDecimal()
}