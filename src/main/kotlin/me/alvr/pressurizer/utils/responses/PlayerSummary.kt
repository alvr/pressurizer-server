package me.alvr.pressurizer.utils.responses

import com.google.gson.annotations.SerializedName

data class PlayerSummary(
    @SerializedName("response")
    val response: Response
) {
    data class Response(
        @SerializedName("players")
        val players: List<Player>
    ) {
        data class Player(
            @SerializedName("loccountrycode")
            val countryCode: String
        )
    }
}