package me.alvr.pressurizer.server

import com.google.gson.annotations.SerializedName

data class ApiResponseWishlist(
    @SerializedName("appId")
    val appId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("shops")
    val shops: List<Shop>
) {
    data class Shop(
        @SerializedName("name")
        val name: String,
        @SerializedName("price_discount")
        val priceDiscount: Int,
        @SerializedName("price_new")
        val priceNew: Double,
        @SerializedName("price_old")
        val priceOld: Double,
        @SerializedName("url")
        val url: String
    )
}