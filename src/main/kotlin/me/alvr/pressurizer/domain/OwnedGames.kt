package me.alvr.pressurizer.domain

import com.google.gson.annotations.SerializedName

data class OwnedGames(
    @SerializedName("response")
    val response: Response
) {
    data class Response(
        @SerializedName("games")
        val games: List<Game>
    ) {
        data class Game(
            @SerializedName("appid")
            val appId: String,
            @SerializedName("name")
            val title: String,
            @SerializedName("playtime_forever")
            val playtime: Int
        )
    }
}