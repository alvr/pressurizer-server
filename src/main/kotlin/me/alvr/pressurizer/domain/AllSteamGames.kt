package me.alvr.pressurizer.domain

import com.google.gson.annotations.SerializedName

data class AllSteamGames(
    @SerializedName("applist")
    val applist: Applist
) {
    data class Applist(
        @SerializedName("apps")
        val apps: Apps
    ) {
        data class Apps(
            @SerializedName("app")
            val app: List<App>
        ) {
            data class App(
                @SerializedName("appid")
                val appid: String,
                @SerializedName("name")
                val name: String
            )
        }
    }
}