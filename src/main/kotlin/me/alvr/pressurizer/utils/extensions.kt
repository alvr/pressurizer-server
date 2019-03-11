package me.alvr.pressurizer.utils

import me.alvr.pressurizer.domain.Currency
import org.jsoup.Jsoup
import java.math.BigDecimal

fun String.getGameCost(currency: Currency): BigDecimal {
    val document = Jsoup.parse(this)

    val price = document.select("div.game_purchase_price").first()?.text()?.trim()
        ?: document.select("div.discount_original_price").first()?.text()?.trim()
        ?: "0"

    return price
        .removePrefix(currency.symbol)
        .removePrefix(currency.code)            // In some cases, Steam adds the currency's code.
        .removeSuffix(currency.symbol)
        .removeSuffix(currency.code)
        .replace("--", "00") // CHF doesn't use .00, instead it uses .-- for decimals.
        .trim()
        .replace(currency.thousand.toString(), "")
        .replace(currency.decimal, '.')
        .toBigDecimal()
}

fun String.getWishlist() =
    """"appid":(\d*),""".toRegex().findAll(this).mapNotNull { it.groupValues[1] }.toList()

fun Iterable<BigDecimal>.sum(): BigDecimal = this.fold(BigDecimal.ZERO, BigDecimal::add)

fun Iterable<BigDecimal>.average(): BigDecimal = this.sum() / this.count().toBigDecimal()

fun Float.round() = kotlin.math.round(this * 100) / 100

fun BigDecimal.round(): BigDecimal = setScale(2, BigDecimal.ROUND_HALF_EVEN)