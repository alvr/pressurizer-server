package me.alvr.pressurizer.domain

import com.google.gson.annotations.SerializedName

data class Shop(
    @SerializedName("price_new")
    val priceNew: Double,
    @SerializedName("price_old")
    val priceOld: Double,
    @SerializedName("price_cut")
    val priceDiscount: Double,
    @SerializedName("shop")
    val shop: ShopId,
    @SerializedName("url")
    val url: String
) : JSONConvertable {
    data class ShopId(
        @SerializedName("id")
        val id: String
    )
}